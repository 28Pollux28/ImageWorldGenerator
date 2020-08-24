package eu.pollux28.genmap.gen.biomes;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import eu.pollux28.genmap.GenMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.source.BiomeSource;
import org.apache.logging.log4j.Level;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.stream.Stream;

public class GenMapBiomeSource extends BiomeSource {

    public static final Codec<GenMapBiomeSource> CODEC = Codec.LONG.fieldOf("seed").xmap(GenMapBiomeSource::new, (source) -> source.seed).stable().codec();

    /*private static final List<Biome> biomes = ImmutableList.of(Biomes.OCEAN, Biomes.PLAINS, Biomes.DESERT, Biomes.MOUNTAINS,
            Biomes.FOREST, Biomes.TAIGA, Biomes.SWAMP, Biomes.RIVER, Biomes.SNOWY_TUNDRA,
            Biomes.SNOWY_MOUNTAINS, Biomes.MUSHROOM_FIELDS, Biomes.MUSHROOM_FIELD_SHORE, Biomes.BEACH, Biomes.DESERT_HILLS, Biomes.WOODED_HILLS,
            Biomes.TAIGA_HILLS, Biomes.MOUNTAIN_EDGE, Biomes.JUNGLE, Biomes.JUNGLE_HILLS, Biomes.JUNGLE_EDGE, Biomes.DEEP_OCEAN,
            Biomes.STONE_SHORE, Biomes.SNOWY_BEACH, Biomes.BIRCH_FOREST, Biomes.BIRCH_FOREST_HILLS, Biomes.DARK_FOREST, Biomes.SNOWY_TAIGA,
            Biomes.SNOWY_TAIGA_HILLS, Biomes.GIANT_TREE_TAIGA, Biomes.GIANT_TREE_TAIGA_HILLS, Biomes.WOODED_MOUNTAINS, Biomes.SAVANNA,
            Biomes.SAVANNA_PLATEAU, Biomes.BADLANDS, Biomes.WOODED_BADLANDS_PLATEAU, Biomes.BADLANDS_PLATEAU, Biomes.WARM_OCEAN,
            Biomes.LUKEWARM_OCEAN, Biomes.COLD_OCEAN, Biomes.DEEP_WARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.DEEP_COLD_OCEAN,
            Biomes.DEEP_FROZEN_OCEAN, Biomes.SUNFLOWER_PLAINS, Biomes.DESERT_LAKES, Biomes.GRAVELLY_MOUNTAINS, Biomes.FLOWER_FOREST,
            Biomes.TAIGA_MOUNTAINS, Biomes.SWAMP_HILLS, Biomes.ICE_SPIKES, Biomes.MODIFIED_JUNGLE, Biomes.MODIFIED_JUNGLE_EDGE,
            Biomes.TALL_BIRCH_FOREST, Biomes.TALL_BIRCH_HILLS, Biomes.DARK_FOREST_HILLS, Biomes.SNOWY_TAIGA_MOUNTAINS,
            Biomes.GIANT_SPRUCE_TAIGA, Biomes.GIANT_SPRUCE_TAIGA_HILLS, Biomes.MODIFIED_GRAVELLY_MOUNTAINS,
            Biomes.SHATTERED_SAVANNA, Biomes.SHATTERED_SAVANNA_PLATEAU, Biomes.ERODED_BADLANDS,
            Biomes.MODIFIED_WOODED_BADLANDS_PLATEAU, Biomes.MODIFIED_BADLANDS_PLATEAU, Biomes.THE_VOID,Biomes.BASALT_DELTAS);*/
    private static final List<Biome>biomes = ImmutableList.copyOf(Biome.BIOMES);

    private final long seed;
    private final BufferedImage image;
    private boolean imgSet = false;
    private int sizeX, sizeZ;
    private final HashMap<Vec3i, Biome> BiomePosCache = new HashMap<>();
    private final Int2ObjectOpenHashMap<Biome> biomesRefColors = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<Biome> colorsForBiome = new Int2ObjectOpenHashMap<>();
    private final Biome defaultBiome = Biomes.OCEAN;
    private int scale;


