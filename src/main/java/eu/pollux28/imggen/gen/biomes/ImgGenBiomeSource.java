package eu.pollux28.imggen.gen.biomes;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pollux28.imggen.ImgGen;
import eu.pollux28.imggen.config.MainConfigData;
import eu.pollux28.imggen.data.BiomeColorConverter;
import eu.pollux28.imggen.data.BiomeColors;
import eu.pollux28.imggen.data.ImageDataProvider;
import eu.pollux28.imggen.util.BiomeIDAndRGBPair;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.RegistryLookupCodec;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeSource;
import org.apache.logging.log4j.Level;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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

    private long seed;
    private final Registry<Biome> biomeRegistry;

    private final BiomeColorConverter biomeColorConverter;
    private final ImageDataProvider<Biome> biomeDataProvider;

    public ImgGenBiomeSource(long seed, Registry<Biome> biomeRegistry) {
        super(BIOMES.stream().map((registryKey) -> () -> (Biome) biomeRegistry.getOrThrow(registryKey)));

        this.seed = seed;
        this.biomeRegistry = biomeRegistry;

        ImgGen.refreshConfig();
        MainConfigData config = ImgGen.CONFIG;

        boolean isClient = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;

        if (ImgGen.biomeColorConverter == null || isClient) {
            Biome defaultBiome = getDefaultBiome();
            ImgGen.biomeColorConverter = new BiomeColorConverter(defaultBiome);
            biomeColorConverter = ImgGen.biomeColorConverter;
            registerBiomes();
        } else {
            biomeColorConverter = ImgGen.biomeColorConverter;
        }
        if (ImgGen.biomeDataProvider == null || isClient) {
            BufferedImage image = loadImage(config.imageName);
            ImgGen.biomeDataProvider = new ImageDataProvider<>(ImgGen.biomeColorConverter, image, config.scale);
            ;
        }


        biomeDataProvider = ImgGen.biomeDataProvider;
    }

    private static BufferedImage loadImage(String pathname) {
        BufferedImage img = null;
        try {
            Path configDir = Paths.get("", "imggen", "image", pathname);
            img = ImageIO.read(configDir.toFile());

        } catch (IOException e) {
            e.getCause();
            ImgGen.logger.log(Level.ERROR, "Couldn't find image at /imggen/image/" + pathname);
        }

        return img;
    }

    @Override
    protected Codec<? extends BiomeSource> getCodec() {
        return CODEC;
    }

    @Override
    public BiomeSource withSeed(long seed) {
        this.seed = seed;
        return this;
    }

    private Biome getDefaultBiome(){
        Identifier bID = getIdFromString(ImgGen.CONFIG.defaultBiome);
        if(bID == null) {
            return biomeRegistry.get(BiomeKeys.OCEAN);
        }

        Biome biome = biomeRegistry.get(bID);
        return biome != null
                ? biome
                : biomeRegistry.get(BiomeKeys.OCEAN);
    }

    private void registerBiomes() {
        for (BiomeColors biomeColor : BiomeColors.values()) {
            RegistryKey<Biome> biomeKey = biomeColor.getBiome();
            Biome biome = biomeRegistry.get(biomeKey);
            biomeColorConverter.RegisterBiome(biomeColor.getRGB(), biome);
            ImgGen.logger.log(Level.DEBUG, "Registered Biome " + biomeKey.getValue().toString() + " with a color code of " + Integer.toHexString(biomeColor.getRGB()));
        }

        for (BiomeIDAndRGBPair biomeIDAndRGBPair : ImgGen.CONFIG.customBiomes) {
            int RGB;
            try {
                RGB = Integer.decode(biomeIDAndRGBPair.RGB);
            } catch (NumberFormatException e) {
                RGB = -1;
            }
            if (RGB == -1) {
                ImgGen.logger.log(Level.ERROR, "Biome " + biomeIDAndRGBPair.biomeID + " has incorrect color code. Must be in the form of : " +
                        "0xRRGGBB using hexadecimal code.");
                continue;
            }

            Identifier bID = getIdFromString(biomeIDAndRGBPair.biomeID);
            if (bID == null) {
                ImgGen.logger.log(Level.ERROR, "Incorrect biomeID format. Expected modid:biomeid, got " + biomeIDAndRGBPair.biomeID);
                continue;
            }

            Biome biome = biomeRegistry.get(bID);
            if (biome == null) {
                if (!biomeIDAndRGBPair.biomeID.equals("modid:biomeid")) {
                    ImgGen.logger.log(Level.ERROR, "Couldn't find biome at " + biomeIDAndRGBPair.biomeID);
                }
                continue;
            }

            biomeColorConverter.RegisterBiome(RGB, biome);
            ImgGen.logger.log(Level.DEBUG, "Registered Biome " + bID.toString() + " with a color code of " + Integer.toHexString(RGB));
        }
    }

    @Override
    public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
        return biomeDataProvider.GetData(biomeX, biomeZ);
    }

    private Identifier getIdFromString(String biomeID) {
        String[] str = biomeID.toLowerCase().split(":");
        if (str.length != 2) {
            return null;
        } else return new Identifier(str[0], str[1]);
    }
}
