package eu.pollux28.imggen.data;

public class HeightMapColorConverter implements ColorConverter{

    private final int defaultHeight;


    public HeightMapColorConverter(int defaultValue){
        this.defaultHeight = defaultValue;
    }
    @Override
    public Integer GetValue(int color) {
        return color&0xFF;

    }

    @Override
    public Integer GetDefaultValue(int biomeX,int biomeZ) {
        return defaultHeight;
    }
}