    public GenMapBiomeSource(long seed) {
        super(biomes);
        this.seed=seed;
        this.image = setImage(GenMap.config.imageName);
        if(this.image!=null) {
            //EdoraMain.log(Level.FATAL, "Could not find image at "+" ! Generating a stub world.");
            this.imgSet = true;
            loadBiomes();
            generateCache();
        }
    }
    @Override
    public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
        return getBiomeFromCache(biomeX,biomeZ);
    }

    @Override
    protected Codec<? extends BiomeSource> method_28442() {
        return CODEC;
    }

    @Override
    public BiomeSource withSeed(long seed) {
        return new GenMapBiomeSource(seed);
    }

    private void loadBiomes(){
        Stream.of(BiomesC.values()).parallel().forEach(biomesC -> biomesRefColors.putIfAbsent(biomesC.getRGB(),biomesC.getBiome()));
        GenMap.config.aList.parallelStream().forEach(biomeIDAndRGBPair -> {
        Identifier bID = getIdFromString(biomeIDAndRGBPair.biomeID);
            if(bID!=null){
                Biome biome = getBiomebyID(bID);
                if(biome!=null){
                    if(!biomesRefColors.containsKey(biomeIDAndRGBPair.RGB)){
                        biomesRefColors.put(biomeIDAndRGBPair.RGB, biome);
                    }else GenMap.logger.log(Level.ERROR,"Biome with key "+Integer.toHexString(biomeIDAndRGBPair.RGB)+ "already exists !, Please choose a different Color Code.");
                }else{
                    if(!biomeIDAndRGBPair.biomeID.equals("modid:biomeid")){
                        GenMap.logger.log(Level.ERROR, "Couldn't find biome at "+biomeIDAndRGBPair.biomeID);
                    }
                }
            }else{
                GenMap.logger.log(Level.ERROR,"Incorrect biomeID format. Expected modid:biomeid, got "+ biomeIDAndRGBPair.biomeID);
            }
        });
    }


    private void generateCache() {
        if(image==null) return;
        for(int ix = 0; ix< sizeX *scale; ix++){
            for(int iz = 0; iz< sizeZ *scale; iz++){
                int RGB = image.getRGB(ix, iz)&0xFFFFFF;
                Vec3i vec = new Vec3i(ix-sizeX, 0, iz-sizeZ);

                if(!this.colorsForBiome.containsKey(RGB)){
                    Biome biome = biomesRefColors.int2ObjectEntrySet().parallelStream().min(Comparator.comparingDouble((bt1) -> getColorDiff(RGB, bt1.getIntKey()))).get().getValue();
                    this.colorsForBiome.put(RGB, biome);
                }
                this.BiomePosCache.put(vec,this.colorsForBiome.get(RGB));
            }
        }

        colorsForBiome.clear();
    }

    private static double getColorDiff(int RGB, int btRGB){
        return Math.pow(((RGB)%256)-((btRGB)%256), 2) + Math.pow(((RGB>>8)%256)-((btRGB>>8)%256), 2) + Math.pow(((RGB>>16)%256)-((btRGB>>16)%256), 2);

    }

    private static class BiomeCount {
        private final Biome b;
        public int count;
        public final Biome biome() { return b; }
        public BiomeCount(Biome b) {
            this.b = b;
            count = 0;
        }
        //public Boolean equals(final Biome b) { return b == this.b; }
    }
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public Biome getBiomeFromCache(int x, int z){
        if(!imgSet)
            return defaultBiome;

        //x & z are xBlock/4 & zBlock/4
        // => 1/16 of a chunk
        int xBase = x/scale;
        int zBase = z/scale;

        Biome currentBiome = BiomePosCache.getOrDefault(new Vec3i(xBase, 0, zBase), defaultBiome);
        Set<BiomeCount> biomesArround = new HashSet<>();

        for(int iz = -2; iz<= 2; iz++) {
            for(int ix = -2; ix<= 2; ix++) {
                Biome b;
                if(iz == 0 && ix == 0)
                    b = currentBiome;
                else
                    b = BiomePosCache.getOrDefault(new Vec3i((xBase+ix)/scale, 0, (zBase+iz)/scale), currentBiome);
                BiomeCount bc = new BiomeCount(b);
                if(!biomesArround.add(bc)) {
                    biomesArround.parallelStream().forEach(bci -> {
                        if (bci.biome()==b){
                            bci.count++;
                        }
                    });
                }
            }
        }
        return biomesArround.parallelStream().max(Comparator.comparingInt((bci) -> bci.count)).get().biome();
    }

    public BufferedImage setImage(String pathname){
        BufferedImage img = null;
        try {
            Path configDir = Paths.get("", "genmap", "image", pathname);
            //available: map_1.png, Edora_island.png
            img = ImageIO.read(configDir.toFile());

        } catch (IOException e) {
            e.getCause();
            GenMap.logger.log(Level.ERROR,"Couldn't find image at /genmap/image/"+pathname);
        }
        if (img!=null){
            scale = GenMap.config.scale;
            if(scale>0) {
                BufferedImage newImg = new BufferedImage((img.getWidth()) * scale, img.getHeight() * scale, BufferedImage.TRANSLUCENT);
                Graphics2D g2 = newImg.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);


                g2.drawImage(img, 0, 0, img.getWidth() * scale, img.getHeight() * scale, null);
                g2.dispose();
                sizeX = newImg.getWidth();
                sizeZ = newImg.getHeight();
                return newImg;
            }else GenMap.logger.log(Level.ERROR,"Scale must be > 0, got : "+scale);
        }
        return img;
    }
    public Biome getBiomebyID(Identifier biomeID){
        return Registry.BIOME.get(biomeID);
    }
    private Identifier getIdFromString(String biomeID) {
        String[] str = biomeID.toLowerCase().split(":");
        if (str.length!=2){
            return null;
        }else return new Identifier(str[0],str[1]);
    }
    /*public Identifier getBiomeId(Biome biome) {
        return Registry.BIOME.getId(biome);
    }*/
    public enum BiomesC{
        //default biomes
        Plains(0x82A84A, Biomes.PLAINS),
        Desert(0xD8C377, Biomes.DESERT),
        Savanna(0xAAAE55, Biomes.SAVANNA),
        Shattered_Savanna(0xB9A762,Biomes.SHATTERED_SAVANNA),
        Jungle(0x38821D,Biomes.JUNGLE),
        Jungle_Edge(0x3F7A24,Biomes.JUNGLE_EDGE),
        Swamp(0x4C9B59, Biomes.SWAMP),
        Sunflower_Plains(0x4F9F28,Biomes.SUNFLOWER_PLAINS),
        Forest(0x3A5223, Biomes.FOREST),
        Taiga(0x597C4A,Biomes.TAIGA),
        Snowy_Tundra(0xE5F4FF,Biomes.SNOWY_TUNDRA),
        Dark_Forest(0x2E7B40,Biomes.DARK_FOREST),
        //High Biomes
        Mountains(0xC0CDB7, Biomes.MOUNTAINS),
        Gravelly_Mountains(0x9fA8A5,Biomes.GRAVELLY_MOUNTAINS),
        Modified_Gravelly_Mountains(0xE5E5E5,Biomes.MODIFIED_GRAVELLY_MOUNTAINS),
        Badlands(0xC9934F,Biomes.BADLANDS),
        Wooded_Badlands_Plateau(0xA09D44,Biomes.WOODED_BADLANDS_PLATEAU),
        Eroded_Badlands(0x7F5D33,Biomes.ERODED_BADLANDS),
        //Weird
        Basalt_Deltas(0x28241B,Biomes.BASALT_DELTAS),
        //water related
        Beach(0xE5D495, Biomes.BEACH),
        River(0x4086BF,Biomes.RIVER),
        Ocean(0x3672C9, Biomes.OCEAN),
        Deep_Ocean(0x02598D,Biomes.OCEAN),
        Warm_Ocean(0x00B7E5,Biomes.WARM_OCEAN);

        /*Mountains(0xA0AAA6, Biomes.GRAVELLY_MOUNTAINS),
        Water_Source(0x034A8C, Biomes.RIVER),
        Dirty(0x675834, Biomes.TAIGA),
        Mesa(0x27241A, Biomes.BADLANDS_PLATEAU),
        Magic_Forest(0x02D7B3, Biomes.JUNGLE),
        Snowy(0xE6E6E6, Biomes.ICE_SPIKES),
        MountainEdge(0xBEC1B6,Biomes.MOUNTAIN_EDGE);*/
        int RGB;
        Biome biome;
        BiomesC(int RGB, Biome biome){
            this.RGB =RGB;
            this.biome = biome;
        }
        Biome getBiome(){ return biome; }
        int getRGB(){
            return RGB;
        }
    }

}
