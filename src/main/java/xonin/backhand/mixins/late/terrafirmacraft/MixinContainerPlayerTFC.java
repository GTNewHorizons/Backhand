package xonin.backhand.mixins.late.terrafirmacraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = { com.bioxx.tfc.Containers.ContainerPlayerTFC.class, com.dunk.tfc.Containers.ContainerPlayerTFC.class })
public class MixinContainerPlayerTFC {

    @ModifyConstant(method = "transferStackInSlot", constant = @Constant(intValue = 50))
    private int backhand$fixPickupSlot(int constant) {
        return constant + 1;
    }
}
