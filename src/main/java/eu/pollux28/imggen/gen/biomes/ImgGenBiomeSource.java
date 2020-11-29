package eu.pollux28.imggen.gen.biomes;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pollux28.imggen.ImgGen;
import eu.pollux28.imggen.config.MainConfigData;
import eu.pollux28.imggen.util.BiomeIDAndRGBPair;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.RegistryLookupCodec;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
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

public class ImgGenBiomeSource extends BiomeSource {

    public static final Codec<ImgGenBiomeSource> CODEC = RecordCodecBuilder.create((instance) -> instance.group(Codec.LONG.fieldOf("seed").stable().forGetter((imgGenBiomeSource) -> imgGenBiomeSource.seed), RegistryLookupCodec.of(Registry.BIOME_KEY).forGetter((imgGenBiomeSource) -> imgGenBiomeSource.biomeRegistry)).apply(instance, instance.stable(ImgGenBiomeSource::new)));
    private static final List<RegistryKey<Biome>> BIOMES = ImmutableList.of(BiomeKeys.OCEAN, BiomeKeys.PLAINS, BiomeKeys.DESERT, BiomeKeys.MOUNTAINS,
            BiomeKeys.FOREST, BiomeKeys.TAIGA, BiomeKeys.SWAMP, BiomeKeys.RIVER, BiomeKeys.SNOWY_TUNDRA,
            BiomeKeys.SNOWY_MOUNTAINS, BiomeKeys.MUSHROOM_FIELDS, BiomeKeys.MUSHROOM_FIELD_SHORE, BiomeKeys.BEACH, BiomeKeys.DESERT_HILLS, BiomeKeys.WOODED_HILLS,
            BiomeKeys.TAIGA_HILLS, BiomeKeys.MOUNTAIN_EDGE, BiomeKeys.JUNGLE, BiomeKeys.JUNGLE_HILLS, BiomeKeys.JUNGLE_EDGE, BiomeKeys.DEEP_OCEAN,
            BiomeKeys.STONE_SHORE, BiomeKeys.SNOWY_BEACH, BiomeKeys.BIRCH_FOREST, BiomeKeys.BIRCH_FOREST_HILLS, BiomeKeys.DARK_FOREST, BiomeKeys.SNOWY_TAIGA,
            BiomeKeys.SNOWY_TAIGA_HILLS, BiomeKeys.GIANT_TREE_TAIGA, BiomeKeys.GIANT_TREE_TAIGA_HILLS, BiomeKeys.WOODED_MOUNTAINS, BiomeKeys.SAVANNA,
            BiomeKeys.SAVANNA_PLATEAU, BiomeKeys.BADLANDS, BiomeKeys.WOODED_BADLANDS_PLATEAU, BiomeKeys.BADLANDS_PLATEAU, BiomeKeys.WARM_OCEAN,
            BiomeKeys.LUKEWARM_OCEAN, BiomeKeys.COLD_OCEAN, BiomeKeys.DEEP_WARM_OCEAN, BiomeKeys.DEEP_LUKEWARM_OCEAN, BiomeKeys.DEEP_COLD_OCEAN,
            BiomeKeys.DEEP_FROZEN_OCEAN, BiomeKeys.SUNFLOWER_PLAINS, BiomeKeys.DESERT_LAKES, BiomeKeys.GRAVELLY_MOUNTAINS, BiomeKeys.FLOWER_FOREST,
            BiomeKeys.TAIGA_MOUNTAINS, BiomeKeys.SWAMP_HILLS, BiomeKeys.ICE_SPIKES, BiomeKeys.MODIFIED_JUNGLE, BiomeKeys.MODIFIED_JUNGLE_EDGE,
            BiomeKeys.TALL_BIRCH_FOREST, BiomeKeys.TALL_BIRCH_HILLS, BiomeKeys.DARK_FOREST_HILLS, BiomeKeys.SNOWY_TAIGA_MOUNTAINS,
            BiomeKeys.GIANT_SPRUCE_TAIGA, BiomeKeys.GIANT_SPRUCE_TAIGA_HILLS, BiomeKeys.MODIFIED_GRAVELLY_MOUNTAINS,
            BiomeKeys.SHATTERED_SAVANNA, BiomeKeys.SHATTERED_SAVANNA_PLATEAU, BiomeKeys.ERODED_BADLANDS,
            BiomeKeys.MODIFIED_WOODED_BADLANDS_PLATEAU, BiomeKeys.MODIFIED_BADLANDS_PLATEAU, BiomeKeys.THE_VOID,BiomeKeys.BASALT_DELTAS);

    private final long seed;
    private final BufferedImage image;
    private boolean imgSet = false;
    private int sizeX, sizeZ;
    private final HashMap<Vec3i, Biome> BiomePosCache = new HashMap<>();
    private final Int2ObjectOpenHashMap<Biome> biomesRefColors = new Int2ObjectOpenHashMap<>();

