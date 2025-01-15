package xonin.backhand.mixins.early.minecraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import xonin.backhand.Backhand;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.packet.OffhandSyncItemPacket;

@Mixin(EntityTrackerEntry.class)
public abstract class MixinEntityTrackerEntry {

    @Shadow
    public Entity myEntity;

    @Inject(
        method = "tryStartWachingThis",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/EntityLivingBase;getEquipmentInSlot(I)Lnet/minecraft/item/ItemStack;"))
    private void backhand$syncOffhand(EntityPlayerMP receivingPlayer, CallbackInfo ci, @Local(name = "i") int i) {
        if (!(myEntity instanceof EntityPlayerMP playerMP) || i > 0) return;
        ItemStack offhand = BackhandUtils.getOffhandItem(playerMP);
        if (offhand != null) {
            Backhand.packetHandler
                .sendPacketToPlayer(new OffhandSyncItemPacket(playerMP).generatePacket(), receivingPlayer);
        }
    }
}
