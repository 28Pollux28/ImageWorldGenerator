package eu.pollux28.imggen;

import eu.pollux28.imggen.command.Commands;
import eu.pollux28.imggen.config.Config;
import eu.pollux28.imggen.config.ConfigUtil;
import eu.pollux28.imggen.config.MainConfigData;
import eu.pollux28.imggen.data.*;
import eu.pollux28.imggen.gen.ImgGenType;
import eu.pollux28.imggen.gen.biomes.ImgGenBiomeSource;
import eu.pollux28.imggen.gen.chunk.ImgGenChunkGenerator;
import eu.pollux28.imggen.gen.heightmap.HeightMapSource;
import eu.pollux28.imggen.gen.structures.StructuresSource;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ImgGen implements ModInitializer {
    public static final String VERSION = "1.3.2";
    public static final Logger logger = LogManager.getLogger();
    public static MainConfigData CONFIG;
    public static BiomeColorConverter biomeColorConverter;
    public static BiomeDataProvider biomeDataProvider;
    public static HeightMapColorConverter heightMapColorConverter;
    public static HeightDataProvider heightMapDataProvider;
    public static StructureColorConverter structureColorConverter;
    public static StructureDataProvider structureDataProvider;
    public static DynamicRegistryManager registryManager;

    private ImgGenType imgGenType;


    @Override
    public void onInitialize() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            imgGenType = new ImgGenType("imggen");
        }
        Commands.init();
        Path genMapDir = Paths.get("", "imggen", "image");
        if (!Files.isDirectory(genMapDir)) {
            boolean dir = genMapDir.toFile().mkdirs();
            if(!dir){
                logger.log(Level.FATAL,"Could not create folder imggen/image !");
            }
        }
        genMapDir = Paths.get("", "imggen", "image","gen");
        if (!Files.isDirectory(genMapDir)) {
            boolean dir = genMapDir.toFile().mkdirs();
            if(!dir){
                ImgGen.logger.log(Level.FATAL,"Could not create folder imggen/image/gen !");
            }
        }
        Config.init();
        Registry.register(Registry.BIOME_SOURCE, new Identifier("imggen:imggen"), ImgGenBiomeSource.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, new Identifier("imggen:imggen"), ImgGenChunkGenerator.CODEC);
        ServerWorldEvents.LOAD.register(getListener());
    }

    private ServerWorldEvents.Load getListener() {
        return (server, world) -> {
            ChunkGenerator chunkGenerator = world.getChunkManager().getChunkGenerator();
            if (chunkGenerator instanceof ImgGenChunkGenerator) {
                registryManager = world.getRegistryManager();
                ImgGenChunkGenerator imgGenChunkGenerator = (ImgGenChunkGenerator) chunkGenerator;
                ImgGenBiomeSource biomeSource = (ImgGenBiomeSource) imgGenChunkGenerator.getBiomeSource();
                refreshConfig();
                File index = Paths.get("", "imggen", "image","gen").toFile();
                try {
                    FileUtils.cleanDirectory(index);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //BiomeSource
                biomeColorConverter = new BiomeColorConverter(biomeSource.getDefaultBiome(),biomeSource.getBiomeSampler(),biomeSource.getBiomeRegistry());
                biomeSource.setBiomeColorConverter(biomeColorConverter);
                biomeSource.registerBiomes();
                biomeDataProvider = new BiomeDataProvider(biomeColorConverter, CONFIG.biomesImageScale,CONFIG.biomesImageName);
                biomeSource.setBiomeDataProvider(biomeDataProvider);
                //HeightMapSource
                if(CONFIG.customHeightMap){
                    imgGenChunkGenerator.setHeightMapSource(new HeightMapSource());
                    heightMapColorConverter = new HeightMapColorConverter(CONFIG.seaLevel);
                    heightMapDataProvider = new HeightDataProvider(ImgGen.heightMapColorConverter, CONFIG.heightMapScale, CONFIG.heightMapName);
                    imgGenChunkGenerator.getHeightMapSource().setHeightMapColorConverter(heightMapColorConverter);
                    imgGenChunkGenerator.getHeightMapSource().setHeightMapDataProvider(heightMapDataProvider);
                }
                //StructureSource
                if(CONFIG.customStructures){
                    imgGenChunkGenerator.setStructuresSource(new StructuresSource(registryManager.get(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY)));
                    structureColorConverter = new StructureColorConverter();
                    imgGenChunkGenerator.getStructuresSource().setStructureColorConverter(structureColorConverter);
                    imgGenChunkGenerator.getStructuresSource().registerStructures();
                    structureDataProvider = new StructureDataProvider(ImgGen.structureColorConverter, CONFIG.customStructuresImage);
                    imgGenChunkGenerator.getStructuresSource().setStructureDataProvider(structureDataProvider);


                    if(CONFIG.useBlackList){
                        imgGenChunkGenerator.getStructuresSource().registerBlackList();
                    }
                }


            }
        };
    }

    public static void refreshConfig(){
        ImgGen.CONFIG= ConfigUtil.getFromConfig(MainConfigData.class,Paths.get("", "config", "imggen.json"));
    }
}
