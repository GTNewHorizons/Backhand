package xonin.backhand.mixins.early.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.IBackhandPlayer;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {

    @Shadow
    private int currentPlayerItem;

    @Inject(
        method = "syncCurrentPlayItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/NetHandlerPlayClient;addToSendQueue(Lnet/minecraft/network/Packet;)V"))
    private void backhand$updateMainhandSlot(CallbackInfo ci) {

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (currentPlayerItem != BackhandUtils.getOffhandSlot(player)) {
            ((IBackhandPlayer) player).setMainhandSlot(currentPlayerItem);
        }
    }
}
