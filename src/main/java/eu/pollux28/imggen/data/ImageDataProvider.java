package eu.pollux28.imggen.data;

import eu.pollux28.imggen.ImgGen;

import java.awt.image.BufferedImage;

public final class ImageDataProvider<T> {
    private final ColorConverter<T> colorConverter;
    private final BufferedImage image;
    private final double scale;
    private final int width;
    private final int height;

    public ImageDataProvider(ColorConverter<T> colorConverter, BufferedImage image, double scale){
        this.colorConverter = colorConverter;
        this.image = image;
        this.scale = scale;

        if (image != null){
            width = image.getWidth();
            height = image.getHeight();
        }else{
            width = 0;
            height = 0;
        }
    }
    public boolean isInImage(int x, int y){
        int centeredX = (int)Math.floor(x / (scale)) + width / 2;
        int centeredY = (int)Math.floor(y / (scale)) + height / 2;

        return !(centeredX < 0         || centeredY < 0 ||
                centeredX > width - 1 || centeredY > height - 1 ||
                image == null);

    }

    public T GetData(int x, int y){
        int centeredX = (int)Math.floor(x / (scale)) + width / 2;
        int centeredY = (int)Math.floor(y / (scale)) + height / 2;
        if (!isInImage(x,y)){
            if(!ImgGen.CONFIG.repeatImage ||image==null){
                return colorConverter.GetDefaultValue(centeredX,centeredY);
            }else{
                if(!ImgGen.CONFIG.repeatMirrorImage){
                    centeredX = centeredX>=0 ? centeredX%width:-((centeredX+1)%width);
                    centeredY = centeredY>=0 ? centeredY%height:-((centeredY+1)%height);
                }else{
                    int width2 = width*2;
                    int height2= height*2;
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
