package xonin.backhand.mixins.early.minecraft;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.S0BPacketAnimation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import xonin.backhand.api.core.IBackhandPlayer;
import xonin.backhand.api.core.IOffhandInventory;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {

    @ModifyExpressionValue(
        method = "handleHeldItemChange",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/play/server/S09PacketHeldItemChange;func_149385_c()I",
            ordinal = 1))
    private int backhand$isValidInventorySlot(int original) {
        // return a valid int e.g. between 0 and < 9
        return IOffhandInventory.isValidSwitch(original) ? 0 : -1;
    }

    @Inject(
        method = "handleAnimation",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/server/S0BPacketAnimation;func_148977_d()I"))
    private void backhand$handleOffhandSwing(S0BPacketAnimation packetIn, CallbackInfo ci, @Local Entity entity) {
        if (!(entity instanceof IBackhandPlayer player)) return;
        if (packetIn.func_148978_c() == IOffhandInventory.OFFHAND_HOTBAR_SLOT) {
            player.swingOffItem();
        }
    }
}
