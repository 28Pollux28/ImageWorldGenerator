package eu.pollux28.imggen.data;

import eu.pollux28.imggen.ImgGen;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.Level;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static net.minecraft.util.math.MathHelper.square;

public class BiomeColorConverter implements ColorConverter<Biome> {

    private Biome defaultValue;

    private Map<Integer, Biome> biomeColorMap;

    public BiomeColorConverter(Biome defaultValue){
        this.defaultValue = defaultValue;

        this.biomeColorMap = new HashMap<>();
    }

    @Override
    public Biome GetValue(int color) {
        int finalColor = color = color&0xFFFFFF;

        Biome biome = biomeColorMap.get(color);

        if (biome != null){
            return biome;
        }

        biome = biomeColorMap.entrySet().stream().sequential().min(Comparator.comparingDouble(
                (bt1) -> getColorDiff(finalColor, bt1.getKey()))).get().getValue();

        ImgGen.logger.log(Level.WARN, "Found unmapped color code " + Integer.toHexString(color) + "! Mapping it to similar color!");
        RegisterBiome(color, biome);

        return biome;
    }

    @Override
    public Biome GetDefaultValue() {
        return defaultValue;
    }

    public void RegisterBiome(int color, Biome biome){
        if (biomeColorMap.containsKey(color)){
            ImgGen.logger.log(Level.ERROR, "There has already been a Biome registered for the color " + Integer.toHexString(color) + "!");
            return;
        }

        biomeColorMap.put(color, biome);
    }

    private static double getColorDiff(int RGB, int btRGB){
        return square(((RGB)%256)-((btRGB)%256)) + square(((RGB>>8)%256)-((btRGB>>8)%256)) + square(((RGB>>16)%256)-((btRGB>>16)%256));
    }
}
