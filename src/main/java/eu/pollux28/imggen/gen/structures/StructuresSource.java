package eu.pollux28.imggen.gen.structures;

import eu.pollux28.imggen.ImgGen;
import eu.pollux28.imggen.util.StructureAndRGBPair;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.ConfiguredStructureFeatures;
import org.apache.logging.log4j.Level;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;

import static net.minecraft.util.math.MathHelper.square;

public class StructuresSource {
    private final Long2ObjectOpenHashMap<ArrayList<?>> structuresMap = new Long2ObjectOpenHashMap<>();
    private BufferedImage image;
    private int sizeX;
    private int sizeZ;
    private boolean imgSet= false;
    private final MutableRegistry<ConfiguredStructureFeature<?,?>> configuredStructureFeatures;
    private final Int2ObjectOpenHashMap<ConfiguredStructureFeature<?,?>> structuresRefColors = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<ConfiguredStructureFeature<?,?>> colorsForStructure = new Int2ObjectOpenHashMap<>();

    public StructuresSource(MutableRegistry<ConfiguredStructureFeature<?, ?>> configuredStructureFeatures) {

        this.configuredStructureFeatures = configuredStructureFeatures;
        this.image = setImage(ImgGen.CONFIG.customStructuresMap);
        if(this.image !=null){
            this.sizeX = image.getWidth();
            this.sizeZ = image.getHeight();
            this.imgSet = true;
            loadStructures();
            generateCache();
            dumpImage();
        }
    }

    public ArrayList<?> getStructuresInPos(ChunkPos chunkPos){
        return structuresMap.getOrDefault(chunkPos.toLong(),new ArrayList<>());
    }


