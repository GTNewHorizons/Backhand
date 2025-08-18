package xonin.backhand.mixins.early.minecraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.server.management.ItemInWorldManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.IOffhandInventory;
import xonin.backhand.hooks.containerfix.IContainerHook;
import xonin.backhand.packet.BackhandPacketHandler;
import xonin.backhand.packet.OffhandCancelUsage;
import xonin.backhand.utils.BackhandConfig;

@Mixin(NetHandlerPlayServer.class)
public abstract class MixinNetHandlerPlayServer {

    @Shadow
    public EntityPlayerMP playerEntity;

    @Unique
    private boolean backhand$dropEntityInteraction = false;

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

    @Inject(
        method = "processPlayerBlockPlacement",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;areItemStacksEqual(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z"),
        cancellable = true)
    private void backhand$processPlayerBlockPlacement(CallbackInfo ci, @Local(name = "slot") Slot slot) {
        if (slot == null) {
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

    @WrapOperation(
        method = "processUseEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayerMP;interactWith(Lnet/minecraft/entity/Entity;)Z"))
    private boolean backhand$checkOffhandInteract(EntityPlayerMP instance, Entity entity, Operation<Boolean> original) {
        boolean result = false;
        if (BackhandUtils.isUsingOffhand(playerEntity)) {
            if (!backhand$dropEntityInteraction) {
                result = original.call(instance, entity);
            }
            backhand$dropEntityInteraction = false;
        } else {
            result = backhand$dropEntityInteraction = original.call(instance, entity);
        }
        return result;
    }

    // Backhand Containerfix
    @Redirect(
        method = "processClickWindow",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/inventory/Container;slotClick(IIILnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;"))
    public ItemStack backhand$windowClick(Container instance, int slotId, int clickedButton, int mode,
        EntityPlayer player) {
        if (((IContainerHook) instance).backhand$wasOpenedWithOffhand()) {
            BackhandUtils.swapToOffhand(player);
            ItemStack result = instance.slotClick(slotId, clickedButton, mode, player);
            BackhandUtils.swapBack(player);
            return result;
        } else {
            return instance.slotClick(slotId, clickedButton, mode, player);
        }
    }
}
