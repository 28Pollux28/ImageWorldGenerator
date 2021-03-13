package eu.pollux28.imggen.gen;
//Code used from Simplex Terrain <https://github.com/SuperCoder7979/simplexterrain>, with permission from SuperCoder79
import eu.pollux28.imggen.ImgGen;
import eu.pollux28.imggen.gen.biomes.ImgGenBiomeSource;
import eu.pollux28.imggen.gen.chunk.ImgGenChunkGenerator;
import net.minecraft.client.world.GeneratorType;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;

public class ImgGenType extends GeneratorType {
    public ImgGenType(String translationKey){
        super("imggen");
        GeneratorType.VALUES.add(this);
    }

    @Override
    protected ChunkGenerator getChunkGenerator(Registry<Biome> biomeRegistry, Registry<ChunkGeneratorSettings> chunkGeneratorSettingsRegistry, long seed) {
        BiomeSource biomeSource = new ImgGenBiomeSource(seed, biomeRegistry);
        return new ImgGenChunkGenerator(biomeSource, seed, () -> chunkGeneratorSettingsRegistry.get(ChunkGeneratorSettings.OVERWORLD));
    }


}

