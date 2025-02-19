package xonin.backhand.mixins.early.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S30PacketWindowItems;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.IBackhandPlayer;
import xonin.backhand.api.core.IOffhandInventory;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {

    @Shadow
    private Minecraft gameController;

    @ModifyExpressionValue(
        method = "handleHeldItemChange",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/play/server/S09PacketHeldItemChange;func_149385_c()I",
            ordinal = 1))
    private int backhand$isValidInventorySlot(int original) {
        // return a valid int e.g. between 0 and < 9
        return IOffhandInventory.isValidSwitch(original, gameController.thePlayer) ? 0 : -1;
    }

    @Inject(
        method = "handleWindowItems",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/inventory/Container;putStacksInSlots([Lnet/minecraft/item/ItemStack;)V",
            ordinal = 0))
    private void backhand$isValidInventorySlot(S30PacketWindowItems packetIn, CallbackInfo ci) {
        // This is a weird bug where the item in the prioritized hand might briefly cancel the other hand's item use
        // if the prioritized hand's item has an item use but didn't execute it. It causes the used hands item to
        // be replaced with a different instance which causes equality checks between it and the itemInUse to fail.
        // This workaround ensures that the itemInUse stays the same as the current item.
        ItemStack[] stacks = packetIn.func_148910_d();
        EntityPlayer player = gameController.thePlayer;
        if (player.isUsingItem()) {
            if (((IBackhandPlayer) player).isOffhandItemInUse()) {
                int offhandSlot = BackhandUtils.getOffhandSlot(player) + InventoryPlayer.getHotbarSize();
                ItemStack offhandItem = BackhandUtils.getOffhandItem(player);
                if (ItemStack.areItemStacksEqual(stacks[offhandSlot], offhandItem)) {
                    stacks[offhandSlot] = offhandItem;
                }
            } else {
                ItemStack currentItem = player.inventory.getCurrentItem();
                int slot = player.inventory.mainInventory.length - 1 + player.inventory.currentItem;
                if (ItemStack.areItemStacksEqual(stacks[slot], currentItem)) {
                    stacks[slot] = currentItem;
                }
            }
        }
    }
}
