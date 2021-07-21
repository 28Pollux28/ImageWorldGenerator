package eu.pollux28.imggen.mixins;

import eu.pollux28.imggen.ImgGen;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.LakeFeature;
import net.minecraft.world.gen.feature.SingleStateFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(LakeFeature.class)
public class MixinLakesGenerator {
    @Inject(method = "generate", at = @At("HEAD"),cancellable = true)
    private void injectLessLakes(FeatureContext<SingleStateFeatureConfig> context, CallbackInfoReturnable<Boolean> cir){
        if (context.getRandom().nextFloat() < ImgGen.CONFIG.lakeFormationPercentChance){
            cir.setReturnValue(false);
        }
    }
}

