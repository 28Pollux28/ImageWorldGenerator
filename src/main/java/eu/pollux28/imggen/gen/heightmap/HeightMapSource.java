package eu.pollux28.imggen.gen.heightmap;

import eu.pollux28.imggen.ImgGen;
import eu.pollux28.imggen.config.MainConfigData;
import eu.pollux28.imggen.data.HeightMapColorConverter;
import eu.pollux28.imggen.data.NotScaledDataProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.Level;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HeightMapSource {
    private final HeightMapColorConverter heightMapColorConverter;
    public final NotScaledDataProvider<Integer> heightMapDataProvider;

    public HeightMapSource(int seaLevel) {
        ImgGen.refreshConfig();
        MainConfigData config = ImgGen.CONFIG;

        boolean isClient = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;

        if (ImgGen.heightMapColorConverter == null || isClient) {

            ImgGen.heightMapColorConverter = new HeightMapColorConverter(seaLevel);
        }
        heightMapColorConverter = ImgGen.heightMapColorConverter;
        if (ImgGen.heightMapDataProvider == null || isClient) {
            BufferedImage image = loadImage(config.heightMapName);
            if(config.heightMapScale != 1){
                image = transformImage(image);
            }
            ImgGen.heightMapDataProvider = new NotScaledDataProvider<Integer>(ImgGen.heightMapColorConverter, image);

        }

        heightMapDataProvider = ImgGen.heightMapDataProvider;
    }

    private static BufferedImage loadImage(String pathname) {
        BufferedImage img = null;
        try {
            Path configDir = Paths.get("", "imggen", "image", pathname);
            img = ImageIO.read(configDir.toFile());

        } catch (IOException e) {
            e.getCause();
            ImgGen.logger.log(Level.ERROR, "Couldn't find image at /imggen/image/" + pathname);
        }

        return img;
    }
    private BufferedImage transformImage(BufferedImage image) {
        float scale = ImgGen.CONFIG.heightMapScale;
        final int w = image.getWidth();
        final int h = image.getHeight();
        BufferedImage scaledImage = new BufferedImage(Math.round(w * scale),Math.round(h * scale), BufferedImage.TYPE_INT_ARGB);
        final AffineTransform at = AffineTransform.getScaleInstance(scale, scale);
        final AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        scaledImage = ato.filter(image, scaledImage);
        return scaledImage;
    }

    public int getHeight(int x, int z){
        return heightMapDataProvider.GetData(x,z);
    }
    public boolean isInImage(int x,int z){ return heightMapDataProvider.isInImage(x,z);}


}
