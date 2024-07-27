package xonin.backhand.mixins.early;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.server.management.ItemInWorldManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.InventoryPlayerBackhand;

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
        return InventoryPlayerBackhand.isValidSwitch(original) ? 0 : -1;
    }

    @Redirect(
        method = "processPlayerDigging",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/management/ItemInWorldManager;uncheckedTryHarvestBlock(III)V"))
    private void backhand$playerDigging(ItemInWorldManager instance, int x, int y, int z) {
        instance.theWorld.destroyBlockInWorldPartially(instance.thisPlayerMP.getEntityId(), x, y, z, -1);
        instance.tryHarvestBlock(x, y, z);
    }

    @Inject(
        method = "processUseEntity",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;func_143004_u()V"))
    private void backhand$swapOnProcess(C02PacketUseEntity packetIn, CallbackInfo ci) {
        if (backhand$swapOffhand(packetIn.func_149565_c())) {
            BackhandUtils.swapOffhandItem(playerEntity);
        }
    }

    @Inject(
        method = "processUseEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/NetHandlerPlayServer;kickPlayerFromServer(Ljava/lang/String;)V"))
    private void backhand$swapOnKick(C02PacketUseEntity packetIn, CallbackInfo ci) {
        if (backhand$swapOffhand(packetIn.func_149565_c())) {
            BackhandUtils.swapOffhandItem(playerEntity);
        }
    }

    @Unique
    private boolean backhand$swapOffhand(C02PacketUseEntity.Action action) {
        return BackhandUtils.checkForRightClickFunction(BackhandUtils.getOffhandItem(playerEntity))
            && !BackhandUtils.checkForRightClickFunction(playerEntity.getCurrentEquippedItem())
            && action == C02PacketUseEntity.Action.INTERACT;
    }
}
