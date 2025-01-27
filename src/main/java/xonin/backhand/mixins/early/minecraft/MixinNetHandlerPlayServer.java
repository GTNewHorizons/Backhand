package xonin.backhand.mixins.early.minecraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.server.management.ItemInWorldManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.IOffhandInventory;
import xonin.backhand.packet.BackhandPacketHandler;
import xonin.backhand.packet.OffhandCancelUsage;
import xonin.backhand.utils.BackhandConfig;

@Mixin(NetHandlerPlayServer.class)
public abstract class MixinNetHandlerPlayServer {

    @Shadow
    public EntityPlayerMP playerEntity;

    @ModifyExpressionValue(
        method = "processHeldItemChange",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/play/client/C09PacketHeldItemChange;func_149614_c()I",
            ordinal = 1))
    private int backhand$isValidInventorySlot(int original) {
        // return a valid int e.g. between 0 and < 9
        return IOffhandInventory.isValidSwitch(original, playerEntity) ? 0 : -1;
    }

    @WrapWithCondition(
        method = "processPlayerDigging",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/management/ItemInWorldManager;uncheckedTryHarvestBlock(III)V"))
    private boolean backhand$playerDigging(ItemInWorldManager instance, int l, int block, int i) {
        return BackhandConfig.OffhandBreakBlocks || !BackhandUtils.isUsingOffhand(playerEntity);
    }

    @Inject(
        method = "processPlayerBlockPlacement",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/event/ForgeEventFactory;onPlayerInteract(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraftforge/event/entity/player/PlayerInteractEvent$Action;IIIILnet/minecraft/world/World;)Lnet/minecraftforge/event/entity/player/PlayerInteractEvent;",
            remap = false),
        cancellable = true)
    private void backhand$itemUse(C08PacketPlayerBlockPlacement packetIn, CallbackInfo ci) {
        if (BackhandUtils.isUsingOffhand(playerEntity)
            && playerEntity.openContainer != playerEntity.inventoryContainer) {
            // Client-side might still think it's using the offhand item, so we need to cancel the usage
            if (!playerEntity.isUsingItem()) {
                BackhandPacketHandler.sendPacketToPlayer(new OffhandCancelUsage(), playerEntity);
            }
            ci.cancel();
        }
    }

    @WrapWithCondition(
        method = "processUseEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayerMP;attackTargetEntityWithCurrentItem(Lnet/minecraft/entity/Entity;)V"))
    private boolean backhand$checkOffhandAttack(EntityPlayerMP instance, Entity entity) {
        return BackhandConfig.OffhandAttack || !BackhandUtils.isUsingOffhand(playerEntity);
    }
}
