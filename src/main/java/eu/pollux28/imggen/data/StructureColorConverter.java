package eu.pollux28.imggen.data;

import eu.pollux28.imggen.ImgGen;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import org.apache.logging.log4j.Level;

import java.util.Comparator;
import java.util.Map;

public class StructureColorConverter implements ColorConverter<ConfiguredStructureFeature<?,?>> {
    private final Map<Integer,ConfiguredStructureFeature<?,?>> structureColorMap;
    public StructureColorConverter(){
        this.structureColorMap = new Int2ObjectOpenHashMap<>();
    }

    @Override
    public ConfiguredStructureFeature<?, ?> GetValue(int color) {
        synchronized (structureColorMap) {
            int finalColor = color = color & 0x00FFFFFF;

            ConfiguredStructureFeature<?, ?> configuredStructureFeature = structureColorMap.get(finalColor);
            if(configuredStructureFeature!=null || finalColor==0){
                return configuredStructureFeature;

            }
            configuredStructureFeature = structureColorMap.entrySet().stream().sequential().min(Comparator.comparingDouble(
                    (bt1) -> getColorDiff(finalColor, bt1.getKey()))).get().getValue();

            ImgGen.logger.log(Level.DEBUG, "Found unmapped color code " + Integer.toHexString(color) + "! Mapping it to similar color!");
            RegisterStructure(finalColor, configuredStructureFeature);

            return configuredStructureFeature;
        }

    }


    @Override
    public ConfiguredStructureFeature<?, ?> GetDefaultValue(int X, int Z) {
        return StructureColors.NONE.configuredStructureFeature;
    }

    public boolean RegisterStructure(int color, ConfiguredStructureFeature<?,?> configuredStructureFeature){
        if (structureColorMap.containsKey(color)){
            ImgGen.logger.log(Level.ERROR, "There has already been a Structure registered for the color " + Integer.toHexString(color) + "!");
            return false;
        }
        structureColorMap.put(color, configuredStructureFeature);
        return true;
    }

    private double getColorDiff(int RGB, int btRGB){
        return square(((RGB)%256)-((btRGB)%256)) + square(((RGB>>8)%256)-((btRGB>>8)%256)) + square(((RGB>>16)%256)-((btRGB>>16)%256));
    }
    private float square(float f) {
        return f * f;
    }
}
