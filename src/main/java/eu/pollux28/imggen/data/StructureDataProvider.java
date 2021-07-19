package eu.pollux28.imggen.data;

import eu.pollux28.imggen.ImgGen;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;

import java.awt.image.BufferedImage;

public final class StructureDataProvider {
    private final ColorConverter<ConfiguredStructureFeature<?,?>> colorConverter;
    private final ImageManipulator imageManipulator;
    private final int width;
    private final int height;
    private final int width12;
    private final int height12;

    public StructureDataProvider(ColorConverter<ConfiguredStructureFeature<?,?>> colorConverter, String path){
        this.colorConverter = colorConverter;
        this.imageManipulator = new ImageManipulator(path,"s");

        width = imageManipulator.getWidth();
        height = imageManipulator.getHeight();

        width12 = width/2;
        height12 = height/2;
    }
    public boolean isInImage(int x, int y){
        int centeredX = x + width12;
        int centeredY = y + height12;

        return !(centeredX < 0         || centeredY < 0 ||
                centeredX > width - 1 || centeredY > height - 1);

    }

    public ConfiguredStructureFeature<?,?> GetData(int x, int y){
        int centeredX = x + width12;
        int centeredY = y + height12;
        if (!isInImage(x,y)){
            if(!ImgGen.CONFIG.repeatStructureImage){
                return colorConverter.GetDefaultValue(centeredX,centeredY);
            }else {
                if(!ImgGen.CONFIG.repeatMirrorStructureImage){
                    centeredX = centeredX>=0 ? centeredX%width:-width*((centeredX+1)/width)+width+centeredX;
                    centeredY = centeredY>=0 ? centeredY%height:-height*((centeredY+1)/height)+height+centeredY;
                }else{
                    centeredX = centeredX<=0? -centeredX-1 : centeredX;
                    centeredY = centeredY<=0? -centeredY-1 : centeredY;
                    int kX = Math.abs(centeredX/(width))+(centeredX>=0?0:1);
                    int kY = Math.abs(centeredY/(height))+(centeredY>=0?0:1);
                    centeredX = kX%2==0 ? centeredX%width : width-1-centeredX%width;
                    centeredY = kY%2==0 ? centeredY%height : height-1-centeredY%height;
                }
                int rgb = imageManipulator.getRGB(centeredX, centeredY);
                return colorConverter.GetValue(rgb);
            }
        }
        int rgb = imageManipulator.getRGB(centeredX, centeredY);
        return colorConverter.GetValue(rgb);
    }
}
