package eu.pollux28.imggen.util;

import java.util.Random;

public class StructureAndRGBPair {
    public final String structureID;
    public final String RGB;

    public StructureAndRGBPair(){
        this.structureID="";
        Random r = new Random();
        this.RGB = Integer.toHexString(r.nextInt(0xFFFFFF));
    }

    public StructureAndRGBPair(String structureID, String RGB) {
        this.structureID = structureID;
        this.RGB = RGB;
    }


    public String getRGB() {
        return RGB;
    }
}
