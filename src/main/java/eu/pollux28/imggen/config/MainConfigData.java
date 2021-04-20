package eu.pollux28.imggen.config;
//Code used from Simplex Terrain <https://github.com/SuperCoder7979/simplexterrain>, with permission from SuperCoder79
import eu.pollux28.imggen.ImgGen;
import eu.pollux28.imggen.util.BiomeIDAndRGBPair;
import eu.pollux28.imggen.util.StructureAndRGBPair;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainConfigData {
    public String configVersion = ImgGen.VERSION;
    public String biomesImageName = "biomeImage.png";
    public float biomesImageScale = 1;
    public boolean repeatBiomeImage = false;
    public boolean repeatMirrorBiomeImage = false;
    public String defaultBiome = "minecraft:ocean";
    public boolean continuousGen = true;
    public boolean largeBiomes = false;
    public List<BiomeIDAndRGBPair> customBiomes = Arrays.asList(new BiomeIDAndRGBPair("modid:biomeid","0x000000"),new BiomeIDAndRGBPair("modid:biomeid","0xFFFFFF"));
    public boolean customHeightMap = false;
    public String heightMapName = "HeightMap.png";
    public float heightMapScale = 1;
    public boolean repeatHeightMapImage = false;
    public boolean repeatMirrorHeightMapImage = false;
    public int seaLevel = 63;
    public boolean placeVanillaStructures = true;
    public boolean customStructures = false;
    public String customStructuresImage = "structureMap.png";
    public boolean repeatStructureImage= false;
    public boolean repeatMirrorStructureImage = false;
    public boolean useBlackList = false;
    public String[] structureBlacklist = {"modid:structureid","modid:structureid"};
    public List<StructureAndRGBPair> customStructuresRGB = Arrays.asList(new StructureAndRGBPair("modid:structureid","0x123456"), new StructureAndRGBPair("modid:structureid","0x024680"));
    public float lakeFormationPercentChance = 0.5f;



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MainConfigData that = (MainConfigData) o;
        return Double.compare(that.biomesImageScale, biomesImageScale) == 0 &&
                Float.compare(that.lakeFormationPercentChance, lakeFormationPercentChance) == 0 &&
                configVersion.equals(that.configVersion) &&
                biomesImageName.equals(that.biomesImageName) &&
                (defaultBiome.equals(that.defaultBiome)) &&
                customBiomes== that.customBiomes;

    }

    @Override
    public int hashCode() {
        return Objects.hash(configVersion, biomesImageName, defaultBiome, biomesImageScale, customBiomes, lakeFormationPercentChance);
    }
}