    private void loadStructures(){
        for(int i = 0; i< StructureC.values().length; i++){
            StructureC structureC = StructureC.values()[i];
            structuresRefColors.putIfAbsent(structureC.getRGB(),structureC.getConfiguredStructureFeature());
        }
        for (StructureAndRGBPair structureIDAndRGBPair :ImgGen.CONFIG.customStructuresRGB){
            int RGB;
            try {
                RGB = Integer.decode(structureIDAndRGBPair.RGB);
            }catch (NumberFormatException e){
                RGB = -1;
            }
            if (RGB==-1){
                ImgGen.logger.log(Level.ERROR,"Structure "+structureIDAndRGBPair.structureID+" has incorrect color code. Must be in the form of : " +
                        "0xRRGGBB using hexadecimal code.");
                continue;
            }
            Identifier sID = getIdFromString(structureIDAndRGBPair.structureID);
            if(sID!=null){
                ConfiguredStructureFeature<?,?> configuredStructureFeature = getStructureByID(sID);
                if(configuredStructureFeature!=null){
                    int finalRGB = RGB;
                    ConfiguredStructureFeature<?,?> cSF2 =structuresRefColors.merge(RGB,configuredStructureFeature,(v1, v2) ->{
                        ImgGen.logger.log(Level.ERROR,"Color code with key "+Integer.toHexString(finalRGB)+
                                " already exists !, Please choose a different Color Code for Structure "+structureIDAndRGBPair.structureID);
                        return v1;
                    });
                    if (cSF2 == configuredStructureFeature){
                        ImgGen.logger.log(Level.INFO,"Structure "+structureIDAndRGBPair.structureID + " registered with color code: "+structureIDAndRGBPair.RGB);
                    }
                }else{
                    if(!structureIDAndRGBPair.structureID.equals("modid:structureid")){
                        ImgGen.logger.log(Level.ERROR, "Couldn't find structure at "+structureIDAndRGBPair.structureID);
                    }
                }
            }else{
                ImgGen.logger.log(Level.ERROR,"Incorrect structureID format. Expected modid:structureid, got "+ structureIDAndRGBPair.structureID);
            }
        }
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

    private void generateCache() {
        if(!imgSet) return;
        for(int ix = 0; ix< sizeX; ix++){
            for(int iz = 0; iz< sizeZ; iz++){
                int RGB = image.getRGB(ix, iz)&0xFFFFFF;
                if(RGB!=0x000000){
                    BlockPos blockPos = new BlockPos(ix-sizeX/2, 0, iz-sizeZ/2);
                    ChunkPos chunkPos = new ChunkPos(blockPos);
                    if(!this.colorsForStructure.containsKey(RGB)){
                        ConfiguredStructureFeature<?,?> cSF = structuresRefColors.int2ObjectEntrySet().stream().sequential().min(Comparator.comparingDouble(
                                (bt1) -> getColorDiff(RGB, bt1.getIntKey()))).get().getValue();
                        this.colorsForStructure.put(RGB, cSF);
                    }
                    addValues(chunkPos.toLong(),this.colorsForStructure.get(RGB));
                }

            }
        }
        colorsForStructure.clear();
    }
    private void addValues(long pos, ConfiguredStructureFeature<?,?> configuredStructureFeature) {
        ArrayList<ConfiguredStructureFeature<?,?>> tempList;
        if (structuresMap.containsKey(pos)) {
            tempList = (ArrayList<ConfiguredStructureFeature<?, ?>>) structuresMap.get(pos);
            if(tempList == null)
                tempList = new ArrayList<>();
        } else {
            tempList = new ArrayList<>();
        }
        tempList.add(configuredStructureFeature);
        structuresMap.put(pos,tempList);
    }


    private static double getColorDiff(int RGB, int btRGB){
        return square(((RGB)%256)-((btRGB)%256)) + square(((RGB>>8)%256)-((btRGB>>8)%256)) + square(((RGB>>16)%256)-((btRGB>>16)%256));
    }

    public BufferedImage setImage(String pathname){
        BufferedImage img = null;
        try {
            Path configDir = Paths.get("", "imggen", "image", pathname);
            img = ImageIO.read(configDir.toFile());

        } catch (IOException e) {
            e.getCause();
            ImgGen.logger.log(Level.ERROR,"Couldn't find image at /imggen/image/"+pathname);
        }
        return img;
    }
    private void dumpImage(){
        this.image = null;
    }

    public enum StructureC {
        PILLAGER_OUTPOST(0xB5DB88, ConfiguredStructureFeatures.PILLAGER_OUTPOST),
        MINESHAFT(0x056621, ConfiguredStructureFeatures.MINESHAFT),
        MINESHAFT_MESA(0xD94515, ConfiguredStructureFeatures.MINESHAFT_MESA),
        MANSION(0x40511A, ConfiguredStructureFeatures.MANSION),
        JUNGLE_PYRAMID(0x537B09, ConfiguredStructureFeatures.JUNGLE_PYRAMID),
        DESERT_PYRAMID(0xFFBC40, ConfiguredStructureFeatures.DESERT_PYRAMID),
        IGLOO(0x243F36, ConfiguredStructureFeatures.IGLOO),
        SHIPWRECK(0x000090, ConfiguredStructureFeatures.SHIPWRECK),
        SHIPWRECK_BEACHED (0xFADE55, ConfiguredStructureFeatures.SHIPWRECK_BEACHED),
        SWAMP_HUT(0x07F9B2, ConfiguredStructureFeatures.SWAMP_HUT),
        STRONGHOLD(0x606060, ConfiguredStructureFeatures.STRONGHOLD),
        MONUMENT(0x000070, ConfiguredStructureFeatures.MONUMENT),
        OCEAN_RUIN_COLD(0x202070, ConfiguredStructureFeatures.OCEAN_RUIN_COLD),
        OCEAN_RUIN_WARM(0x0000AC, ConfiguredStructureFeatures.OCEAN_RUIN_WARM),
        FORTRESS(0x5e3830, ConfiguredStructureFeatures.FORTRESS),
        NETHER_FOSSIL(0x49907B, ConfiguredStructureFeatures.NETHER_FOSSIL),
        END_CITY(0x8080FF, ConfiguredStructureFeatures.END_CITY),
        BURIED_TREASURE(0xFAF0C0, ConfiguredStructureFeatures.BURIED_TREASURE),
        BASTION_REMNANT(0x403636, ConfiguredStructureFeatures.BASTION_REMNANT),
        VILLAGE_PLAINS(0x8DB360, ConfiguredStructureFeatures.VILLAGE_PLAINS),
        VILLAGE_DESERT(0xFA9418, ConfiguredStructureFeatures.VILLAGE_DESERT),
        VILLAGE_SAVANNA(0xBDB25F, ConfiguredStructureFeatures.VILLAGE_SAVANNA),
        VILLAGE_SNOWY(0xFFFFFF, ConfiguredStructureFeatures.VILLAGE_SNOWY),
        VILLAGE_TAIGA(0x0B6659, ConfiguredStructureFeatures.VILLAGE_TAIGA),
        RUINED_PORTAL(0xDD0808, ConfiguredStructureFeatures.RUINED_PORTAL),
        RUINED_PORTAL_DESERT(0xD25F12, ConfiguredStructureFeatures.RUINED_PORTAL_DESERT),
        RUINED_PORTAL_JUNGLE(0x2C4205, ConfiguredStructureFeatures.RUINED_PORTAL_JUNGLE),
        RUINED_PORTAL_SWAMP(0x2FFFDA, ConfiguredStructureFeatures.RUINED_PORTAL_SWAMP),
        RUINED_PORTAL_MOUNTAIN(0x789878, ConfiguredStructureFeatures.RUINED_PORTAL_MOUNTAIN),
        RUINED_PORTAL_OCEAN(0x202038, ConfiguredStructureFeatures.RUINED_PORTAL_OCEAN),
        RUINED_PORTAL_NETHER(0xbf3b3b, ConfiguredStructureFeatures.RUINED_PORTAL_NETHER),
        NONE(0x000000,null);
        final int RGB;
        final ConfiguredStructureFeature<?,?> configuredStructureFeature;

        StructureC(int RGB, ConfiguredStructureFeature<?,?> configuredStructureFeature) {
            this.RGB = RGB;
            this.configuredStructureFeature = configuredStructureFeature;
        }
        ConfiguredStructureFeature<?,?> getConfiguredStructureFeature() {
            return configuredStructureFeature;
        }
        int getRGB() {
            return RGB;
        }
        }
}
