package eu.pollux28.imggen.data;

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
        int centeredX = (int)Math.floor(x / (scale )) + width / 2;
        int centeredY = (int)Math.floor(y / (scale )) + height / 2;

        return !(centeredX < 0         || centeredY < 0 ||
                centeredX > width - 1 || centeredY > height - 1 ||
                image == null);

    }

    public T GetData(int x, int y){
        int centeredX = (int)Math.floor(x / (scale )) + width / 2;
        int centeredY = (int)Math.floor(y / (scale )) + height / 2;
        if (!isInImage(x,y)){
            return colorConverter.GetDefaultValue(centeredX,centeredY);
        }
        int rgb = image.getRGB(centeredX, centeredY);
        return colorConverter.GetValue(rgb);
    }
}
