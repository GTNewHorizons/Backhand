package xonin.backhand.mixins.early.minecraft.containerfix;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraftforge.common.ForgeHooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.hooks.containerfix.IContainerHook;

@Mixin(value = ForgeHooks.class, remap = false)
public class MixinForgeHooks {

    @Unique
    private static int backhand$heldItem;

    @Inject(method = "canInteractWith", at = @At("HEAD"))
    private static void backhand$canInteractWithPre(EntityPlayer player, Container openContainer,
        CallbackInfoReturnable<Boolean> ci) {
        if (((IContainerHook) openContainer).backhand$wasOpenedWithBackhand()) {
            backhand$heldItem = player.inventory.currentItem;
            player.inventory.currentItem = BackhandUtils.getOffhandSlot(player);
        }
    }

    @Inject(method = "canInteractWith", at = @At("RETURN"))
    private static void backhand$canInteractWithPost(EntityPlayer player, Container openContainer,
        CallbackInfoReturnable<Boolean> ci) {
        if (((IContainerHook) openContainer).backhand$wasOpenedWithBackhand()) {
            player.inventory.currentItem = backhand$heldItem;
        }
    }
}
