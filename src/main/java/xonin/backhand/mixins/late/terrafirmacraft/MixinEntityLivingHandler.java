package xonin.backhand.mixins.late.terrafirmacraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(
    value = { com.bioxx.tfc.Handlers.EntityLivingHandler.class, com.dunk.tfc.Handlers.EntityLivingHandler.class },
    remap = false)
public class MixinEntityLivingHandler {

    @ModifyConstant(method = "handleItemPickup", constant = @Constant(intValue = 36))
    private int backhand$fixPickupSlot(int constant) {
        return constant + 1;
    }
}
