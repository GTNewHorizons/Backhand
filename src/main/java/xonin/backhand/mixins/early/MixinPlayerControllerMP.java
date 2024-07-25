package xonin.backhand.mixins.early;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import xonin.backhand.client.BackhandClientTickHandler;
import xonin.backhand.client.ClientEventHandler;
import xonin.backhand.client.utils.BackhandClientUtils;

@Mixin(PlayerControllerMP.class)
public abstract class MixinPlayerControllerMP {

    @Shadow
    public abstract void resetBlockRemoving();

    @Inject(method = "resetBlockRemoving", at = @At("HEAD"), cancellable = true)
    private void backhand$cancelRemoval(CallbackInfo ci) {
        if (BackhandClientUtils.countToCancel > 0) {
            BackhandClientUtils.countToCancel--;
            ci.cancel();
        } else {
            if (BackhandClientUtils.hotSwapped) {
                Minecraft.getMinecraft().playerController.syncCurrentPlayItem();
                BackhandClientUtils.hotSwapped = false;
            }
        }
    }

    @Inject(method = "clickBlock", at = @At("HEAD"), cancellable = true)
    private void backhand$clickBlock(CallbackInfo ci) {
        if (ClientEventHandler.cancelone) {
            resetBlockRemoving();
            ci.cancel();
        }
    }

    @Inject(method = "onPlayerDamageBlock", at = @At("HEAD"), cancellable = true)
    private void backhand$onPlayerDamageBlock(CallbackInfo ci) {
        if (ClientEventHandler.cancelone) {
            resetBlockRemoving();
            ci.cancel();
        }
    }

    @ModifyReturnValue(method = "interactWithEntitySendPacket", at = @At("RETURN"))
    private boolean backhand$setAttackDelay(boolean original) {
        if (original) {
            BackhandClientTickHandler.attackDelay = 5;
        }
        return original;
    }
}
