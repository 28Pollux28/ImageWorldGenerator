package eu.pollux28.genmap;

import eu.pollux28.genmap.config.GenMapConfig;
import eu.pollux28.genmap.gen.GenMapType;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class GenMap implements ModInitializer {

    public static GenMapType levelGeneratorType;
    public static GenMapConfig config;


    @Override
    public void onInitialize() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT){
            levelGeneratorType = new GenMapType("genmap");
        }
        AutoConfig.register(GenMapConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(GenMapConfig.class).getConfig();

    }
}
