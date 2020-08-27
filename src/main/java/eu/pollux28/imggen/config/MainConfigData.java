package eu.pollux28.imggen.config;
//Code used from Simplex Terrain <https://github.com/SuperCoder7979/simplexterrain>, with permission from SuperCoder79
import eu.pollux28.imggen.ImgGen;
import eu.pollux28.imggen.util.BiomeIDAndRGBPair;

import java.util.Arrays;
import java.util.List;

public class MainConfigData {
    public String configVersion = ImgGen.VERSION;
    public String imageName="defaultImage.jpg";
    public String defaultBiome="minecraft:ocean";
    public double scale = 1.0;
    public List<BiomeIDAndRGBPair> customBiomes = Arrays.asList(new BiomeIDAndRGBPair("modid:biomeid","0x000000"),new BiomeIDAndRGBPair("modid:biomeid","0xFFFFFF"));
    public float lakeFormationPercentChance = 0.5f;
}
