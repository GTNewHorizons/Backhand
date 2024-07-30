package xonin.backhand.mixins.early;

import net.minecraft.client.multiplayer.PlayerControllerMP;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import xonin.backhand.client.BackhandClientTickHandler;
import xonin.backhand.client.utils.BackhandClientUtils;

@Mixin(PlayerControllerMP.class)
public abstract class MixinPlayerControllerMP {

    @Inject(method = "resetBlockRemoving", at = @At("HEAD"), cancellable = true)
    private void backhand$cancelRemoval(CallbackInfo ci) {
        if (BackhandClientUtils.countToCancel > 0) {
            BackhandClientUtils.countToCancel--;
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
