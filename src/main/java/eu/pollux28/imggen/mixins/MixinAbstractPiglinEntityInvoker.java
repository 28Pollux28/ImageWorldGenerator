package eu.pollux28.imggen.mixins;

import net.minecraft.entity.mob.AbstractPiglinEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractPiglinEntity.class)
public interface MixinAbstractPiglinEntityInvoker {
    @Invoker("isImmuneToZombification")
    boolean invokeIsImmuneToZombification();
}

