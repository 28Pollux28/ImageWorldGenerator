package eu.pollux28.imggen.data;

import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

public enum BiomeColors{
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

    private final int RGB;
    private final RegistryKey<Biome> biome;

    BiomeColors(int RGB, RegistryKey<Biome> biome){
        this.RGB =RGB;
        this.biome = biome;
    }
    public RegistryKey<Biome> getBiome()
    {
        return biome;
    }
    public int getRGB(){
        return RGB;
    }
}