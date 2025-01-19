package xonin.backhand.mixins.late.tconstruct;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import tconstruct.library.weaponry.IWindup;
import tconstruct.weaponry.client.CrosshairHandler;
import xonin.backhand.api.core.BackhandUtils;

@Mixin(CrosshairHandler.class)
public class MixinCrosshairHandler {

    @ModifyExpressionValue(
        method = "onRenderOverlay",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/entity/EntityClientPlayerMP;getCurrentEquippedItem()Lnet/minecraft/item/ItemStack;",
            ordinal = 0),
        remap = false)
    private ItemStack backhand$onRenderOverlay(ItemStack original) {
        if (original == null || !(original.getItem() instanceof IWindup)) {
            ItemStack offhand = BackhandUtils.getOffhandItem(Minecraft.getMinecraft().thePlayer);
            if (offhand != null && offhand.getItem() instanceof IWindup) {
                return offhand;
            }
        }
        return original;
    }
}
