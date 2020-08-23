package eu.pollux28.genmap;

import eu.pollux28.genmap.config.GenMapConfig;
import eu.pollux28.genmap.gen.GenMapType;
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


public class GenMap implements ModInitializer {
    public static Logger logger = LogManager.getLogger();
    public static GenMapType levelGeneratorType;
    public static GenMapConfig config;


    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void onInitialize() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT){
            levelGeneratorType = new GenMapType("genmap");
        }
        AutoConfig.register(GenMapConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(GenMapConfig.class).getConfig();
        Path genMapDir = Paths.get("", "genmap","image");
        if (!Files.isDirectory(genMapDir)){
            genMapDir.toFile().mkdirs();
        }

    }
}
