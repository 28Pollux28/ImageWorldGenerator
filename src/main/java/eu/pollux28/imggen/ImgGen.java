package eu.pollux28.imggen;

import eu.pollux28.imggen.config.Config;
import eu.pollux28.imggen.config.ConfigUtil;
import eu.pollux28.imggen.config.MainConfigData;
import eu.pollux28.imggen.gen.ImgGenType;
import eu.pollux28.imggen.gen.biomes.ImgGenBiomeSource;
import eu.pollux28.imggen.gen.chunk.ImgGenChunkGenerator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class ImgGen implements ModInitializer {
    public static final String VERSION ="0.1.3.1";
    public static Logger logger = LogManager.getLogger();
    public static ImgGenType levelGeneratorType;
    public static MainConfigData CONFIG;
    public static ImgGenBiomeSource biomeSource= null;


    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void onInitialize() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT){
            levelGeneratorType = new ImgGenType("imggen");
        }
        Path genMapDir = Paths.get("", "imggen","image");
        if (!Files.isDirectory(genMapDir)){
            genMapDir.toFile().mkdirs();
        }
        Config.init();
        Registry.register(Registry.CHUNK_GENERATOR, new Identifier("imggen:imggen"), ImgGenChunkGenerator.CODEC);
        Registry.register(Registry.BIOME_SOURCE, new Identifier("imggen:imggen"), ImgGenBiomeSource.CODEC);


    }
    public static void refreshConfig(){
        ImgGen.CONFIG= ConfigUtil.getFromConfig(MainConfigData.class,Paths.get("", "config", "imggen.json"));
    }
}
