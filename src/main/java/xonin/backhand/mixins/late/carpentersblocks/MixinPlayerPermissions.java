package xonin.backhand.mixins.late.carpentersblocks;

import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.carpentersblocks.util.protection.IProtected;
import com.carpentersblocks.util.protection.PlayerPermissions;

import xonin.backhand.client.world.ClientFakePlayer;

@Mixin(value = PlayerPermissions.class, remap = false)
public abstract class MixinPlayerPermissions {

    @Inject(method = "hasElevatedPermission", at = @At(value = "HEAD"), cancellable = true)
    private static void backhand$fixFakePlayerNPE(IProtected object, EntityPlayer entityPlayer,
        boolean enforceOwnership, CallbackInfoReturnable<Boolean> cir) {
        if (entityPlayer instanceof ClientFakePlayer) {
            cir.setReturnValue(true);
        }
    }
}
