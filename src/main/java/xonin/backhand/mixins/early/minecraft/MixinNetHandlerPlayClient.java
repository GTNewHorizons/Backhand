package xonin.backhand.mixins.early.minecraft;

import net.minecraft.client.network.NetHandlerPlayClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import xonin.backhand.api.core.InventoryPlayerBackhand;

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
        return InventoryPlayerBackhand.isValidSwitch(original) ? 0 : -1;
    }
}