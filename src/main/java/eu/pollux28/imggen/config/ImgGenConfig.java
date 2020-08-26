package eu.pollux28.imggen.config;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;

import java.util.Arrays;
import java.util.List;

@Config(name = "imggen")
public class ImgGenConfig implements ConfigData {
    public String imageName = "defaultImage.jpg";
    public double scale = 1.0;
    public String defaultBiome="minecraft:ocean";
    public float lakeFormationPercent = 0.5f;



    @ConfigEntry.Gui.Excluded
    @ConfigEntry.Gui.PrefixText()
    public List<BiomeIDAndRGBPair> biomeList = Arrays.asList(new BiomeIDAndRGBPair("modid:biomeid","0x000000"),new BiomeIDAndRGBPair("modid:biomeid","0xFFFFFF"));

    public static class BiomeIDAndRGBPair {


        public String biomeID;
        public int RGB;



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
