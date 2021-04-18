package eu.pollux28.imggen.data;

import eu.pollux28.imggen.ImgGen;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeLayerSampler;
import org.apache.logging.log4j.Level;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class BiomeColorConverter implements ColorConverter<Biome> {

    private final Biome defaultValue;
    private final BiomeLayerSampler biomeSampler;
    private final Registry<Biome> biomeRegistry;
    private final Map<Integer, Biome> biomeColorMap;

    public BiomeColorConverter(Biome defaultValue, BiomeLayerSampler biomeSampler, Registry<Biome>biomeRegistry){
        this.defaultValue = defaultValue;
        this.biomeSampler = biomeSampler;
        this.biomeRegistry = biomeRegistry;
        this.biomeColorMap = new ConcurrentHashMap<>();

    }

    @Override
    public Biome GetValue(int color) {
        int finalColor = color = color&0x00FFFFFF;

        Biome biome = biomeColorMap.get(finalColor);

        if (biome != null){
            return biome;
        }

        biome = biomeColorMap.entrySet().stream().sequential().min(Comparator.comparingDouble(
                (bt1) -> getColorDiff(finalColor, bt1.getKey()))).get().getValue();

        ImgGen.logger.log(Level.DEBUG, "Found unmapped color code " + Integer.toHexString(color) + "! Mapping it to similar color!");
        RegisterBiome(finalColor, biome);

        return biome;
    }

    @Override
    public Biome GetDefaultValue(int biomeX,int biomeZ) {
        if(ImgGen.CONFIG.continuousGen){
            return this.biomeSampler.sample(this.biomeRegistry, biomeX, biomeZ);
        }else{
            return this.defaultValue;
        }
    }

    public void RegisterBiome(int color, Biome biome){
        if (biomeColorMap.containsKey(color)){
            ImgGen.logger.log(Level.ERROR, "There has already been a Biome registered for the color " + Integer.toHexString(color) + "!");
            return;
        }

        biomeColorMap.put(color, biome);
    }

    private double getColorDiff(int RGB, int btRGB){
        return square(((RGB)%256)-((btRGB)%256)) + square(((RGB>>8)%256)-((btRGB>>8)%256)) + square(((RGB>>16)%256)-((btRGB>>16)%256));
    }
    private float square(float f){
        return f*f;
    }
}
