package eu.pollux28.imggen;

import eu.pollux28.imggen.config.ImgGenConfig;
import eu.pollux28.imggen.gen.ImgGenType;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class ImgGen implements ModInitializer {
    public static Logger logger = LogManager.getLogger();
    public static ImgGenType levelGeneratorType;
    public static ImgGenConfig config;


    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void onInitialize() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT){
            levelGeneratorType = new ImgGenType("imggen");
        }
        AutoConfig.register(ImgGenConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ImgGenConfig.class).getConfig();
        Path genMapDir = Paths.get("", "imggen","image");
        if (!Files.isDirectory(genMapDir)){
            genMapDir.toFile().mkdirs();
        }
    }
}