    private final Int2ObjectOpenHashMap<Biome> colorsForBiome = new Int2ObjectOpenHashMap<>();
    private Biome defaultBiome;
    private double scale;
    public MainConfigData config;
    private final Registry<Biome> biomeRegistry;


    public ImgGenBiomeSource(long seed, Registry<Biome> biomeRegistry) {
        super(BIOMES.stream().map((registryKey) -> () -> (Biome)biomeRegistry.getOrThrow(registryKey)));
        ImgGen.refreshConfig();
        config = ImgGen.CONFIG;
        this.seed=seed;
        this.biomeRegistry=biomeRegistry;
        this.image = setImage(config.imageName);
        getDefaultBiome();
        if(this.image!=null) {
            this.imgSet = true;
            sizeX=image.getWidth();
            sizeZ=image.getHeight();
            loadBiomes();
            generateCache();
        }
    }
    @Override
    public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
        return getBiomeFromCache(biomeX,biomeZ);
    }

    @Override
    protected Codec<? extends BiomeSource> getCodec() {
        return CODEC;
    }

    @Override
    public BiomeSource withSeed(long seed) {
        return new ImgGenBiomeSource(seed,this.biomeRegistry);
    }

    private void getDefaultBiome(){
        Identifier bID = getIdFromString(ImgGen.CONFIG.defaultBiome);
        if(bID!=null){
            Biome biome = getBiomeByID(bID);
            if (biome !=null){
                defaultBiome= biome;
            }else{
                defaultBiome= biomeRegistry.get(BiomeKeys.OCEAN);
            }
        }else{
            defaultBiome= biomeRegistry.get(BiomeKeys.OCEAN);
        }
    }

    private void loadBiomes(){
        for(int i =0;i<BiomesC.values().length;i++){
            BiomesC biomesC = BiomesC.values()[i];
            biomesRefColors.putIfAbsent(biomesC.getRGB(),biomeRegistry.get(biomesC.getBiome()));
        }
        for (BiomeIDAndRGBPair biomeIDAndRGBPair :ImgGen.CONFIG.customBiomes){
            int RGB;
            try {
                RGB = Integer.decode(biomeIDAndRGBPair.RGB);
            }catch (NumberFormatException e){
                RGB = -1;
            }
            if (RGB==-1){
                ImgGen.logger.log(Level.ERROR,"Biome "+biomeIDAndRGBPair.biomeID+" has incorrect color code. Must be in the form of : " +
                        "0xRRGGBB using hexadecimal code.");
                return;
            }
            Identifier bID = getIdFromString(biomeIDAndRGBPair.biomeID);
            if(bID!=null){
                Biome biome = getBiomeByID(bID);
                if(biome!=null){
                    int finalRGB = RGB;
                    Biome b2 =biomesRefColors.merge(RGB,biome,(v1, v2) ->{
                        ImgGen.logger.log(Level.ERROR,"Color code with key "+Integer.toHexString(finalRGB)+
                                " already exists !, Please choose a different Color Code for biome "+biomeIDAndRGBPair.biomeID);
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
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public Biome getBiomeFromCache(int x, int z){
        if(!imgSet)
            return defaultBiome;
        int xBase;
        int zBase;
        if(scale==2 || scale==4){
            xBase = x <<(int)(2-scale/2);
            zBase = z <<(int)(2-scale/2);
        }else if((1/scale)%2==0){
            xBase = x<<(2+(int)((1/scale)/2));
            zBase = z<<(2+(int)((1/scale)/2));
        }else{
            xBase=x<<2;
            zBase=z<<2;
        }

        Biome currentBiome = BiomePosCache.getOrDefault(new Vec3i(xBase, 0, zBase), defaultBiome);
        Set<BiomeCount> biomesAround = new HashSet<>();

        for(int iz = -2; iz<= 2; iz++) {
            for(int ix = -2; ix<= 2; ix++) {
                Biome b;
                if(iz == 0 && ix == 0)
                    b = currentBiome;
                else
                    b = BiomePosCache.getOrDefault(new Vec3i((xBase+ix), 0, (zBase+iz)), currentBiome);
                BiomeCount bc = new BiomeCount(b);
                if(!biomesAround.add(bc)) {
                    biomesAround.parallelStream().forEach(bci -> {
                        if (bci.biome()==b){
                            bci.count++;
                        }
                    });
                }
            }
        }
        return biomesAround.parallelStream().max(Comparator.comparingInt((bci) -> bci.count)).get().biome();
    }

    public BufferedImage setImage(String pathname){
        BufferedImage img = null;
        try {
            Path configDir = Paths.get("", "imggen", "image", pathname);
            img = ImageIO.read(configDir.toFile());

        } catch (IOException e) {
            e.getCause();
            ImgGen.logger.log(Level.ERROR,"Couldn't find image at /imggen/image/"+pathname);
        }
        if (img!=null){
            scale = ImgGen.CONFIG.scale;
            if(scale>0 && scale !=1 && scale!=2 && scale !=4 && (1/scale)%2!=0) {
                BufferedImage newImg = new BufferedImage((int)Math.ceil((img.getWidth()) * scale), (int)Math.ceil(img.getHeight() * scale), BufferedImage.TRANSLUCENT);
                Graphics2D g2 = newImg.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);


                g2.drawImage(img, 0, 0, (int)Math.ceil(img.getWidth() * scale), (int)Math.ceil(img.getHeight() * scale), null);
                g2.dispose();
                return newImg;
            }else if (scale ==1 || scale==2 || scale ==4 || (1/scale)%2==0){
                return img;
            }else ImgGen.logger.log(Level.ERROR,"Scale must be > 0, got : "+scale);
        }
        return img;
    }
    public Biome getBiomeByID(Identifier biomeID){
        return biomeRegistry.get(biomeID);
    }
    private Identifier getIdFromString(String biomeID) {
        String[] str = biomeID.toLowerCase().split(":");
        if (str.length!=2){
            return null;
        }else return new Identifier(str[0],str[1]);
    }
    public enum BiomesC{
        Ocean(0x000070, BiomeKeys.OCEAN),
        Plains(0x8DB360, BiomeKeys.PLAINS),
        Desert(0xFA9418, BiomeKeys.DESERT),
        Mountains(0x606060, BiomeKeys.MOUNTAINS),
        Forest(0x056621, BiomeKeys.FOREST),
        Taiga(0x0B6659,BiomeKeys.TAIGA),
        Swamp(0x07F9B2, BiomeKeys.SWAMP),
        River(0x0000FF,BiomeKeys.RIVER),
        Nether_Wastes(0xbf3b3b,BiomeKeys.NETHER_WASTES),
        The_End(0x8080FF,BiomeKeys.THE_END),
        Frozen_Ocean(0x7070D6,BiomeKeys.FROZEN_OCEAN),
        Frozen_River(0xA0A0FF,BiomeKeys.FROZEN_RIVER),
        Snowy_Tundra(0xFFFFFF,BiomeKeys.SNOWY_TUNDRA),
        Snowy_Mountains(0xA0A0A0, BiomeKeys.SNOWY_MOUNTAINS),
        Mushroom_Fields(0xFF00FF,BiomeKeys.MUSHROOM_FIELDS),
        Mushroom_Field_Shore(0xA000FF,BiomeKeys.MUSHROOM_FIELD_SHORE),
        Beach(0xFADE55, BiomeKeys.BEACH),
        Desert_Hills(0xD25F12,BiomeKeys.DESERT_HILLS),
        Wooded_Hills(0x22551C,BiomeKeys.WOODED_HILLS),
        Taiga_Hills(0x163933,BiomeKeys.TAIGA_HILLS),
        Mountain_Edge(0x72789A,BiomeKeys.MOUNTAIN_EDGE),
        Jungle(0x537B09,BiomeKeys.JUNGLE),
        Jungle_Hills(0x2C4205,BiomeKeys.JUNGLE_HILLS),
        Jungle_Edge(0x628B17,BiomeKeys.JUNGLE_EDGE),
        Deep_Ocean(0x000030,BiomeKeys.DEEP_OCEAN),
        Stone_Shore(0xA2A284,BiomeKeys.STONE_SHORE),
        Snowy_Beach(0xFAF0C0,BiomeKeys.SNOWY_BEACH),
        Birch_Forest(0x307444,BiomeKeys.BIRCH_FOREST),
        Birch_Forest_Hills(0x1F5F32,BiomeKeys.BIRCH_FOREST_HILLS),
        Dark_Forest(0x40511A,BiomeKeys.DARK_FOREST),
        Snowy_Taiga(0x31554A,BiomeKeys.SNOWY_TAIGA),
        Snowy_Taiga_Hills(0x243F36,BiomeKeys.SNOWY_TAIGA_HILLS),
        Giant_Tree_Taiga(0x596651,BiomeKeys.GIANT_TREE_TAIGA),
        Giant_Tree_Taiga_Hills(0x454F3E,BiomeKeys.GIANT_TREE_TAIGA_HILLS),
        Wooded_Mountains(0x507050,BiomeKeys.WOODED_MOUNTAINS),
        Savanna(0xBDB25F, BiomeKeys.SAVANNA),
        Savanna_Plateau(0xA79D64, BiomeKeys.SAVANNA_PLATEAU),
        Badlands(0xD94515,BiomeKeys.BADLANDS),
        Wooded_Badlands_Plateau(0xb09765,BiomeKeys.WOODED_BADLANDS_PLATEAU),
        Badlands_Plateau(0xCA8C65,BiomeKeys.BADLANDS_PLATEAU),
        Small_End_Island(0x8080FF,BiomeKeys.SMALL_END_ISLANDS),
        End_Midlands(0x8080FF,BiomeKeys.END_MIDLANDS),
        End_HighLands(0x8080FF,BiomeKeys.END_HIGHLANDS),
        End_Barrens(0x8080FF,BiomeKeys.END_BARRENS),
        Warm_Ocean(0x0000AC,BiomeKeys.WARM_OCEAN),
        Lukewarm_Ocean(0x000090,BiomeKeys.LUKEWARM_OCEAN),
        Cold_Ocean(0x202070,BiomeKeys.COLD_OCEAN),
        Deep_Warm_Ocean(0x000050,BiomeKeys.DEEP_WARM_OCEAN),
        Deep_Lukewarm_Ocean(0x000040,BiomeKeys.DEEP_LUKEWARM_OCEAN),
        Deep_Cold_Ocean(0x202038,BiomeKeys.DEEP_COLD_OCEAN),
        Deep_Frozen_Ocean(0x404090, BiomeKeys.DEEP_FROZEN_OCEAN),
        The_Void(0x000000,BiomeKeys.THE_END),
        Sunflower_Plains(0xB5DB88,BiomeKeys.SUNFLOWER_PLAINS),
        Desert_Lakes(0xFFBC40,BiomeKeys.DESERT_LAKES),
        Gravelly_Mountains(0x888888,BiomeKeys.GRAVELLY_MOUNTAINS),
        Flower_Forest(0x2D8E49,BiomeKeys.FLOWER_FOREST),
        Taiga_Mountains(0x338E81,BiomeKeys.TAIGA_MOUNTAINS),
        Swamp_Hills(0x2FFFDA,BiomeKeys.SWAMP_HILLS),
        Ice_Spikes(0xB4DCDC, BiomeKeys.ICE_SPIKES),
        Modified_Jungle(0x7Ba331,BiomeKeys.MODIFIED_JUNGLE),
        Modified_Jungle_Edge(0x8AB33F,BiomeKeys.MODIFIED_JUNGLE_EDGE),
        Tall_Birch_Forest(0x589C6C,BiomeKeys.TALL_BIRCH_FOREST),
        Tall_Birch_Hills(0x47875A,BiomeKeys.TALL_BIRCH_HILLS),
        Dark_Forest_Hills(0x687942,BiomeKeys.DARK_FOREST_HILLS),
        Snowy_Taiga_Mountains(0x597D72,BiomeKeys.SNOWY_TAIGA_MOUNTAINS),
        Giant_Spruce_Taiga(0x818E79,BiomeKeys.GIANT_TREE_TAIGA),
        Giant_Spruce_Taiga_Hills(0x6D7766,BiomeKeys.GIANT_TREE_TAIGA_HILLS),
        Modified_Gravelly_Mountains(0x789878,BiomeKeys.MODIFIED_GRAVELLY_MOUNTAINS),
        Shattered_Savanna(0xE5DA87,BiomeKeys.SHATTERED_SAVANNA),
        Shattered_Savanna_Plateau(0xCFC58C,BiomeKeys.SHATTERED_SAVANNA_PLATEAU),
        Eroded_Badlands(0xFF6D3D,BiomeKeys.ERODED_BADLANDS),
        Modified_Wooded_Badlands_Plateau(0xD8BF8D,BiomeKeys.MODIFIED_WOODED_BADLANDS_PLATEAU),
        Modified_Badlands_Plateau(0xF2B48D,BiomeKeys.MODIFIED_BADLANDS_PLATEAU),
        Bamboo_Jungle(0x768E14,BiomeKeys.BAMBOO_JUNGLE),
        Bamboo_Jungle_Hills(0x3B470A,BiomeKeys.BAMBOO_JUNGLE_HILLS),
        Soul_Sand_Valley(0x5e3830,BiomeKeys.SOUL_SAND_VALLEY),
        Crimson_Forest(0xDD0808,BiomeKeys.CRIMSON_FOREST),
        Warped_Forest(0x49907B,BiomeKeys.WARPED_FOREST),
        Basalt_Deltas(0x403636,BiomeKeys.BASALT_DELTAS);
        int RGB;
        RegistryKey<Biome> biome;
        BiomesC(int RGB, RegistryKey<Biome> biome){
            this.RGB =RGB;
            this.biome = biome;
        }
        RegistryKey<Biome> getBiome(){ return biome; }
        int getRGB(){
            return RGB;
        }
    }

}
