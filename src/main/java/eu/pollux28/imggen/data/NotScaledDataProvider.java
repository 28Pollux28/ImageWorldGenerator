package eu.pollux28.imggen.data;

import eu.pollux28.imggen.ImgGen;

import java.awt.image.BufferedImage;

public final class NotScaledDataProvider<T> {
    private final ColorConverter<T> colorConverter;
    private final BufferedImage image;
    private final int width;
    private final int height;
    private final int width12;
    private final int height12;
    private final int width2;
    private final int height2;

    public NotScaledDataProvider(ColorConverter<T> colorConverter, BufferedImage image){
        this.colorConverter = colorConverter;
        this.image = image;

        if (image != null){
            width = image.getWidth();
            height = image.getHeight();
        }else{
            width = 0;
            height = 0;
        }
        width12 = width/2;
        height12 = height/2;
        width2 = width*2;
        height2 = height*2;
    }
    public boolean isInImage(int x, int y){
        int centeredX = x + width12;
        int centeredY = y + height12;

        return !(centeredX < 0         || centeredY < 0 ||
                centeredX > width - 1 || centeredY > height - 1 ||
                image == null);

    }

    public T GetData(int x, int y){
        int centeredX = x + width12;
        int centeredY = y + height12;
        if (!isInImage(x,y)){
            if(!ImgGen.CONFIG.repeatImage ||image==null){
                return colorConverter.GetDefaultValue(centeredX,centeredY);
            }else {
                if(!ImgGen.CONFIG.repeatMirrorImage){
                    centeredX = centeredX>=0 ? centeredX%width:((centeredX+1)%width);
                    centeredY = centeredY>=0 ? centeredY%height:((centeredY+1)%height);
                }else{
                    int modWidth = centeredX%(width2);
                    int modHeight = centeredY%(height2);
                    int modWidthNeg = -((centeredX+1)%width2);
                    int modHeightNeg = -((centeredY+1)%height2);
                    centeredX = centeredX>=0 ? modWidth>=width ? width2-1-modWidth:modWidth :
                            modWidthNeg>=width ? width2-1-modWidthNeg:modWidthNeg;
                    centeredY = centeredY>=0 ? modHeight>=height ? height2-1-modHeight:modHeight :
                            modHeightNeg>=height ? height2-1-modHeightNeg:modHeightNeg;
                }
                int rgb = image.getRGB(centeredX, centeredY);
                return colorConverter.GetValue(rgb);
            }
        }
        int rgb = image.getRGB(centeredX, centeredY);
        return colorConverter.GetValue(rgb);
    }
}
