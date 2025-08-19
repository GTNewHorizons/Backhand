package xonin.backhand.mixins.early.minecraft.containerfix;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.authlib.GameProfile;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.hooks.containerfix.IContainerHook;

@Pseudo
@Mixin(value = EntityPlayerMP.class, priority = 9999999)
public abstract class MixinEntityPlayerMP_PlayerAPI extends EntityPlayer {

    public MixinEntityPlayerMP_PlayerAPI(World p_i45324_1_, GameProfile p_i45324_2_) {
        super(p_i45324_1_, p_i45324_2_);
    }

    @Dynamic("Target gets added by PlayerAPI")
    @Redirect(
        method = "localOnUpdate",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Container;detectAndSendChanges()V"))
    private void backhand$detectAndSendChanges_PlayerAPI(Container instance) {
        if (((IContainerHook) instance).backhand$wasOpenedWithOffhand()) {
            int currentItem = BackhandUtils.swapToOffhand(this);
            instance.detectAndSendChanges();
            BackhandUtils.swapBack(this, currentItem);
        } else {
            instance.detectAndSendChanges();
        }
    }
}
