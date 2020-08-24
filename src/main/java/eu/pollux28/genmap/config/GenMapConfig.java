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
    public String imageName = "";
    public int scale = 1;




    @ConfigEntry.Gui.Excluded
    @ConfigEntry.Gui.PrefixText()
    public List<BiomeIDAndRGBPair> aList = Arrays.asList(new BiomeIDAndRGBPair("modid:biomeid","0x000000"),new BiomeIDAndRGBPair("modid:biomeid","0xFFFFFF"));

    public static class BiomeIDAndRGBPair {


        public String biomeID;
        public int RGB= 0x000000;



        BiomeIDAndRGBPair(String biomeID, String RGB) {
            this.biomeID = biomeID;
            try {
                this.RGB = Integer.decode(RGB);
            }catch (NumberFormatException e ){
                this.RGB =-1;
            }
        }
    }

}
