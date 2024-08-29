package xonin.backhand.mixins.early.minecraft;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xonin.backhand.api.core.BackhandUtils;

@Mixin(EntityItem.class)
public abstract class MixinEntityItem {

    @Inject(
        method = "onCollideWithPlayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/item/EntityItem;getEntityItem()Lnet/minecraft/item/ItemStack;"))
    private void backhand$resetHotswapOnPickup(EntityPlayer entityIn, CallbackInfo ci) {
        BackhandUtils.resetAndDelayHotswap(entityIn, 0);
    }
}
