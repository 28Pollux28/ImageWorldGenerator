package eu.pollux28.imggen.mixins;

import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoglinEntity.class)
public class MixinHoglinEntity {
    @Inject(method = "canConvert", at = @At("RETURN"), cancellable = true)
    public void protectHoglinInNetherBiome(CallbackInfoReturnable<Boolean> cir){
        HoglinEntity piglin =((HoglinEntity)(Object)this);
        cir.setReturnValue(piglin.world.getBiome(piglin.getBlockPos()).getCategory() != Biome.Category.NETHER && !piglin.isAiDisabled()&& !((MixinHoglinEntityInvoker)piglin).invokeIsImmuneToZombification());
    }

}
