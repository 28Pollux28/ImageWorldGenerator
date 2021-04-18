package eu.pollux28.imggen.util;

public class StructureAndRGBPair {
    public final String structureID;
    public final String RGB;
    public StructureAndRGBPair(String structureID, String RGB) {
        this.structureID = structureID;
        this.RGB = RGB;
    }

    public String getRGB() {
        return RGB;
    }
}
