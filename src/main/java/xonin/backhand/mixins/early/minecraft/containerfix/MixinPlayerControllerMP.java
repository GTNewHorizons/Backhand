package xonin.backhand.mixins.early.minecraft.containerfix;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.hooks.containerfix.IContainerHook;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {

    @Redirect(
        method = "windowClick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/inventory/Container;slotClick(IIILnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;"))
    public ItemStack backhand$windowClick(Container instance, int slotId, int clickedButton, int mode,
        EntityPlayer player) {
        if (((IContainerHook) instance).backhand$wasOpenedWithOffhand()) {
            int heldItem = player.inventory.currentItem;
            player.inventory.currentItem = BackhandUtils.getOffhandSlot(player);
            ItemStack result = instance.slotClick(slotId, clickedButton, mode, player);
            player.inventory.currentItem = heldItem;
            return result;
        } else {
            return instance.slotClick(slotId, clickedButton, mode, player);
        }
    }
}
