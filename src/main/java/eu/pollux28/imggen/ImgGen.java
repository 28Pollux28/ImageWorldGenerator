package eu.pollux28.imggen;

import eu.pollux28.imggen.command.Commands;
import eu.pollux28.imggen.config.Config;
import eu.pollux28.imggen.config.ConfigUtil;
import eu.pollux28.imggen.config.MainConfigData;
import eu.pollux28.imggen.data.*;
import eu.pollux28.imggen.gen.ImgGenType;
import eu.pollux28.imggen.gen.biomes.ImgGenBiomeSource;
import eu.pollux28.imggen.gen.chunk.ImgGenChunkGenerator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ImgGen implements ModInitializer {
    public static final String VERSION = "1.1.3-beta.1";
    public static final Logger logger = LogManager.getLogger();
    public static MainConfigData CONFIG;
    public static BiomeColorConverter biomeColorConverter;
    public static BiomeDataProvider biomeDataProvider;
    public static HeightMapColorConverter heightMapColorConverter;
    public static HeightDataProvider heightMapDataProvider;
    public static StructureColorConverter structureColorConverter;
    public static StructureDataProvider structureDataProvider;

    private ImgGenType imgGenType;


    @Override
    public void onInitialize() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            imgGenType = new ImgGenType("imggen");
        }
        Commands.init();
        Path genMapDir = Paths.get("", "imggen", "image");
        if (!Files.isDirectory(genMapDir)) {
            boolean dir = genMapDir.toFile().mkdirs();
            if(!dir){
                logger.log(Level.FATAL,"Could not create folder imggen/image !");
            }
        }
        Config.init();
        Registry.register(Registry.BIOME_SOURCE, new Identifier("imggen:imggen"), ImgGenBiomeSource.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, new Identifier("imggen:imggen"), ImgGenChunkGenerator.CODEC);
    }

    public static void refreshConfig(){
        ImgGen.CONFIG= ConfigUtil.getFromConfig(MainConfigData.class,Paths.get("", "config", "imggen.json"));
    }
}
