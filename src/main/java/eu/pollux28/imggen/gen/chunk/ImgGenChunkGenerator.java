package eu.pollux28.imggen.gen.chunk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pollux28.imggen.ImgGen;
import eu.pollux28.imggen.gen.heightmap.HeightMapSource;
import eu.pollux28.imggen.gen.structures.StructuresSource;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.structure.*;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.*;
import net.minecraft.util.math.noise.*;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.ConfiguredStructureFeatures;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.surfacebuilder.ConfiguredSurfaceBuilders;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class ImgGenChunkGenerator extends ChunkGenerator{
    public static final Codec<ImgGenChunkGenerator> CODEC = RecordCodecBuilder.create((instance) -> instance.group(BiomeSource.CODEC.fieldOf("biome_source").forGetter((ImgGenChunkGenerator) -> ImgGenChunkGenerator.biomeSource), Codec.LONG.fieldOf("seed").stable().forGetter((ImgGenChunkGenerator) -> ImgGenChunkGenerator.worldSeed), ChunkGeneratorSettings.REGISTRY_CODEC.fieldOf("settings").forGetter((ImgGenChunkGenerator) -> ImgGenChunkGenerator.settings)).apply(instance, instance.stable(ImgGenChunkGenerator::new)));
    private static final float[] NOISE_WEIGHT_TABLE = Util.make(new float[13824], (array) -> {
        for(int i = 0; i < 24; ++i) {
            for(int j = 0; j < 24; ++j) {
                for(int k = 0; k < 24; ++k) {
                    array[i * 24 * 24 + j * 24 + k] = (float)calculateNoiseWeight(j - 12, k - 12, i - 12);
                }
            }
        }

    });
    private static final float[] BIOME_WEIGHT_TABLE = Util.make(new float[25], (fs) -> {
        for(int i = -2; i <= 2; ++i) {
            for(int j = -2; j <= 2; ++j) {
                float f = 10.0F / MathHelper.sqrt((float)(i * i + j * j) + 0.2F);
                fs[i + 2 + (j + 2) * 5] = f;
            }
        }

    });
    private static final BlockState AIR;
    private static final BlockState NETHERRACK;
    private static final BlockState END_STONE;
    private static final BlockState LAVA;
    private final int verticalNoiseResolution;
    private final int horizontalNoiseResolution;
    private final int noiseSizeX;
    private final int noiseSizeY;
    private final int noiseSizeZ;
    protected final ChunkRandom random;
    private final OctavePerlinNoiseSampler lowerInterpolatedNoise;
    private final OctavePerlinNoiseSampler upperInterpolatedNoise;
    private final OctavePerlinNoiseSampler interpolationNoise;
    private final NoiseSampler surfaceDepthNoise;
    private final OctavePerlinNoiseSampler densityNoise;
    private final SimplexNoiseSampler islandNoise;
    protected final BlockState defaultBlock;
    protected final BlockState defaultFluid;
    private final long worldSeed;
    protected final Supplier<ChunkGeneratorSettings> settings;
    private final int worldHeight;
    private final StructuresConfig structuresConfig;
    private StructuresSource structuresSource = null;
    public HeightMapSource heightMapSource = null;
    private int seaLevel;

    public ImgGenChunkGenerator(BiomeSource biomeSource, long worldSeed, Supplier<ChunkGeneratorSettings> supplier) {
        this(biomeSource, biomeSource, worldSeed, supplier);
    }
    public ImgGenChunkGenerator(BiomeSource biomeSource, long worldSeed,Supplier<ChunkGeneratorSettings> supplier, MutableRegistry<ConfiguredStructureFeature<?,?>>configuredStructureFeatures){
        this(biomeSource,biomeSource,worldSeed,supplier);
    }
    private ImgGenChunkGenerator(BiomeSource biomeSource, BiomeSource biomeSource2, long worldSeed, Supplier<ChunkGeneratorSettings> supplier) {
        super(biomeSource, biomeSource2, supplier.get().getStructuresConfig(), worldSeed);
        this.worldSeed = worldSeed;
        ChunkGeneratorSettings chunkGeneratorSettings = supplier.get();
        this.settings = supplier;
        GenerationShapeConfig generationShapeConfig = chunkGeneratorSettings.getGenerationShapeConfig();
        this.worldHeight = generationShapeConfig.getHeight();
        this.verticalNoiseResolution = generationShapeConfig.getSizeVertical() * 4;
        this.horizontalNoiseResolution = generationShapeConfig.getSizeHorizontal() * 4;
        this.defaultBlock = chunkGeneratorSettings.getDefaultBlock();
        this.defaultFluid = chunkGeneratorSettings.getDefaultFluid();
        this.noiseSizeX = 16 / this.horizontalNoiseResolution;
        this.noiseSizeY = generationShapeConfig.getHeight() / this.verticalNoiseResolution;
        this.noiseSizeZ = 16 / this.horizontalNoiseResolution;
        this.random = new ChunkRandom(worldSeed);
        this.lowerInterpolatedNoise = new OctavePerlinNoiseSampler(this.random, IntStream.rangeClosed(-15, 0));
        this.upperInterpolatedNoise = new OctavePerlinNoiseSampler(this.random, IntStream.rangeClosed(-15, 0));
        this.interpolationNoise = new OctavePerlinNoiseSampler(this.random, IntStream.rangeClosed(-7, 0));
        this.surfaceDepthNoise = generationShapeConfig.hasSimplexSurfaceNoise() ? new OctaveSimplexNoiseSampler(this.random, IntStream.rangeClosed(-3, 0)) : new OctavePerlinNoiseSampler(this.random, IntStream.rangeClosed(-3, 0));
        this.random.consume(2620);
        this.densityNoise = new OctavePerlinNoiseSampler(this.random, IntStream.rangeClosed(-15, 0));
        if (generationShapeConfig.hasIslandNoiseOverride()) {
            ChunkRandom chunkRandom = new ChunkRandom(worldSeed);
            chunkRandom.consume(17292);
            this.islandNoise = new SimplexNoiseSampler(chunkRandom);
        } else {
            this.islandNoise = null;
        }
        this.structuresConfig = supplier.get().getStructuresConfig();
        int seaLevel1 = settings.get().getSeaLevel();
        ImgGen.refreshConfig();
        this.seaLevel = seaLevel1;
        if(ImgGen.CONFIG.customHeightMap) {
            this.seaLevel = ImgGen.CONFIG.seaLevel;
        }
    }

    @Override
    protected Codec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    @Environment(EnvType.CLIENT)
    public ChunkGenerator withSeed(long seed) {
        return new ImgGenChunkGenerator(this.biomeSource.withSeed(seed),seed, this.settings);
    }

    public boolean method_28548(long l, RegistryKey<ChunkGeneratorSettings> registryKey) {
        return this.worldSeed == l && this.settings.get().equals(registryKey);
    }

    private double sampleNoise(int x, int y, int z, double horizontalScale, double verticalScale, double horizontalStretch, double verticalStretch) {
        double d = 0.0D;
        double e = 0.0D;
        double f = 0.0D;
        double g = 1.0D;

        for(int i = 0; i < 16; ++i) {
            double h = OctavePerlinNoiseSampler.maintainPrecision((double)x * horizontalScale * g);
            double j = OctavePerlinNoiseSampler.maintainPrecision((double)y * verticalScale * g);
            double k = OctavePerlinNoiseSampler.maintainPrecision((double)z * horizontalScale * g);
            double l = verticalScale * g;
            PerlinNoiseSampler perlinNoiseSampler = this.lowerInterpolatedNoise.getOctave(i);
            if (perlinNoiseSampler != null) {
                d += perlinNoiseSampler.sample(h, j, k, l, (double)y * l) / g;
            }

            PerlinNoiseSampler perlinNoiseSampler2 = this.upperInterpolatedNoise.getOctave(i);
            if (perlinNoiseSampler2 != null) {
                e += perlinNoiseSampler2.sample(h, j, k, l, (double)y * l) / g;
            }

            if (i < 8) {
                PerlinNoiseSampler perlinNoiseSampler3 = this.interpolationNoise.getOctave(i);
                if (perlinNoiseSampler3 != null) {
                    f += perlinNoiseSampler3.sample(OctavePerlinNoiseSampler.maintainPrecision((double)x * horizontalStretch * g), OctavePerlinNoiseSampler.maintainPrecision((double)y * verticalStretch * g), OctavePerlinNoiseSampler.maintainPrecision((double)z * horizontalStretch * g), verticalStretch * g, (double)y * verticalStretch * g) / g;
                }
            }

            g /= 2.0D;
        }

        return MathHelper.clampedLerp(d / 512.0D, e / 512.0D, (f / 10.0D + 1.0D) / 2.0D);
    }

    private double[] sampleNoiseColumn(int x, int z) {
        double[] ds = new double[this.noiseSizeY + 1];
        this.sampleNoiseColumn(ds, x, z);
        return ds;
    }

    private void sampleNoiseColumn(double[] buffer, int x, int z) {
        GenerationShapeConfig generationShapeConfig = this.settings.get().getGenerationShapeConfig();
        double ac;
        double ad;
        double ai;
        double aj;
        if (this.islandNoise != null) {
            ac = TheEndBiomeSource.getNoiseAt(this.islandNoise, x, z) - 8.0F;
            if (ac > 0.0D) {
                ad = 0.25D;
            } else {
                ad = 1.0D;
            }
        } else {
            float g = 0.0F;
            float h = 0.0F;
            float i = 0.0F;
            int k = this.seaLevel;
            float l = this.biomeSource.getBiomeForNoiseGen(x, k, z).getDepth();

            for(int m = -2; m <= 2; ++m) {
                for(int n = -2; n <= 2; ++n) {
                    Biome biome = this.biomeSource.getBiomeForNoiseGen(x + m, k, z + n);
                    float o = biome.getDepth();
                    float p = biome.getScale();
                    float s;
                    float t;
                    if (generationShapeConfig.isAmplified() && o > 0.0F) {
                        s = 1.0F + o * 2.0F;
                        t = 1.0F + p * 4.0F;
                    } else {
                        s = o;
                        t = p;
                    }

                    float u = o > l ? 0.5F : 1.0F;
                    float v = u * BIOME_WEIGHT_TABLE[m + 2 + (n + 2) * 5] / (s + 2.0F);
                    g += t * v;
                    h += s * v;
                    i += v;
                }
            }

            float w = h / i;
            float y = g / i;
            ai = w * 0.5F - 0.125F;
            aj = y * 0.9F + 0.1F;
            ac = ai * 0.265625D;
            ad = 96.0D / aj;
        }

        double ae = 684.412D * generationShapeConfig.getSampling().getXZScale();
        double af = 684.412D * generationShapeConfig.getSampling().getYScale();
        double ag = ae / generationShapeConfig.getSampling().getXZFactor();
        double ah = af / generationShapeConfig.getSampling().getYFactor();
        ai = generationShapeConfig.getTopSlide().getTarget();
        aj = generationShapeConfig.getTopSlide().getSize();
        double ak = generationShapeConfig.getTopSlide().getOffset();
        double al = generationShapeConfig.getBottomSlide().getTarget();
        double am = generationShapeConfig.getBottomSlide().getSize();
        double an = generationShapeConfig.getBottomSlide().getOffset();
        double ao = generationShapeConfig.hasRandomDensityOffset() ? this.getRandomDensityAt(x, z) : 0.0D;
        double ap = generationShapeConfig.getDensityFactor();
        double aq = generationShapeConfig.getDensityOffset();

        for(int ar = 0; ar <= this.noiseSizeY; ++ar) {
            double as = this.sampleNoise(x, ar, z, ae, af, ag, ah);
            double at = 1.0D - (double)ar * 2.0D / (double)this.noiseSizeY + ao;
            double au = at * ap + aq;
            double av = (au + ac) * ad;
            if (av > 0.0D) {
                as += av * 4.0D;
            } else {
                as += av;
            }

            double ax;
            if (aj > 0.0D) {
                ax = ((double)(this.noiseSizeY - ar) - ak) / aj;
                as = MathHelper.clampedLerp(ai, as, ax);
            }

            if (am > 0.0D) {
                ax = ((double)ar - an) / am;
                as = MathHelper.clampedLerp(al, as, ax);
            }

            buffer[ar] = as;
        }

    }

    private double getRandomDensityAt(int x, int z) {
        double d = this.densityNoise.sample(x * 200, 10.0D, z * 200, 1.0D, 0.0D, true);
        double f;
        if (d < 0.0D) {
            f = -d * 0.3D;
        } else {
            f = d;
        }

        double g = f * 24.575625D - 2.0D;
        return g < 0.0D ? g * 0.009486607142857142D : Math.min(g, 1.0D) * 0.006640625D;
    }

    public int getHeight(int x, int z, Heightmap.Type heightmapType) {
        if(ImgGen.CONFIG.customHeightMap){
            if(heightMapSource.getHeightMapDataProvider().isInImage(x,z)||ImgGen.CONFIG.repeatHeightMapImage){
                int height = this.heightMapSource.getHeight(x,z)+1;
                if(heightmapType== Heightmap.Type.WORLD_SURFACE_WG){
                    return Math.max(height, seaLevel+1);
                }
                return height;
            }
        }
        return this.sampleHeightmap(x, z, null, heightmapType.getBlockPredicate());
    }

    public BlockView getColumnSample(int x, int z) {
        BlockState[] blockStates = new BlockState[this.noiseSizeY * this.verticalNoiseResolution];
        this.sampleHeightmap(x, z, blockStates, null);
        return new VerticalBlockSample(blockStates);
    }

    private int sampleHeightmap(int x, int z, BlockState[] states, Predicate<BlockState> predicate) {
        int i = Math.floorDiv(x, this.horizontalNoiseResolution);
        int j = Math.floorDiv(z, this.horizontalNoiseResolution);
        int k = Math.floorMod(x, this.horizontalNoiseResolution);
        int l = Math.floorMod(z, this.horizontalNoiseResolution);
        double d = (double)k / (double)this.horizontalNoiseResolution;
        double e = (double)l / (double)this.horizontalNoiseResolution;
        double[][] ds = new double[][]{this.sampleNoiseColumn(i, j), this.sampleNoiseColumn(i, j + 1), this.sampleNoiseColumn(i + 1, j), this.sampleNoiseColumn(i + 1, j + 1)};

        for(int m = this.noiseSizeY - 1; m >= 0; --m) {
            double f = ds[0][m];
            double g = ds[1][m];
            double h = ds[2][m];
            double n = ds[3][m];
            double o = ds[0][m + 1];
            double p = ds[1][m + 1];
            double q = ds[2][m + 1];
            double r = ds[3][m + 1];

            for(int s = this.verticalNoiseResolution - 1; s >= 0; --s) {
                double t = (double)s / (double)this.verticalNoiseResolution;
                double u = MathHelper.lerp3(t, d, e, f, o, h, q, g, p, n, r);
                int v = m * this.verticalNoiseResolution + s;
                BlockState blockState = this.getBlockState(u, v);
                if (states != null) {
                    states[v] = blockState;
                }

                if (predicate != null && predicate.test(blockState)) {
                    return v + 1;
                }
            }
        }

        return 0;
    }

    protected BlockState getBlockState(double density, int y) {
        BlockState blockState3;
        if (density > 0.0D) {
            blockState3 = this.defaultBlock;
        } else if (y < this.seaLevel) {
            blockState3 = this.defaultFluid;
        } else {
            blockState3 = AIR;
        }
        return blockState3;
    }

    @Override
    public void buildSurface(ChunkRegion region, Chunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        int i = chunkPos.x;
        int j = chunkPos.z;
        ChunkRandom chunkRandom = new ChunkRandom();
        chunkRandom.setTerrainSeed(i, j);
        ChunkPos chunkPos2 = chunk.getPos();
        int k = chunkPos2.getStartX();
        int l = chunkPos2.getStartZ();
        double d = 0.0625D;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for(int m = 0; m < 16; ++m) {
            for(int n = 0; n < 16; ++n) {
                int o = k + m;
                int p = l + n;
                int q = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, m, n) + 1;
                int z = this.seaLevel;
                if(ImgGen.CONFIG.customHeightMap&& (heightMapSource.getHeightMapDataProvider().isInImage(o,p)||ImgGen.CONFIG.repeatHeightMapImage)){
                    z = heightMapSource.getHeight(o,p)+1;
                }
                double e = this.surfaceDepthNoise.sample((double)o * 0.0625D, (double)p * 0.0625D, 0.0625D, (double)m * 0.0625D) * 15.0D;
                Biome biome = region.getBiome(mutable.set(k + m, q, l + n));
                BlockState blockState = this.defaultBlock;
                BlockState blockStateF = this.defaultFluid;
                Biome.Category cat = biome.getCategory();
                if(cat == Biome.Category.NETHER){
                    blockState = NETHERRACK;
                    blockStateF = LAVA;
                }else if(cat == Biome.Category.THEEND){
                    blockState = END_STONE;
                }
                biome.buildSurface(chunkRandom, chunk, o, p, q, e, blockState, blockStateF, seaLevel, region.getSeed());

            }
        }

        this.buildBedrock(chunk, chunkRandom);
    }
    private void buildBedrock(Chunk chunk, Random random) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int i = chunk.getPos().getStartX();
        int j = chunk.getPos().getStartZ();
        ChunkGeneratorSettings chunkGeneratorSettings = this.settings.get();
        int k = chunkGeneratorSettings.getBedrockFloorY();
        int l = this.worldHeight - 1 - chunkGeneratorSettings.getBedrockCeilingY();
        boolean bl = l + 4 >= 0 && l < this.worldHeight;
        boolean bl2 = k + 4 >= 0 && k < this.worldHeight;
        if (bl || bl2) {
            Iterator var12 = BlockPos.iterate(i, 0, j, i + 15, 0, j + 15).iterator();

            while(true) {
                BlockPos blockPos;
                int o;
                do {
                    if (!var12.hasNext()) {
                        return;
                    }

                    blockPos = (BlockPos)var12.next();
                    if (bl) {
                        for(o = 0; o < 5; ++o) {
                            if (o <= random.nextInt(5)) {
                                chunk.setBlockState(mutable.set(blockPos.getX(), l - o, blockPos.getZ()), Blocks.BEDROCK.getDefaultState(), false);
                            }
                        }
                    }
                } while(!bl2);
                Biome biome2 = this.biomeSource.getBiomeForNoiseGen(blockPos.getX()>>2,0, blockPos.getZ()>>2);
                if(ImgGen.registryManager.get(Registry.BIOME_KEY).getId(biome2) == BiomeKeys.THE_VOID.getValue()){
                    for(o = 4; o >= 0; --o) {
                        if (o <= random.nextInt(5)) {
                            chunk.setBlockState(mutable.set(blockPos.getX(), k + o, blockPos.getZ()), Blocks.BEDROCK.getDefaultState(), false);
                        }
                    }
                }
            }
        }
    }

    public void populateNoise(WorldAccess world, StructureAccessor accessor, Chunk chunk) {
        ObjectList<StructurePiece> objectList = new ObjectArrayList(10);
        ObjectList<JigsawJunction> objectList2 = new ObjectArrayList(32);
        ChunkPos chunkPos = chunk.getPos();
        int i = chunkPos.x;
        int j = chunkPos.z;
        int k = i << 4;
        int l = j << 4;
        int[][] heightT =new int[16][16];
        Biome[][] biomeT = new Biome[16][16];
        for(int ix = 0;ix<16;ix++){
            for(int iz = 0;iz<16;iz++){
                if(ImgGen.CONFIG.customHeightMap){
                    heightT[ix][iz] = heightMapSource.getHeight(k+ix,l+iz);
                }
                biomeT[ix][iz] = this.biomeSource.getBiomeForNoiseGen((k>>2)+(ix>>2),80,(l>>2)+(iz>>2));
            }
        }
        for (StructureFeature<?> feature : StructureFeature.JIGSAW_STRUCTURES) {
            StructureFeature<?> structureFeature = feature;
            accessor.getStructuresWithChildren(ChunkSectionPos.from(chunkPos, 0), structureFeature).forEach((start) -> {
                Iterator var6 = start.getChildren().iterator();

                while (true) {
                    StructurePiece structurePiece;
                    do {
                        if (!var6.hasNext()) {
                            return;
                        }

                        structurePiece = (StructurePiece) var6.next();
                    } while (!structurePiece.intersectsChunk(chunkPos, 12));

                    if (structurePiece instanceof PoolStructurePiece) {
                        PoolStructurePiece poolStructurePiece = (PoolStructurePiece) structurePiece;
                        StructurePool.Projection projection = poolStructurePiece.getPoolElement().getProjection();
                        if (projection == StructurePool.Projection.RIGID) {
                            objectList.add(poolStructurePiece);
                        }

                        for (JigsawJunction jigsawJunction : poolStructurePiece.getJunctions()) {
                            int kx = jigsawJunction.getSourceX();
                            int lx = jigsawJunction.getSourceZ();
                            if (kx > k - 12 && lx > l - 12 && kx < k + 15 + 12 && lx < l + 15 + 12) {
                                objectList2.add(jigsawJunction);
                            }
                        }
                    } else {
                        objectList.add(structurePiece);
                    }
                }
            });
        }

        double[][][] ds = new double[2][this.noiseSizeZ + 1][this.noiseSizeY + 1];

        for(int m = 0; m < this.noiseSizeZ + 1; ++m) {
            ds[0][m] = new double[this.noiseSizeY + 1];
            this.sampleNoiseColumn(ds[0][m], i * this.noiseSizeX, j * this.noiseSizeZ + m);
            ds[1][m] = new double[this.noiseSizeY + 1];
        }

        ProtoChunk protoChunk = (ProtoChunk)chunk;
        Heightmap heightmap = protoChunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
        Heightmap heightmap2 = protoChunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        ObjectListIterator<StructurePiece> objectListIterator = objectList.iterator();
        ObjectListIterator<JigsawJunction> objectListIterator2 = objectList2.iterator();
        int n1 =objectList.size();
        int n2 = objectList2.size();

        for(int n = 0; n < this.noiseSizeX; ++n) {
            int p;
            for(p = 0; p < this.noiseSizeZ + 1; ++p) {
                this.sampleNoiseColumn(ds[1][p], i * this.noiseSizeX + n + 1, j * this.noiseSizeZ + p);
            }

            for(p = 0; p < this.noiseSizeZ; ++p) {
                ChunkSection chunkSection = protoChunk.getSection(15);
                chunkSection.lock();

                for(int q = this.noiseSizeY - 1; q >= 0; --q) {
                    double d = ds[0][p][q];
                    double e = ds[0][p + 1][q];
                    double f = ds[1][p][q];
                    double g = ds[1][p + 1][q];
                    double h = ds[0][p][q + 1];
                    double r = ds[0][p + 1][q + 1];
                    double s = ds[1][p][q + 1];
                    double t = ds[1][p + 1][q + 1];

                    for(int u = this.verticalNoiseResolution - 1; u >= 0; --u) {
                        int v = q * this.verticalNoiseResolution + u;
                        int w = v & 15;
                        int x = v >> 4;
                        if (chunkSection.getYOffset() >> 4 != x) {
                            chunkSection.unlock();
                            chunkSection = protoChunk.getSection(x);
                            chunkSection.lock();
                        }

                        double y = (double)u / (double)this.verticalNoiseResolution;
                        double z = MathHelper.lerp(y, d, h);
                        double aa = MathHelper.lerp(y, f, s);
                        double ab = MathHelper.lerp(y, e, r);
                        double ac = MathHelper.lerp(y, g, t);

                        for(int ad = 0; ad < this.horizontalNoiseResolution; ++ad) {
                            int ae = k + n * this.horizontalNoiseResolution + ad;
                            int af = ae & 15;
                            double ag = (double)ad / (double)this.horizontalNoiseResolution;
                            double ah = MathHelper.lerp(ag, z, aa);
                            double ai = MathHelper.lerp(ag, ab, ac);

                            for(int aj = 0; aj < this.horizontalNoiseResolution; ++aj) {
                                int ak = l + p * this.horizontalNoiseResolution + aj;
                                int al = ak & 15;
                                double am = (double)aj / (double)this.horizontalNoiseResolution;
                                double an = MathHelper.lerp(am, ah, ai);
                                double ao = MathHelper.clamp(an / 200.0D, -1.0D, 1.0D);

                                int at;
                                int au;
                                int ar;
                                for(ao = ao / 2.0D - ao * ao * ao / 24.0D; objectListIterator.hasNext(); ao += getNoiseWeight(at, au, ar) * 0.8D) {
                                    StructurePiece structurePiece = objectListIterator.next();
                                    BlockBox blockBox = structurePiece.getBoundingBox();
                                    at = Math.max(0, Math.max(blockBox.minX - ae, ae - blockBox.maxX));
                                    au = v - (blockBox.minY + (structurePiece instanceof PoolStructurePiece ? ((PoolStructurePiece)structurePiece).getGroundLevelDelta() : 0));
                                    ar = Math.max(0, Math.max(blockBox.minZ - ak, ak - blockBox.maxZ));
                                }

                                objectListIterator.back(n1);

                                while(objectListIterator2.hasNext()) {
                                    JigsawJunction jigsawJunction = objectListIterator2.next();
                                    int as = ae - jigsawJunction.getSourceX();
                                    at = v - jigsawJunction.getSourceGroundY();
                                    au = ak - jigsawJunction.getSourceZ();
                                    ao += getNoiseWeight(as, at, au) * 0.4D;
                                }

                                objectListIterator2.back(n2);
                                BlockState blockState = this.getBlockState(ao, v);
                                Biome biome2 = biomeT[af][al];
                                Biome.Category cat = biome2.getCategory();
                                if(ImgGen.CONFIG.customHeightMap) {
                                    if(this.heightMapSource.getHeightMapDataProvider().isInImage(ae,ak)||ImgGen.CONFIG.repeatHeightMapImage){
                                        int height = heightT[af][al];
                                        if(height>this.seaLevel){
                                            blockState=this.defaultBlock;
                                            if(v>height){
                                                blockState = AIR;
                                            }
                                        }else{
                                            if(v>=seaLevel){
                                                blockState=AIR;
                                            }else if(v<=height){
                                                blockState=defaultBlock;
                                            }else{
                                                blockState= defaultFluid;
                                            }
                                        }
                                    }
                                }

                                if (blockState != AIR) {
                                    if (blockState.getLuminance() != 0) {
                                        mutable.set(ae, v, ak);
                                        protoChunk.addLightSource(mutable);
                                    }
                                    if(cat==Biome.Category.NETHER){
                                        if(blockState==defaultFluid){
                                            blockState = LAVA;
                                        }else{
                                            blockState = NETHERRACK;
                                        }
                                    }else if(cat== Biome.Category.THEEND){
                                        if(blockState==defaultBlock){
                                            blockState= END_STONE;
                                        }
                                    }else if(world.getRegistryManager().get(Registry.BIOME_KEY).getId(biome2) == BiomeKeys.THE_VOID.getValue()){
                                        blockState = Blocks.VOID_AIR.getDefaultState();
                                    }
                                    chunkSection.setBlockState(af, w, al, blockState, false);
                                    heightmap.trackUpdate(af, v, al, blockState);
                                    heightmap2.trackUpdate(af, v, al, blockState);
                                }
                            }
                        }
                    }
                }

                chunkSection.unlock();
            }

            double[][] es = ds[0];
            ds[0] = ds[1];
            ds[1] = es;
        }

    }
    @Override
    public void carve(long seed, BiomeAccess access, Chunk chunk, GenerationStep.Carver carver) {
        BiomeAccess biomeAccess = access.withSource(this.biomeSource);
        ChunkRandom chunkRandom = new ChunkRandom();
        ChunkPos chunkPos = chunk.getPos();
        int j = chunkPos.x;
        int k = chunkPos.z;
        GenerationSettings generationSettings = this.biomeSource.getBiomeForNoiseGen(chunkPos.x << 2, 0, chunkPos.z << 2).getGenerationSettings();
        BitSet bitSet = ((ProtoChunk)chunk).getOrCreateCarvingMask(carver);
        for(int l = j - 8; l <= j + 8; ++l) {
            for(int m = k - 8; m <= k + 8; ++m) {
                List<Supplier<ConfiguredCarver<?>>> list = generationSettings.getCarversForStep(carver);
                ListIterator listIterator = list.listIterator();

                while(listIterator.hasNext()) {
                    int n = listIterator.nextIndex();
                    ConfiguredCarver<?> configuredCarver = (ConfiguredCarver)((Supplier)listIterator.next()).get();
                    chunkRandom.setCarverSeed(seed + (long)n, l, m);
                    if (configuredCarver.shouldCarve(chunkRandom, l, m)) {
                        if(ImgGen.CONFIG.customHeightMap){
                            if(heightMapSource.getHeightMapDataProvider().isInImage(j*16+l,k*16+m)||ImgGen.CONFIG.repeatHeightMapImage){
                                configuredCarver.carve(chunk, biomeAccess::getBiome, chunkRandom, chunk.getHighestNonEmptySectionYOffset()-16, l, m, j, k, bitSet);
                                continue;
                            }
                        }
                        configuredCarver.carve(chunk, biomeAccess::getBiome, chunkRandom, this.seaLevel, l, m, j, k, bitSet);

                    }
                }
            }
        }

    }

    private static double getNoiseWeight(int x, int y, int z) {
        int i = x + 12;
        int j = y + 12;
        int k = z + 12;
        if (i >= 0 && i < 24) {
            if (j >= 0 && j < 24) {
                return k >= 0 && k < 24 ? (double)NOISE_WEIGHT_TABLE[k * 24 * 24 + i * 24 + j] : 0.0D;
            } else {
                return 0.0D;
            }
        } else {
            return 0.0D;
        }
    }

    private static double calculateNoiseWeight(int x, int y, int z) {
        double d = x * x + z * z;
        double e = (double)y + 0.5D;
        double f = e * e;
        double g = Math.pow(2.718281828459045D, -(f / 16.0D + d / 16.0D));
        double h = -e * MathHelper.fastInverseSqrt(f / 2.0D + d / 2.0D) / 2.0D;
        return h * g;
    }

    public int getMaxY() {
        return this.worldHeight;
    }

    public int getSeaLevel() {
        return this.seaLevel;
    }

    public List<SpawnSettings.SpawnEntry> getEntitySpawnList(Biome biome, StructureAccessor accessor, SpawnGroup group, BlockPos pos) {
        if (accessor.getStructureAt(pos, true, StructureFeature.SWAMP_HUT).hasChildren()) {
            if (group == SpawnGroup.MONSTER) {
                return StructureFeature.SWAMP_HUT.getMonsterSpawns();
            }

            if (group == SpawnGroup.CREATURE) {
                return StructureFeature.SWAMP_HUT.getCreatureSpawns();
            }
        }

        if (group == SpawnGroup.MONSTER) {
            if (accessor.getStructureAt(pos, false, StructureFeature.PILLAGER_OUTPOST).hasChildren()) {
                return StructureFeature.PILLAGER_OUTPOST.getMonsterSpawns();
            }
            if (accessor.getStructureAt(pos, false, StructureFeature.MONUMENT).hasChildren()) {
                return StructureFeature.MONUMENT.getMonsterSpawns();
            }

            if (accessor.getStructureAt(pos, true, StructureFeature.FORTRESS).hasChildren()) {
                return StructureFeature.FORTRESS.getMonsterSpawns();
            }
        }
        return super.getEntitySpawnList(biome, accessor, group, pos);
    }

    public void setStructureStarts(DynamicRegistryManager dynamicRegistryManager, StructureAccessor structureAccessor, Chunk chunk, StructureManager structureManager, long worldSeed) {
        ChunkPos chunkPos = chunk.getPos();
        Biome biome = this.biomeSource.getBiomeForNoiseGen((chunkPos.x << 2) + 2, 0, (chunkPos.z << 2) + 2);
        if(ImgGen.CONFIG.customStructures||ImgGen.CONFIG.useBlackList){
            if(this.structuresSource == null){
                this.structuresSource = new StructuresSource(dynamicRegistryManager.get(Registry.CONFIGURED_STRUCTURE_FEATURE_WORLDGEN));
            }
            if(ImgGen.CONFIG.customStructures){
                for (Object o : structuresSource.getStructuresInPos(chunkPos)) {
                    if(o!=null) {
                        ConfiguredStructureFeature<?, ?> configuredStructureFeature = (ConfiguredStructureFeature<?, ?>) o;
                        this.setStructureStart(configuredStructureFeature, dynamicRegistryManager, structureAccessor, chunk, structureManager, worldSeed, chunkPos, biome, true);
                    }
                }
            }
        }
        if(ImgGen.CONFIG.placeVanillaStructures){
            this.setStructureStart(ConfiguredStructureFeatures.STRONGHOLD, dynamicRegistryManager, structureAccessor, chunk, structureManager, worldSeed, chunkPos, biome,false);
            for (Supplier<ConfiguredStructureFeature<?, ?>> configuredStructureFeatureSupplier : biome.getGenerationSettings().getStructureFeatures()) {
                this.setStructureStart(configuredStructureFeatureSupplier.get(), dynamicRegistryManager, structureAccessor, chunk, structureManager, worldSeed, chunkPos, biome,false);
            }
        }
    }



    private void setStructureStart(ConfiguredStructureFeature<?, ?> configuredStructureFeature, DynamicRegistryManager dynamicRegistryManager, StructureAccessor structureAccessor, Chunk chunk, StructureManager structureManager, long worldSeed, ChunkPos chunkPos, Biome biome, Boolean bl) {
        StructureStart<?> structureStart = structureAccessor.getStructureStart(ChunkSectionPos.from(chunk.getPos(), 0), configuredStructureFeature.feature, chunk);
        int i = structureStart != null ? structureStart.getReferences() : 0;
        StructureConfig structureConfig = this.structuresConfig.getForType(configuredStructureFeature.feature);
        if(ImgGen.CONFIG.customStructures||ImgGen.CONFIG.useBlackList){
            if (structureConfig != null) {
                if(bl){
                    if(ImgGen.CONFIG.useBlackList&&(ImgGen.CONFIG.blackListApplication ==1||ImgGen.CONFIG.blackListApplication ==2)){
                        if(this.structuresSource.configuredStructureFeaturesBlackList.contains(configuredStructureFeature.feature)) return;
                    }
                    StructureStart<FeatureConfig> structureStart2 = (StructureStart<FeatureConfig>) configuredStructureFeature.feature.createStart(chunkPos.x, chunkPos.z, BlockBox.empty(), i, worldSeed);
                    structureStart2.init(dynamicRegistryManager, this, structureManager, chunkPos.x, chunkPos.z, biome, configuredStructureFeature.config);
                    structureAccessor.setStructureStart(ChunkSectionPos.from(chunk.getPos(), 0), configuredStructureFeature.feature, structureStart2, chunk);
                }else{
                    if(ImgGen.CONFIG.useBlackList&&(ImgGen.CONFIG.blackListApplication ==0||ImgGen.CONFIG.blackListApplication==2)){
                        if(this.structuresSource.configuredStructureFeaturesBlackList.contains(configuredStructureFeature.feature)) return;
                    }
                    StructureStart<?> structureStart2 = configuredStructureFeature.tryPlaceStart(dynamicRegistryManager, this, this.biomeSource, structureManager, worldSeed, chunkPos, biome, i, structureConfig);
                    structureAccessor.setStructureStart(ChunkSectionPos.from(chunk.getPos(), 0), configuredStructureFeature.feature, structureStart2, chunk);
                }
            }
        }else{
            StructureStart<?> structureStart2 = configuredStructureFeature.tryPlaceStart(dynamicRegistryManager, this, this.biomeSource, structureManager, worldSeed, chunkPos, biome, i, structureConfig);
            structureAccessor.setStructureStart(ChunkSectionPos.from(chunk.getPos(), 0), configuredStructureFeature.feature, structureStart2, chunk);
        }
    }

    public void addStructureReferences(StructureWorldAccess structureWorldAccess, StructureAccessor accessor, Chunk chunk) {

        int j = chunk.getPos().x;
        int k = chunk.getPos().z;
        int l = j << 4;
        int m = k << 4;
        ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(chunk.getPos(), 0);

        for(int n = j - 8; n <= j + 8; ++n) {
            for(int o = k - 8; o <= k + 8; ++o) {
                long p = ChunkPos.toLong(n, o);

                for (StructureStart<?> start : structureWorldAccess.getChunk(n, o).getStructureStarts().values()) {
                    StructureStart structureStart = start;

                    try {
                        if (structureStart != StructureStart.DEFAULT && structureStart.getBoundingBox().intersectsXZ(l, m, l + 15, m + 15)) {
                            accessor.addStructureReference(chunkSectionPos, structureStart.getFeature(), p, chunk);
                            DebugInfoSender.sendStructureStart(structureWorldAccess, structureStart);
                        }
                    } catch (Exception var19) {
                        CrashReport crashReport = CrashReport.create(var19, "Generating structure reference");
                        CrashReportSection crashReportSection = crashReport.addElement("Structure");
                        crashReportSection.add("Id", () -> {
                            return Registry.STRUCTURE_FEATURE.getId(structureStart.getFeature()).toString();
                        });
                        crashReportSection.add("Name", () -> {
                            return structureStart.getFeature().getName();
                        });
                        crashReportSection.add("Class", () -> {
                            return structureStart.getFeature().getClass().getCanonicalName();
                        });
                        throw new CrashException(crashReport);
                    }
                }
            }
        }

    }

    public void populateEntities(ChunkRegion region) {
        int i = region.getCenterChunkX();
        int j = region.getCenterChunkZ();
        Biome biome = region.getBiome((new ChunkPos(i, j)).getStartPos());
        ChunkRandom chunkRandom = new ChunkRandom();
        chunkRandom.setPopulationSeed(region.getSeed(), i << 4, j << 4);
        SpawnHelper.populateEntities(region, biome, i, j, chunkRandom);

    }

    public void setStructuresSource(StructuresSource structuresSource) {
        this.structuresSource = structuresSource;
    }

    public StructuresSource getStructuresSource() {
        return structuresSource;
    }

    public void setHeightMapSource(HeightMapSource heightMapSource) {
        this.heightMapSource = heightMapSource;
    }

    public HeightMapSource getHeightMapSource() {
        return heightMapSource;
    }

    static {
        AIR = Blocks.AIR.getDefaultState();
        NETHERRACK = Blocks.NETHERRACK.getDefaultState();
        LAVA = Blocks.LAVA.getDefaultState();
        END_STONE = Blocks.END_STONE.getDefaultState();
    }
}
