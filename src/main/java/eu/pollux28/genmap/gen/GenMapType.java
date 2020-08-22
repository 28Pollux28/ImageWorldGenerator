package eu.pollux28.genmap.gen;
//Code used from Simplex Terrain <https://github.com/SuperCoder7979/simplexterrain>, with permission from SuperCoder79
import eu.pollux28.genmap.gen.biomes.GenMapBiomeSource;
import eu.pollux28.genmap.gen.chunk.GenMapChunkGenerator;
import net.minecraft.client.world.GeneratorType;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorType;

public class GenMapType extends GeneratorType {
    public GenMapType(String translationKey){
        super("genmap");
        GeneratorType.VALUES.add(this);
    }

    @Override
    protected ChunkGenerator method_29076(long l) {
        return new GenMapChunkGenerator(new GenMapBiomeSource(l), l, ChunkGeneratorType.Preset.OVERWORLD.getChunkGeneratorType());
    }

}

