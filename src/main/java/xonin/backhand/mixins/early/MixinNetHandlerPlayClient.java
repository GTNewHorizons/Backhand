package xonin.backhand.mixins.early;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S2FPacketSetSlot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import xonin.backhand.api.core.InventoryPlayerBackhand;
import xonin.backhand.client.utils.BackhandClientUtils;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {

    @ModifyExpressionValue(
        method = "handleHeldItemChange",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/play/server/S09PacketHeldItemChange;func_149385_c()I",
            ordinal = 1))
    private int backhand$isValidIventorySlot(int original) {
        // return a valid int e.g. between 0 and < 9
        return InventoryPlayerBackhand.isValidSwitch(original) ? 0 : -1;
    }

    @Inject(
        method = "handleSetSlot",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/GuiScreen;",
            ordinal = 0),
        cancellable = true)
    private void backhand$checkIfIgnore(S2FPacketSetSlot packetIn, CallbackInfo ci) {
        if (BackhandClientUtils.ignoreSetSlot) {
            ci.cancel();
        }
    }

}
