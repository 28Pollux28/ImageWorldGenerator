package eu.pollux28.imggen.mixins;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.surfacebuilder.AbstractNetherSurfaceBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.util.Random;

@Mixin(AbstractNetherSurfaceBuilder.class)
public class MixinValleySurfaceBuilder {

    /**
     * Allow vanilla nether surfacebuilders to work at any height for any dimension including my own.
     *
     * @author TelepathicGrunt
     * @reason We do this to maximize mod compat with vanilla and modded nether biomes as best as we can.
     */
    @ModifyConstant(method = "generate(Ljava/util/Random;Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/world/biome/Biome;IIIDLnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;IIJLnet/minecraft/world/gen/surfacebuilder/TernarySurfaceConfig;)V",
            constant = @Constant(intValue = 127),require = 0)
    private int buildSurfaceAnyHeight(int constant, Random random, Chunk chunkIn, Biome biomeIn, int x, int z, int startHeight)
    {
        return startHeight;
    }
}