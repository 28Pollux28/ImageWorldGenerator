package eu.pollux28.genmap;

import eu.pollux28.genmap.gen.GenMapType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class GenMap implements ModInitializer {

    public static GenMapType levelGeneratorType;

    @Override
    public void onInitialize() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT){
            levelGeneratorType = new GenMapType("genmap");
        }
    }
}
