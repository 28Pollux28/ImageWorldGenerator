package eu.pollux28.imggen.gen.biomes;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import eu.pollux28.imggen.ImgGen;
import eu.pollux28.imggen.config.ImgGenConfig;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenCustomHashMap;
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

public class ImgGenBiomeSource extends BiomeSource {

    public static final Codec<ImgGenBiomeSource> CODEC = Codec.LONG.fieldOf("seed").xmap(ImgGenBiomeSource::new, (source) -> source.seed).stable().codec();

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


    public ImgGenBiomeSource(long seed) {
        super(biomes);
        this.seed=seed;
        this.image = setImage(ImgGen.config.imageName);
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
        return new ImgGenBiomeSource(seed);
    }

    private void loadBiomes(){
        Stream.of(BiomesC.values()).forEach(biomesC -> {

        });
        for(int i =0;i<BiomesC.values().length;i++){
            BiomesC biomesC = BiomesC.values()[i];
            biomesRefColors.putIfAbsent(biomesC.getRGB(),biomesC.getBiome());
            ImgGen.logger.log(Level.INFO,"register");
        }
        for (int i =0;i<ImgGen.config.aList.size();i++){
            ImgGenConfig.BiomeIDAndRGBPair biomeIDAndRGBPair = ImgGen.config.aList.get(i);
            Identifier bID = getIdFromString(biomeIDAndRGBPair.biomeID);
            if (biomeIDAndRGBPair.RGB==-1){
                ImgGen.logger.log(Level.ERROR,"Biome "+biomeIDAndRGBPair.biomeID+" has incorrect color code. Must be in the form of : " +
                        "0xRRGGBB using hexadecimal code.");
                return;
            }
            if(bID!=null){
                Biome biome = getBiomebyID(bID);
                if(biome!=null){
                    Biome b2 =biomesRefColors.merge(biomeIDAndRGBPair.RGB,biome,(v1, v2) ->{
                        ImgGen.logger.log(Level.ERROR,"Color code with key "+Integer.toHexString(biomeIDAndRGBPair.RGB)+
                                "already exists !, Please choose a different Color Code for biome "+biomeIDAndRGBPair.biomeID);
                        return v1;
                    });
                    if (b2 == biome){
                        ImgGen.logger.log(Level.INFO,"Biome "+biomeIDAndRGBPair.biomeID + " registered with color code: "+biomeIDAndRGBPair.RGB);
                    }
                }else{
                    if(!biomeIDAndRGBPair.biomeID.equals("modid:biomeid")){
                        ImgGen.logger.log(Level.ERROR, "Couldn't find biome at "+biomeIDAndRGBPair.biomeID);
                    }
                }
            }else{
                ImgGen.logger.log(Level.ERROR,"Incorrect biomeID format. Expected modid:biomeid, got "+ biomeIDAndRGBPair.biomeID);
            }
        }


        /*ImgGen.config.aList.forEach(biomeIDAndRGBPair -> {
            Identifier bID = getIdFromString(biomeIDAndRGBPair.biomeID);
            if (biomeIDAndRGBPair.RGB==-1){
                ImgGen.logger.log(Level.ERROR,"Biome "+biomeIDAndRGBPair.biomeID+" has incorrect color code. Must be in the form of : " +
                        "0xRRGGBB using hexadecimal code.");
                return;
            }
            if(bID!=null){
                Biome biome = getBiomebyID(bID);
                if(biome!=null){
                    Biome b2 =biomesRefColors.merge(biomeIDAndRGBPair.RGB,biome,(v1, v2) ->{
                        ImgGen.logger.log(Level.ERROR,"Color code with key "+Integer.toHexString(biomeIDAndRGBPair.RGB)+
                                "already exists !, Please choose a different Color Code for biome "+biomeIDAndRGBPair.biomeID);
                        return v1;
                    });
                    if (b2 == biome){
                        ImgGen.logger.log(Level.INFO,"Biome "+biomeIDAndRGBPair.biomeID + " registered with color code: "+biomeIDAndRGBPair.RGB);
                    }
                }else{
                    if(!biomeIDAndRGBPair.biomeID.equals("modid:biomeid")){
                        ImgGen.logger.log(Level.ERROR, "Couldn't find biome at "+biomeIDAndRGBPair.biomeID);
                    }
                }
            }else{
                ImgGen.logger.log(Level.ERROR,"Incorrect biomeID format. Expected modid:biomeid, got "+ biomeIDAndRGBPair.biomeID);
            }
        });*/
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void generateCache() {
        if(image==null) return;
        for(int ix = 0; ix< sizeX; ix++){
            for(int iz = 0; iz< sizeZ; iz++){
                int RGB = image.getRGB(ix, iz)&0xFFFFFF;
                Vec3i vec = new Vec3i(ix-sizeX/2, 0, iz-sizeZ/2);

                if(!this.colorsForBiome.containsKey(RGB)){
                    Biome biome = biomesRefColors.int2ObjectEntrySet().parallelStream().min(Comparator.comparingDouble(
                            (bt1) -> getColorDiff(RGB, bt1.getIntKey()))).get().getValue();
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
    /*public Biome getBiomeFromCache(int x, int z){
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
    }*/
    public Biome getBiomeFromCache(int x, int z){
        if(!imgSet)
            return defaultBiome;

        //x & z are xBlock/4 & zBlock/4
        // => 1/16 of a chunk
        int scale = 2;
        int xBase = x/scale;
        int zBase = z/scale;

        Biome currentBiome = BiomePosCache.getOrDefault(new Vec3i(xBase, 0, zBase), defaultBiome);
        Set<BiomeCount> biomesArround = new HashSet<BiomeCount>();

        for(int iz = -2; iz<= 2; iz++) {
            for(int ix = -2; ix<= 2; ix++) {
                Biome b;
                if(iz == 0 && ix == 0)
                    b = currentBiome;
                else
                    b = BiomePosCache.getOrDefault(new Vec3i(xBase+ix/scale, 0, zBase+iz/scale), currentBiome);
                BiomeCount bc = new BiomeCount(b);
                if(!biomesArround.contains(bc)) {
                    biomesArround.add(bc);
                }
                else {
                    for(BiomeCount bci : biomesArround) {
                        if(bci.biome() == b) {
                            bci.count++;
                            break;
                        }
                    }
                }
            }
        }
        int bestBiomeCount = 0;
        for(BiomeCount b : biomesArround) {
            if(bestBiomeCount < b.count) {
                bestBiomeCount = b.count;
                currentBiome = b.biome();
            }
        }

        return currentBiome;
    }

    public BufferedImage setImage(String pathname){
        BufferedImage img = null;
        try {
            Path configDir = Paths.get("", "imggen", "image", pathname);
            //available: map_1.png, Edora_island.png
            img = ImageIO.read(configDir.toFile());

        } catch (IOException e) {
            e.getCause();
            ImgGen.logger.log(Level.ERROR,"Couldn't find image at /imggen/image/"+pathname);
        }
        if (img!=null){
            scale = ImgGen.config.scale;
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
            }else ImgGen.logger.log(Level.ERROR,"Scale must be > 0, got : "+scale);
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
        Ocean(0x000070, Biomes.OCEAN),
        Plains(0x8DB360, Biomes.PLAINS),
        Desert(0xFA947C, Biomes.DESERT),
        Mountains(0x606060, Biomes.MOUNTAINS),
        Forest(0x056621, Biomes.FOREST),
        Taiga(0x0B0259,Biomes.TAIGA),
        Swamp(0x07F9B3, Biomes.SWAMP),
        River(0x0000FF,Biomes.RIVER),
        Nether_Wastes(0xFF0000,Biomes.NETHER_WASTES),
        The_End(0x8080FF,Biomes.THE_END),
        Frozen_Ocean(0x7070D6,Biomes.FROZEN_OCEAN),
        Frozen_River(0xA0A0FF,Biomes.FROZEN_RIVER),
        Snowy_Tundra(0xFFFFFF,Biomes.SNOWY_TUNDRA),
        Snowy_Mountains(0xA0A0A0, Biomes.SNOWY_MOUNTAINS),
        Mushroom_Fields(0xFF00FF,Biomes.MUSHROOM_FIELDS),
        Mushroom_Field_Shore(0xA000FF,Biomes.MUSHROOM_FIELD_SHORE),
        Beach(0xFADE55, Biomes.BEACH),
        Desert_Hills(0xD25F12,Biomes.DESERT_HILLS),
        Wooded_Hills(0x22551C,Biomes.WOODED_HILLS),
        Taiga_Hills(0x163933,Biomes.TAIGA_HILLS),
        Mountain_Edge(0x72789A,Biomes.MOUNTAIN_EDGE),
        Jungle(0x537B09,Biomes.JUNGLE),
        Jungle_Hills(0x2C4205,Biomes.JUNGLE_HILLS),
        Jungle_Edge(0x628817,Biomes.JUNGLE_EDGE),
        Deep_Ocean(0x000030,Biomes.OCEAN),
        Stone_Shore(0xA2A284,Biomes.STONE_SHORE),
        Snowy_Beach(0xFAF0C0,Biomes.SNOWY_BEACH),
        Birch_Forest(0x307444,Biomes.BIRCH_FOREST),
        Birch_Forest_Hills(0x1F0532,Biomes.BIRCH_FOREST_HILLS),
        Dark_Forest(0x40511A,Biomes.DARK_FOREST),
        Snowy_Taiga(0x31554A,Biomes.SNOWY_TAIGA),
        Snowy_Taiga_Hills(0x243F36,Biomes.SNOWY_TAIGA_HILLS),
        Giant_Tree_Taiga(0x596651,Biomes.GIANT_TREE_TAIGA),
        Giant_Tree_Taiga_Hills(0x45073E,Biomes.GIANT_TREE_TAIGA_HILLS),
        Wooded_Mountains(0x507050,Biomes.WOODED_MOUNTAINS),
        Savanna(0xBD125F, Biomes.SAVANNA),
        Savanna_Plateau(0xA79D64, Biomes.SAVANNA_PLATEAU),
        Badlands(0xD94515,Biomes.BADLANDS),
        Wooded_Badlands_Plateau(0x119765,Biomes.WOODED_BADLANDS_PLATEAU),
        Badlands_Plateau(0xCA8C65,Biomes.BADLANDS_PLATEAU),
        Small_End_Island(0x8080FF,Biomes.SMALL_END_ISLANDS),
        End_Midlands(0x8080FF,Biomes.END_MIDLANDS),
        End_HighLands(0x8080FF,Biomes.END_HIGHLANDS),
        End_Barrens(0x8080FF,Biomes.END_BARRENS),
        Warm_Ocean(0x0000AC,Biomes.WARM_OCEAN),
        Lukewarm_Ocean(0x000090,Biomes.LUKEWARM_OCEAN),
        Cold_Ocean(0x202070,Biomes.COLD_OCEAN),
        Deep_Warm_Ocean(0x000050,Biomes.DEEP_WARM_OCEAN),
        Deep_Lukewarm_Ocean(0x000040,Biomes.DEEP_LUKEWARM_OCEAN),
        Deep_Cold_Ocean(0x202038,Biomes.DEEP_COLD_OCEAN),
        Deep_Frozen_Ocean(0x404090, Biomes.DEEP_FROZEN_OCEAN),
        The_Void(0x000000,Biomes.THE_END),
        Sunflower_Plains(0xB5D888,Biomes.SUNFLOWER_PLAINS),
        Desert_Lakes(0xFFBC40,Biomes.DESERT_LAKES),
        Gravelly_Mountains(0x888888,Biomes.GRAVELLY_MOUNTAINS),
        Flower_Forest(0x2D8E49,Biomes.FLOWER_FOREST),
        Taiga_Mountains(0x338E13,Biomes.TAIGA_MOUNTAINS),
        Swamp_Hills(0x2FFF12,Biomes.SWAMP_HILLS),
        Ice_Spikes(0xB414DC, Biomes.ICE_SPIKES),
        Modified_Jungle(0x7B0D31,Biomes.MODIFIED_JUNGLE),
        Modified_Jungle_Edge(0x8AB33F,Biomes.MODIFIED_JUNGLE_EDGE),
        Tall_Birch_Forest(0x589C6C,Biomes.TALL_BIRCH_FOREST),
        Tall_Birch_Hills(0x470F5A,Biomes.TALL_BIRCH_HILLS),
        Dark_Forest_Hills(0x68794,Biomes.DARK_FOREST_HILLS),
        Snowy_Taiga_Mountains(0x597D72,Biomes.SNOWY_TAIGA_MOUNTAINS),
        Giant_Spruce_Taiga(0x818E79,Biomes.GIANT_TREE_TAIGA),
        Giant_Spruce_Taiga_Hills(0x6D7766,Biomes.GIANT_TREE_TAIGA_HILLS),
        Modified_Gravelly_Mountains(0x783478,Biomes.MODIFIED_GRAVELLY_MOUNTAINS),
        Shattered_Savanna(0xE5DA87,Biomes.SHATTERED_SAVANNA),
        Shattered_Savanna_Plateau(0xCFC58C,Biomes.SHATTERED_SAVANNA_PLATEAU),
        Eroded_Badlands(0xFF6D3D,Biomes.ERODED_BADLANDS),
        Modified_Wooded_Badlands_Plateau(0xD8BF8D,Biomes.MODIFIED_WOODED_BADLANDS_PLATEAU),
        Modified_Badlands_Plateau(0xF2B48D,Biomes.MODIFIED_BADLANDS_PLATEAU),
        Bamboo_Jungle(0x768E14,Biomes.BAMBOO_JUNGLE),
        Bamboo_Jungle_Hills(0x3B470A,Biomes.BAMBOO_JUNGLE_HILLS),
        Soul_Sand_Valley(0x522921,Biomes.SOUL_SAND_VALLEY),
        Crimson_Forest(0xDD0808,Biomes.CRIMSON_FOREST),
        Warped_Forest(0x49907B,Biomes.WARPED_FOREST),
        Basalt_Deltas(0x403636,Biomes.BASALT_DELTAS);
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
