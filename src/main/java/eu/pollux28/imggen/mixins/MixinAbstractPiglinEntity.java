package eu.pollux28.imggen.mixins;

import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractPiglinEntity.class)
public class MixinAbstractPiglinEntity {
    @Inject(method = "shouldZombify", at = @At("RETURN"), cancellable = true)
    public void protectPiglinInNetherBiome(CallbackInfoReturnable<Boolean> cir){
        AbstractPiglinEntity piglin =((AbstractPiglinEntity)(Object)this);
        cir.setReturnValue(piglin.world.getBiome(piglin.getBlockPos()).getCategory() != Biome.Category.NETHER && !piglin.isAiDisabled()&& !((MixinAbstractPiglinEntityInvoker)piglin).invokeIsImmuneToZombification());
    }
}

