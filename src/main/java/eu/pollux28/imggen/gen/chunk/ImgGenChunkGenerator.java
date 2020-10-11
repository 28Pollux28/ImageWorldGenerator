package eu.pollux28.imggen.gen.chunk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pollux28.imggen.gen.biomes.ImgGenBiomeSource;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.structure.JigsawJunction;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.util.Util;
import net.minecraft.util.math.*;
import net.minecraft.util.math.noise.*;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.gen.feature.StructureFeature;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class ImgGenChunkGenerator extends ChunkGenerator{
    private final long seed;
    private final BiomeSource biomeSource;
    protected final ChunkGeneratorSettings chunkGeneratorType;
    //CopyPaste from vanilla :
    private static final float[] field_16649 = (float[]) Util.make(new float[13824], (array) -> {
        for(int i = 0; i < 24; ++i) {
            for(int j = 0; j < 24; ++j) {
                for(int k = 0; k < 24; ++k) {
                    array[i * 24 * 24 + j * 24 + k] = (float)method_16571(j - 12, k - 12, i - 12);
                }
            }
        }

    });
    private static final float[] field_24775 = (float[])Util.make(new float[25], (fs) -> {
        for(int i = -2; i <= 2; ++i) {
            for(int j = -2; j <= 2; ++j) {
                float f = 10.0F / MathHelper.sqrt((float)(i * i + j * j) + 0.2F);
                fs[i + 2 + (j + 2) * 5] = f;
            }
        }

    });
    private static final BlockState AIR;
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
    private final OctavePerlinNoiseSampler field_24776;
    private final SimplexNoiseSampler field_24777;
    protected final BlockState defaultBlock;
    protected final BlockState defaultFluid;
    private final int field_24779;
    protected final Supplier<ChunkGeneratorSettings> settings;

    public ImgGenChunkGenerator(BiomeSource biomeSource, long worldSeed, Supplier<ChunkGeneratorSettings> supplier) {
        super(biomeSource, new StructuresConfig(true));
        this.biomeSource=biomeSource;
        this.settings=supplier;
        this.chunkGeneratorType= supplier.get();
        this.seed = worldSeed;
        GenerationShapeConfig noiseConfig = chunkGeneratorType.getGenerationShapeConfig();
        this.field_24779 = noiseConfig.getHeight();
        this.verticalNoiseResolution = noiseConfig.getSizeVertical() * 4;
        this.horizontalNoiseResolution = noiseConfig.getSizeHorizontal() * 4;
        this.defaultBlock = chunkGeneratorType.getDefaultBlock();
        this.defaultFluid = chunkGeneratorType.getDefaultFluid();
        this.noiseSizeX = 16 / this.horizontalNoiseResolution;
        this.noiseSizeY = noiseConfig.getHeight() / this.verticalNoiseResolution;
        this.noiseSizeZ = 16 / this.horizontalNoiseResolution;
        this.random = new ChunkRandom(seed);
        this.lowerInterpolatedNoise = new OctavePerlinNoiseSampler(this.random, IntStream.rangeClosed(-15, 0));
        this.upperInterpolatedNoise = new OctavePerlinNoiseSampler(this.random, IntStream.rangeClosed(-15, 0));
        this.interpolationNoise = new OctavePerlinNoiseSampler(this.random, IntStream.rangeClosed(-7, 0));
        this.surfaceDepthNoise = (NoiseSampler)(noiseConfig.hasSimplexSurfaceNoise() ? new OctaveSimplexNoiseSampler(this.random, IntStream.rangeClosed(-3, 0)) : new OctavePerlinNoiseSampler(this.random, IntStream.rangeClosed(-3, 0)));
        this.random.consume(2620);
        this.field_24776 = new OctavePerlinNoiseSampler(this.random, IntStream.rangeClosed(-15, 0));
        if (noiseConfig.hasIslandNoiseOverride()) {
            ChunkRandom chunkRandom = new ChunkRandom(seed);
            chunkRandom.consume(17292);
            this.field_24777 = new SimplexNoiseSampler(chunkRandom);
        } else {
            this.field_24777 = null;
        }
    }

	/*public static void registerChunkGenerator() {
		Registry.register(Registry.CHUNK_GENERATOR, EdoraMain.MOD_FULL_ID, ImgGenChunkGenerator.CODEC);
	}*/

    public static final Codec<ImgGenChunkGenerator> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter((generator) -> generator.biomeSource),
            Codec.LONG.fieldOf("seed").stable().forGetter((generator) -> generator.seed),
            ChunkGeneratorSettings.REGISTRY_CODEC.fieldOf("settings").forGetter((surfaceChunkGenerator) -> {
                return surfaceChunkGenerator.settings;
            }))
            .apply(instance, instance.stable(ImgGenChunkGenerator::new)));

    @Override
    protected Codec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    @Environment(EnvType.CLIENT)
    public ChunkGenerator withSeed(long seed) {
        return new ImgGenChunkGenerator(this.biomeSource.withSeed(seed),seed, this.settings);
    }
    /*@Override
    public int getSeaLevel() {

        return 62;
    }*/
    @Override
    public void buildSurface(ChunkRegion chunkRegion, Chunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        int i = chunkPos.x;
        int j = chunkPos.z;
        ChunkRandom chunkRandom = new ChunkRandom();
        chunkRandom.setTerrainSeed(i, j);
        ChunkPos chunkPos2 = chunk.getPos();
        int startX = chunkPos2.getStartX();
        int startZ = chunkPos2.getStartZ();
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for(int localX = 0; localX < 16; ++localX) {
            for(int localZ = 0; localZ < 16; ++localZ) {
                int x = startX + localX;
                int z = startZ + localZ;
                int height = /*getHeight(x, z);//*/chunk.sampleHeightmap(net.minecraft.world.Heightmap.Type.WORLD_SURFACE_WG, localX, localZ) + 1;
                double noise = chunkRandom.nextDouble();
                chunkRegion.getBiome(mutable.set(startX + localX, height, startZ + localZ)).buildSurface(chunkRandom, chunk, x, z, height, noise, Blocks.STONE.getDefaultState(), Blocks.WATER.getDefaultState(), this.getSeaLevel(), seed);
            }
        }

        this.buildBedrock(chunk, chunkRandom);
    }
    private void buildBedrock(Chunk chunk, Random random) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int i = chunk.getPos().getStartX();
        int j = chunk.getPos().getStartZ();
        int k = 0; //bedrock floor y
        int l = 0; //bedrock ceiling y
        Iterator<BlockPos> var9 = BlockPos.iterate(i, 0, j, i + 15, 0, j + 15).iterator();

        while(true) {
            BlockPos blockPos;
            int n;
            do {
                if (!var9.hasNext()) {
                    return;
                }

                blockPos = var9.next();
                if (l > 0) {
                    for(n = l; n >= l - 4; --n) {
                        if (n >= l - random.nextInt(5)) {
                            chunk.setBlockState(mutable.set(blockPos.getX(), n, blockPos.getZ()), Blocks.BEDROCK.getDefaultState(), false);
                        }
                    }
                }
            } while(k >= 256);

            for(n = k + 4; n >= k; --n) {
                if (n <= k + random.nextInt(5)) {
                    chunk.setBlockState(mutable.set(blockPos.getX(), n, blockPos.getZ()), Blocks.BEDROCK.getDefaultState(), false);
                }
            }
        }
    }
	/*@Override
	public void populateNoise(WorldAccess world, StructureAccessor accessor, Chunk chunk) {
		BlockPos.Mutable pos = new BlockPos.Mutable();
		for (int x = 0; x < 16; ++x) {
			pos.setX(x);
			chunk.getPos().toLong();
			for (int z = 0; z < 16; ++z) {
				pos.setZ(z);
				int height = chunk.sampleHeightmap(net.minecraft.world.Heightmap.Type.WORLD_SURFACE_WG, x, z) + 1;//getHeight(pos.getX(), pos.getZ());
				for (int y = 0; y <= height; ++y) {
					pos.setY(y);
					chunk.setBlockState(pos, Blocks.STONE.getDefaultState(), false);
				}
				for (int y = 0; y < getSeaLevel(); y++) {
					pos.setY(y);
					if (chunk.getBlockState(pos).isAir()) {
						chunk.setBlockState(pos, Blocks.WATER.getDefaultState(), false);
					}
				}
			}
		}
	}*/


    /*@Override
    public void populateBiomes(Chunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        ((ProtoChunk)chunk).setBiomes(new BiomeArray(chunkPos, this.biomeSource));
    }*/
	/*@Override
	public int getHeight(int x, int z, Type heightmapType) {
		return getHeight(x, z);
	}*/

	/*@Override
	public BlockView getColumnSample(int x, int z) {
		int height = getHeight(x, z);
		BlockState[] array = new BlockState[256];
		for (int y = 255; y >= 0; y--) {
			if (y > height) {
				if (y > getSeaLevel()) {
					array[y] = Blocks.AIR.getDefaultState();
				} else {
					array[y] = Blocks.WATER.getDefaultState();
				}
			} else {
				array[y] = Blocks.STONE.getDefaultState();
			}
		}

		return new VerticalBlockSample(array);
	}*/

	/*private int getHeight(int x, int z) {
		return 63;//BiomeSelector.getAlpha(x, z);
	}*/
    //CopyPasted from vanilla
    //TODO : Figure out how this shit works :

    private double sampleNoise(int x, int y, int z, double horizontalScale, double verticalScale, double horizontalStretch, double verticalStretch) {
        double d = 0.0D;
        double e = 0.0D;
        double f = 0.0D;
        boolean bl = true;
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
        GenerationShapeConfig noiseConfig = this.chunkGeneratorType.getGenerationShapeConfig();
        double ac;
        double ad;
        double ai;
        double aj;
        if (this.field_24777 != null) {
            ac = (double)(TheEndBiomeSource.getNoiseAt(this.field_24777, x, z) - 8.0F);
            if (ac > 0.0D) {
                ad = 0.25D;
            } else {
                ad = 1.0D;
            }
        } else {
            float g = 0.0F;
            float h = 0.0F;
            float i = 0.0F;
            //int j = true;
            int k = this.getSeaLevel();
            float l = this.biomeSource.getBiomeForNoiseGen(x, k, z).getDepth();

            for(int m = -2; m <= 2; ++m) {
                for(int n = -2; n <= 2; ++n) {
                    Biome biome = this.biomeSource.getBiomeForNoiseGen(x + m, k, z + n);
                    float o = biome.getDepth();
                    float p = biome.getScale();
                    float s;
                    float t;
                    if ((noiseConfig.isAmplified()||biome.equals(BiomeKeys.SHATTERED_SAVANNA)) && o > 0.0F) {
                        s = 1.0F + o * 2.0F;
                        t = 1.0F + p * 4.0F;
                    } else {
                        s = o;
                        t = p;
                    }

                    float u = o > l ? 0.5F : 1.0F;
                    float v = u * field_24775[m + 2 + (n + 2) * 5] / (s + 2.0F);
                    g += t * v;
                    h += s * v;
                    i += v;
                }
            }

            float w = h / i;
            float y = g / i;
            ai = (double)(w * 0.5F - 0.125F);
            aj = (double)(y * 0.9F + 0.1F);
            ac = ai * 0.265625D;
            ad = 96.0D / aj;
        }

        double ae = 684.412D * noiseConfig.getSampling().getXZScale();
        double af = 684.412D * noiseConfig.getSampling().getYScale();
        double ag = ae / noiseConfig.getSampling().getXZFactor();
        double ah = af / noiseConfig.getSampling().getYFactor();
        ai = (double)noiseConfig.getTopSlide().getTarget();
        aj = (double)noiseConfig.getTopSlide().getSize();
        double ak = (double)noiseConfig.getTopSlide().getOffset();
        double al = (double)noiseConfig.getBottomSlide().getTarget();
        double am = (double)noiseConfig.getBottomSlide().getSize();
        double an = (double)noiseConfig.getBottomSlide().getOffset();
        double ao = noiseConfig.hasRandomDensityOffset() ? this.method_28553(x, z) : 0.0D;
        double ap = noiseConfig.getDensityFactor();
        double aq = noiseConfig.getDensityOffset();

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

    private double method_28553(int i, int j) {
        double d = this.field_24776.sample((double)(i * 200), 10.0D, (double)(j * 200), 1.0D, 0.0D, true);
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
        return this.sampleHeightmap(x, z, (BlockState[])null, heightmapType.getBlockPredicate());
    }


    public BlockView getColumnSample(int x, int z) {
        BlockState[] blockStates = new BlockState[this.noiseSizeY * this.verticalNoiseResolution];
        this.sampleHeightmap(x, z, blockStates, (Predicate)null);
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
        } else if (y < this.getSeaLevel()) {
            blockState3 = this.defaultFluid;
        } else {
            blockState3 = AIR;
        }

        return blockState3;
    }

    public void populateNoise(WorldAccess world, StructureAccessor accessor, Chunk chunk) {
        ObjectList<StructurePiece> objectList = new ObjectArrayList(10);
        ObjectList<JigsawJunction> objectList2 = new ObjectArrayList(32);
        ChunkPos chunkPos = chunk.getPos();
        int i = chunkPos.x;
        int j = chunkPos.z;
        int k = i << 4;
        int l = j << 4;
        Iterator var11 = StructureFeature.JIGSAW_STRUCTURES.iterator();

        while(var11.hasNext()) {
            StructureFeature<?> structureFeature = (StructureFeature)var11.next();
            accessor.getStructuresWithChildren(ChunkSectionPos.from(chunkPos, 0), structureFeature).forEach((start) -> {
                Iterator var6 = start.getChildren().iterator();

                while(true) {
                    while(true) {
                        StructurePiece structurePiece;
                        do {
                            if (!var6.hasNext()) {
                                return;
                            }

                            structurePiece = (StructurePiece)var6.next();
                        } while(!structurePiece.intersectsChunk(chunkPos, 12));

                        if (structurePiece instanceof PoolStructurePiece) {
                            PoolStructurePiece poolStructurePiece = (PoolStructurePiece)structurePiece;
                            StructurePool.Projection projection = poolStructurePiece.getPoolElement().getProjection();
                            if (projection == StructurePool.Projection.RIGID) {
                                objectList.add(poolStructurePiece);
                            }

                            Iterator var10 = poolStructurePiece.getJunctions().iterator();

                            while(var10.hasNext()) {
                                JigsawJunction jigsawJunction = (JigsawJunction)var10.next();
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
                                for(ao = ao / 2.0D - ao * ao * ao / 24.0D; objectListIterator.hasNext(); ao += method_16572(at, au, ar) * 0.8D) {
                                    StructurePiece structurePiece = (StructurePiece)objectListIterator.next();
                                    BlockBox blockBox = structurePiece.getBoundingBox();
                                    at = Math.max(0, Math.max(blockBox.minX - ae, ae - blockBox.maxX));
                                    au = v - (blockBox.minY + (structurePiece instanceof PoolStructurePiece ? ((PoolStructurePiece)structurePiece).getGroundLevelDelta() : 0));
                                    ar = Math.max(0, Math.max(blockBox.minZ - ak, ak - blockBox.maxZ));
                                }

                                objectListIterator.back(objectList.size());

                                while(objectListIterator2.hasNext()) {
                                    JigsawJunction jigsawJunction = (JigsawJunction)objectListIterator2.next();
                                    int as = ae - jigsawJunction.getSourceX();
                                    at = v - jigsawJunction.getSourceGroundY();
                                    au = ak - jigsawJunction.getSourceZ();
                                    ao += method_16572(as, at, au) * 0.4D;
                                }

                                objectListIterator2.back(objectList2.size());
                                BlockState blockState = this.getBlockState(ao, v);
                                if (blockState != AIR) {
                                    if (blockState.getLuminance() != 0) {
                                        mutable.set(ae, v, ak);
                                        protoChunk.addLightSource(mutable);
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

    private static double method_16572(int i, int j, int k) {
        int l = i + 12;
        int m = j + 12;
        int n = k + 12;
        if (l >= 0 && l < 24) {
            if (m >= 0 && m < 24) {
                return n >= 0 && n < 24 ? (double)field_16649[n * 24 * 24 + l * 24 + m] : 0.0D;
            } else {
                return 0.0D;
            }
        } else {
            return 0.0D;
        }
    }

    private static double method_16571(int i, int j, int k) {
        double d = (double)(i * i + k * k);
        double e = (double)j + 0.5D;
        double f = e * e;
        double g = Math.pow(2.718281828459045D, -(f / 16.0D + d / 16.0D));
        double h = -e * MathHelper.fastInverseSqrt(f / 2.0D + d / 2.0D) / 2.0D;
        return h * g;
    }

    public int getMaxY() {
        return this.field_24779;
    }

    public int getSeaLevel() {
        return this.chunkGeneratorType.getSeaLevel();
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

    public void populateEntities(ChunkRegion region) {
        if (!false) {
            int i = region.getCenterChunkX();
            int j = region.getCenterChunkZ();
            Biome biome = region.getBiome((new ChunkPos(i, j)).getStartPos());
            ChunkRandom chunkRandom = new ChunkRandom();
            chunkRandom.setPopulationSeed(region.getSeed(), i << 4, j << 4);
            SpawnHelper.populateEntities(region, biome, i, j, chunkRandom);
        }
    }



    static {
        AIR = Blocks.AIR.getDefaultState();
    }
}
