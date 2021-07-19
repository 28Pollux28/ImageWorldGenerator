package eu.pollux28.imggen.gen.structures;

import eu.pollux28.imggen.ImgGen;
import eu.pollux28.imggen.data.StructureColorConverter;
import eu.pollux28.imggen.data.StructureColors;
import eu.pollux28.imggen.data.StructureDataProvider;
import eu.pollux28.imggen.util.StructureAndRGBPair;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.HashSet;

public class StructuresSource {

    private final MutableRegistry<ConfiguredStructureFeature<?,?>> configuredStructureFeatures;

    public HashSet<StructureFeature<?>> configuredStructureFeaturesBlackList = new HashSet<>();
    private StructureColorConverter structureColorConverter;
    private StructureDataProvider structureDataProvider;

    public StructuresSource(MutableRegistry<ConfiguredStructureFeature<?, ?>> configuredStructureFeatures) {
        this.configuredStructureFeatures = configuredStructureFeatures;
    }

    public StructureColorConverter getStructureColorConverter() {
        return structureColorConverter;
    }

    public void setStructureColorConverter(StructureColorConverter structureColorConverter) {
        this.structureColorConverter = structureColorConverter;
    }

    public StructureDataProvider getStructureDataProvider() {
        return structureDataProvider;
    }

    public void setStructureDataProvider(StructureDataProvider structureDataProvider) {
        this.structureDataProvider = structureDataProvider;
    }

    public void registerStructures(){
        for(StructureColors structureColor:StructureColors.values()){
            structureColorConverter.RegisterStructure(structureColor.getRGB(),structureColor.getConfiguredStructureFeature());
        }
        for (StructureAndRGBPair structureIDAndRGBPair :ImgGen.CONFIG.customStructuresRGB) {
            int RGB;
            try {
                RGB = Integer.decode(structureIDAndRGBPair.RGB);
            } catch (NumberFormatException e) {
                RGB = -1;
            }
            if (RGB == -1) {
                ImgGen.logger.log(Level.ERROR, "Structure " + structureIDAndRGBPair.structureID + " has incorrect color code. Must be in the form of : " +
                        "0xRRGGBB using hexadecimal code.");
                continue;
            }
            Identifier sID = getIdFromString(structureIDAndRGBPair.structureID);
            if (sID == null) {
                ImgGen.logger.log(Level.ERROR, "Incorrect structureID format. Expected modid:structureid, got " + structureIDAndRGBPair.structureID);
                continue;
            }
            ConfiguredStructureFeature<?, ?> configuredStructureFeature = getStructureByID(sID);
            if (configuredStructureFeature == null) {
                if (!structureIDAndRGBPair.structureID.equals("modid:structureid")) {
                    ImgGen.logger.log(Level.ERROR, "Couldn't find structure at " + structureIDAndRGBPair.structureID);
                }
                continue;
            }
            if(structureColorConverter.RegisterStructure(RGB,configuredStructureFeature)){
            ImgGen.logger.log(Level.DEBUG, "Registered Structure " + sID.toString() + " with a color code of " + Integer.toHexString(RGB));
            }
        }

    }
    public void registerBlackList(){
        for(String id : ImgGen.CONFIG.structureBlacklist){
            Identifier sID =getIdFromString(id);
            if(sID==null){
                continue;
            }
            ConfiguredStructureFeature<?, ?> configuredStructureFeature = getStructureByID(sID);
            if (configuredStructureFeature == null) {
                continue;
            }
            this.configuredStructureFeaturesBlackList.add(configuredStructureFeature.feature);
        }
    }

    public ArrayList<?> getStructuresInPos(ChunkPos chunkPos){
        ArrayList<ConfiguredStructureFeature<?,?>> structureArray = new ArrayList<>();
        for(int x = chunkPos.getStartX();x<= chunkPos.getEndX();x++){
            for(int z = chunkPos.getStartZ();z<= chunkPos.getEndZ();z++){
                ConfiguredStructureFeature<?,?> configuredStructureFeature = structureDataProvider.GetData(x,z);
                structureArray.add(configuredStructureFeature);
            }
        }
        return structureArray;
    }

    public ConfiguredStructureFeature<?, ?> getStructureByID(Identifier structureID){
        return configuredStructureFeatures.get(structureID);
    }
    private Identifier getIdFromString(String structureID) {
        String[] str = structureID.toLowerCase().split(":");
        if (str.length!=2){
            return null;
        }else return new Identifier(str[0],str[1]);
    }

}
