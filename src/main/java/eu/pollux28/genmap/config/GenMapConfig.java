package eu.pollux28.genmap.config;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Config(name = "genmap")
public class GenMapConfig implements ConfigData {
    public boolean test = false;
    public String imageName = "";
    public int scale = 1;


    @ConfigEntry.Gui.CollapsibleObject
    InnerStuff stuff = new InnerStuff();

    @ConfigEntry.Gui.Excluded
    @ConfigEntry.Gui.PrefixText()
    public List<BiomeIDAndRGBPair> aList = Arrays.asList(new BiomeIDAndRGBPair("modid:biomeid",0x000000),new BiomeIDAndRGBPair("modid:biomeid",0xFFFFFF));

    static class InnerStuff {
        int a = 0;
        int b = 1;
    }

    public static class BiomeIDAndRGBPair {


        public String biomeID;
        public int RGB= 0x000000;



        BiomeIDAndRGBPair(String biomeID, int RGB) {
            this.biomeID = biomeID;
            this.RGB = RGB;
        }
    }

}
