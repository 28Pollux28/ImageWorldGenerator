package eu.pollux28.imggen.gen.heightmap;

import eu.pollux28.imggen.ImgGen;
import eu.pollux28.imggen.config.MainConfigData;
import eu.pollux28.imggen.data.HeightMapColorConverter;
import eu.pollux28.imggen.data.HeightDataProvider;
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
    private HeightMapColorConverter heightMapColorConverter;
    private HeightDataProvider heightMapDataProvider;

    public HeightMapSource() {
        ImgGen.refreshConfig();
        MainConfigData config = ImgGen.CONFIG;
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
        return getHeightMapDataProvider().GetData(x,z);
    }
    public boolean isInImage(int x,int z){ return getHeightMapDataProvider().isInImage(x,z);}

    public HeightMapColorConverter getHeightMapColorConverter() {
        return heightMapColorConverter;
    }

    public void setHeightMapColorConverter(HeightMapColorConverter heightMapColorConverter) {
        this.heightMapColorConverter = heightMapColorConverter;
    }

    public HeightDataProvider getHeightMapDataProvider() {
        return heightMapDataProvider;
    }

    public void setHeightMapDataProvider(HeightDataProvider heightMapDataProvider) {
        this.heightMapDataProvider = heightMapDataProvider;
    }
}
