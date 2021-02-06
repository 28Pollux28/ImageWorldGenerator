package eu.pollux28.imggen.util;

public class BiomeIDAndRGBPair {
    public final String biomeID;
    public final String RGB;
    public BiomeIDAndRGBPair(String biomeID, String RGB) {
        this.biomeID = biomeID;
        this.RGB = RGB;
    }

    public String getRGB() {
        return RGB;
    }
}
