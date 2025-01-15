package xonin.backhand.mixins.early.minecraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.management.ItemInWorldManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import xonin.backhand.api.core.IOffhandInventory;
import xonin.backhand.utils.BackhandConfig;

@Mixin(NetHandlerPlayServer.class)
public abstract class MixinNetHandlerPlayServer {

    @Shadow
    public EntityPlayerMP playerEntity;

    @Unique
    private boolean backhand$swappedOffhand = false;

    @ModifyExpressionValue(
        method = "processHeldItemChange",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/play/client/C09PacketHeldItemChange;func_149614_c()I",
            ordinal = 1))
    private int backhand$isValidInventorySlot(int original) {
        // return a valid int e.g. between 0 and < 9
        return IOffhandInventory.isValidSwitch(original) ? 0 : -1;
    }

    @WrapWithCondition(
        method = "processPlayerDigging",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/management/ItemInWorldManager;uncheckedTryHarvestBlock(III)V"))
    private boolean backhand$playerDigging(ItemInWorldManager instance, int l, int block, int i) {
        return BackhandConfig.OffhandBreakBlocks
            || playerEntity.inventory.currentItem != IOffhandInventory.OFFHAND_HOTBAR_SLOT;
    }

    @WrapWithCondition(
        method = "processUseEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayerMP;attackTargetEntityWithCurrentItem(Lnet/minecraft/entity/Entity;)V"))
    private boolean backhand$checkOffhandAttack(EntityPlayerMP instance, Entity entity) {
        return BackhandConfig.OffhandAttack
            || playerEntity.inventory.currentItem != IOffhandInventory.OFFHAND_HOTBAR_SLOT;
    }
}
