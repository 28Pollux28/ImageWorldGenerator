package eu.pollux28.imggen.gen.heightmap;

import eu.pollux28.imggen.ImgGen;
import eu.pollux28.imggen.config.MainConfigData;
import eu.pollux28.imggen.data.BiomeColorConverter;
import eu.pollux28.imggen.data.HeightMapColorConverter;
import eu.pollux28.imggen.data.ImageDataProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.Level;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HeightMapSource {
    private HeightMapColorConverter heightMapColorConverter;
    private final ImageDataProvider<Integer> heightMapDataProvider;

    public HeightMapSource(int seaLevel) {
        ImgGen.refreshConfig();
        MainConfigData config = ImgGen.CONFIG;

        boolean isClient = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;

        if (ImgGen.heightMapColorConverter == null || isClient) {

            ImgGen.heightMapColorConverter = new HeightMapColorConverter(seaLevel);
            heightMapColorConverter = ImgGen.heightMapColorConverter;
        } else {
            heightMapColorConverter = ImgGen.heightMapColorConverter;
        }
        if (ImgGen.heightMapDataProvider == null || isClient) {
            BufferedImage image = loadImage(config.HeightMapName);
            ImgGen.heightMapDataProvider = new ImageDataProvider<>(ImgGen.heightMapColorConverter, image, config.scale);

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
    public int getHeight(int x, int z){
        return heightMapDataProvider.GetData(x,z);
    }


}
