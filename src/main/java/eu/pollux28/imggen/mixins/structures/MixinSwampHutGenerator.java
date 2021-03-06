package eu.pollux28.imggen.mixins.structures;


import eu.pollux28.imggen.gen.chunk.ImgGenChunkGenerator;
import net.minecraft.structure.JungleTempleGenerator;
import net.minecraft.structure.SwampHutGenerator;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(SwampHutGenerator.class)
public class MixinSwampHutGenerator {
    @Inject(method = "generate",at = @At("HEAD"))
    public void fixedYImgGen(StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos, CallbackInfoReturnable cir){
        if(chunkGenerator instanceof ImgGenChunkGenerator){
            BlockBox blockBox = ((SwampHutGenerator)(Object)this).getBoundingBox();
            blockBox.move(0,chunkGenerator.getHeight(blockBox.getCenter().getX(),blockBox.getCenter().getZ(), Heightmap.Type.WORLD_SURFACE_WG)-blockBox.minY,0);
        }
    }
    
    
}
    