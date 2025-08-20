package xonin.backhand.mixins.early.minecraft.containerfix;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.hooks.containerfix.IContainerHook;

@Mixin(EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP extends EntityPlayer {

    public MixinEntityPlayerMP(World p_i45324_1_, GameProfile p_i45324_2_) {
        super(p_i45324_1_, p_i45324_2_);
    }

    @Unique
    private int backhand$heldItemTemp;

    @Inject(method = "onUpdate", at = @At("HEAD"))
    private void backhand$onUpdatePre(CallbackInfo ci) {
        if (((IContainerHook) this.openContainer).backhand$wasOpenedWithOffhand()) {
            backhand$heldItemTemp = BackhandUtils.swapToOffhand(this);
        }
    }

    @Inject(method = "onUpdate", at = @At("RETURN"))
    private void backhand$onUpdatePost(CallbackInfo ci) {
        if (((IContainerHook) this.openContainer).backhand$wasOpenedWithOffhand()) {
            BackhandUtils.swapBack(this, backhand$heldItemTemp);
        }
    }

    @Redirect(
        method = "onItemPickup",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Container;detectAndSendChanges()V"))
    private void backhand$detectAndSendChanges2(Container instance) {
        if (((IContainerHook) instance).backhand$wasOpenedWithOffhand()) {
            int currentItem = BackhandUtils.swapToOffhand(this);
            instance.detectAndSendChanges();
            BackhandUtils.swapBack(this, currentItem);
        } else {
            instance.detectAndSendChanges();
        }
    }
}
