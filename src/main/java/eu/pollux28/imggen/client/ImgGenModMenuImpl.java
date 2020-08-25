package eu.pollux28.imggen.client;

import eu.pollux28.imggen.config.ImgGenConfig;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;


@Environment(EnvType.CLIENT)
public class ImgGenModMenuImpl implements ModMenuApi{
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory(){
        return parent -> AutoConfig.getConfigScreen(ImgGenConfig.class, parent).get();
    }

}
