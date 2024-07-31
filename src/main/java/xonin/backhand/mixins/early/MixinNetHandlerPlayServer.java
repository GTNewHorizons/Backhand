package xonin.backhand.mixins.early;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.server.management.ItemInWorldManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.InventoryPlayerBackhand;

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
        return InventoryPlayerBackhand.isValidSwitch(original) ? 0 : -1;
    }

    @Inject(
        method = "processPlayerDigging",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/management/ItemInWorldManager;uncheckedTryHarvestBlock(III)V"))
    private void backhand$playerDigging(C07PacketPlayerDigging packetIn, CallbackInfo ci) {
        ItemInWorldManager instance = playerEntity.theItemInWorldManager;
        int x = packetIn.func_149505_c();
        int y = packetIn.func_149503_d();
        int z = packetIn.func_149502_e();
        instance.theWorld.destroyBlockInWorldPartially(instance.thisPlayerMP.getEntityId(), x, y, z, -1);
        instance.tryHarvestBlock(x, y, z);
    }

    @Inject(method = "processUseEntity", at = @At("HEAD"))
    private void backhand$hotswapOnEntityInteract(C02PacketUseEntity packetIn, CallbackInfo ci) {
        if (backhand$shouldSwapOffhand(packetIn.func_149565_c())) {
            BackhandUtils.swapOffhandItem(playerEntity);
            backhand$swappedOffhand = true;
        }
    }

    @Inject(
        method = "processUseEntity",
        at = {
            @At(
                value = "INVOKE",
                target = "Lnet/minecraft/network/NetHandlerPlayServer;kickPlayerFromServer(Ljava/lang/String;)V"),
            @At(value = "TAIL") })
    private void backhand$swapBackPostEntityInteract(C02PacketUseEntity packetIn, CallbackInfo ci) {
        if (backhand$swappedOffhand) {
            BackhandUtils.swapOffhandItem(playerEntity);
        }
        backhand$swappedOffhand = false;
    }

    @Inject(method = "sendPacket", at = @At(value = "HEAD"), cancellable = true)
    private void backhand$skipSetSlotOnHotswap(Packet packetIn, CallbackInfo ci) {
        if (packetIn instanceof S2FPacketSetSlot setSlotPacket) {
            if (backhand$shouldSkipSetSlot(setSlotPacket)) {
                ci.cancel();
            }
        }
    }

    @Unique
    private boolean backhand$shouldSwapOffhand(C02PacketUseEntity.Action action) {
        return !playerEntity.isUsingItem()
            && BackhandUtils.checkForRightClickFunction(BackhandUtils.getOffhandItem(playerEntity))
            && !BackhandUtils.checkForRightClickFunction(playerEntity.getCurrentEquippedItem())
            && action == C02PacketUseEntity.Action.INTERACT;
    }

    @Unique
    private boolean backhand$shouldSkipSetSlot(S2FPacketSetSlot setSlotPacket) {
        int packetWindowId = setSlotPacket.field_149179_a;
        int packetSlot = setSlotPacket.field_149177_b;
        ItemStack packetStack = setSlotPacket.field_149178_c;
        ItemStack offhandStack = BackhandUtils.getOffhandItem(playerEntity);
        return offhandStack != null && BackhandUtils.getOffhandEP(playerEntity).ignoreSetSlot
            && packetStack != null
            && packetSlot == BackhandUtils.getOffhandEP(playerEntity).activeSlot
            && packetStack.getItem() == offhandStack.getItem()
            && packetWindowId != -1;
    }
}
