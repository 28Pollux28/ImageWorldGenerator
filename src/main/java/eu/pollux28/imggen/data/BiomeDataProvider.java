package eu.pollux28.imggen.data;

import eu.pollux28.imggen.ImgGen;
import net.minecraft.world.biome.Biome;

import java.awt.image.BufferedImage;

public final class BiomeDataProvider {
    private final ColorConverter<Biome> colorConverter;
    private final BufferedImage image;
    private final float scale;
    private final int width;
    private final int height;
    private final int width12;
    private final int height12;
    private final int width2;
    private final int height2;

    public BiomeDataProvider(ColorConverter<Biome> colorConverter, BufferedImage image, float scale){
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
        width12 = width/2;
        height12 = height/2;
        width2 = width*2;
        height2 = height*2;
    }
    public boolean isInImage(int x, int y){
        int centeredX = (int)(x / (scale)) + width12;
        int centeredY = (int)(y / (scale)) + height12;

        return !(centeredX < 0         || centeredY < 0 ||
                centeredX > width - 1 || centeredY > height - 1 ||
                image == null);

    }

    public Biome GetData(int x, int y){
        int centeredX = (int)((x / (scale)) + width12);
        int centeredY = (int)((y / (scale)) + height12);
        int oX = centeredX;
        int oY = centeredY;
        if (!isInImage(x,y)){
            if(!ImgGen.CONFIG.repeatBiomeImage ||image==null){
                return colorConverter.GetDefaultValue(centeredX,centeredY);
            }else {
                if(!ImgGen.CONFIG.repeatMirrorBiomeImage){
                    centeredX = centeredX>=0 ? centeredX%width:-width*((centeredX+1)/width)+width+centeredX;
                    centeredY = centeredY>=0 ? centeredY%height:-height*((centeredY+1)/height)+height+centeredY;
                }else{
                    centeredX = centeredX<0? -centeredX-1 : centeredX;
                    centeredY = centeredY<0? -centeredY-1 : centeredY;
                    int kX = Math.abs(centeredX/(width))+(centeredX>=0?0:1);
                    int kY = Math.abs(centeredY/(height))+(centeredY>=0?0:1);
                    centeredX = kX%2==0 ? centeredX%width : width-1-centeredX%width;
                    centeredY = kY%2==0 ? centeredY%height : height-1-centeredY%height;
                }
                int rgb = image.getRGB(centeredX, centeredY);
                return colorConverter.GetValue(rgb);
            }
        }
        int rgb = image.getRGB(centeredX, centeredY);
        return colorConverter.GetValue(rgb);
    }

    public int[] getImageCoordAndValue(int x,int y){
        int centeredX = (int)((x / (scale)) + width12);
        int centeredY = (int)((y / (scale)) + height12);
        if (!isInImage(x,y)){
            if(!ImgGen.CONFIG.repeatBiomeImage){
                int rgb = imageManipulator.getRGB(centeredX, centeredY);
                return new int[]{centeredX, centeredY, rgb};
            }
            else {
                if(!ImgGen.CONFIG.repeatMirrorBiomeImage){
                    centeredX = centeredX>=0 ? centeredX%width:-width*((centeredX+1)/width)+width+centeredX;
                    centeredY = centeredY>=0 ? centeredY%height:-height*((centeredY+1)/height)+height+centeredY;
                }else{
                    centeredX = centeredX<0? -centeredX-1 : centeredX;
                    centeredY = centeredY<0? -centeredY-1 : centeredY;
                    int kX = Math.abs(centeredX/(width))+(centeredX>=0?0:1);
                    int kY = Math.abs(centeredY/(height))+(centeredY>=0?0:1);
                    centeredX = kX%2==0 ? centeredX%width : width-1-centeredX%width;
                    centeredY = kY%2==0 ? centeredY%height : height-1-centeredY%height;
                }
                int rgb = imageManipulator.getRGB(centeredX, centeredY);
                return new int[]{centeredX,centeredY, rgb};
            }
        }
        int rgb = imageManipulator.getRGB(centeredX, centeredY);
        return new int[]{centeredX,centeredY,rgb&0x00FFFFFF};
    }
}
