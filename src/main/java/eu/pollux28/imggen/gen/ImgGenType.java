package eu.pollux28.imggen.gen;
//Code used from Simplex Terrain <https://github.com/SuperCoder7979/simplexterrain>, with permission from SuperCoder79
import eu.pollux28.imggen.ImgGen;
import eu.pollux28.imggen.gen.biomes.ImgGenBiomeSource;
import eu.pollux28.imggen.gen.chunk.ImgGenChunkGenerator;
import net.minecraft.client.world.GeneratorType;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorType;
import net.minecraft.world.gen.chunk.SurfaceChunkGenerator;

public class ImgGenType extends GeneratorType {
    public ImgGenType(String translationKey){
        super("imggen");
        GeneratorType.VALUES.add(this);
    }

    @Override
    protected ChunkGenerator method_29076(long l) {
        //ImgGen.refreshConfig();

        ImgGen.biomeSource = new ImgGenBiomeSource(l);
        return new SurfaceChunkGenerator(ImgGen.biomeSource, l, ChunkGeneratorType.Preset.OVERWORLD.getChunkGeneratorType());
    }

}

