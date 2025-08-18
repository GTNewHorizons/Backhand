package xonin.backhand.mixins.early.minecraft.containerfix;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.authlib.GameProfile;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.hooks.containerfix.IContainerHook;

@Mixin(EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP extends EntityPlayer {

    public MixinEntityPlayerMP(World p_i45324_1_, GameProfile p_i45324_2_) {
        super(p_i45324_1_, p_i45324_2_);
    }

    @Redirect(
        method = "onUpdate",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Container;detectAndSendChanges()V"))
    private void backhand$detectAndSendChanges_Vanilla(Container instance) {
        if (((IContainerHook) instance).backhand$wasOpenedWithOffhand()) {
            int currentItem = BackhandUtils.swapToOffhand(this);
            instance.detectAndSendChanges();
            BackhandUtils.swapBack(this, currentItem);
        } else {
            instance.detectAndSendChanges();
        }
    }

    // PlayerAPI shenanigans
    @Redirect(
        method = "localOnUpdate",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Container;detectAndSendChanges()V"),
        remap = false)
    private void backhand$detectAndSendChanges_PlayerAPI(Container instance) {
        if (((IContainerHook) instance).backhand$wasOpenedWithOffhand()) {
            int currentItem = BackhandUtils.swapToOffhand(this);
            instance.detectAndSendChanges();
            BackhandUtils.swapBack(this, currentItem);
        } else {
            instance.detectAndSendChanges();
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
