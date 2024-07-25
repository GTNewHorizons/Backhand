package xonin.backhand.mixins.early;

import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.management.ItemInWorldManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import xonin.backhand.api.core.InventoryPlayerBackhand;

@Mixin(NetHandlerPlayServer.class)
public abstract class MixinNetHandlerPlayServer {

    @ModifyExpressionValue(
        method = "processHeldItemChange",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/play/client/C09PacketHeldItemChange;func_149614_c()I",
            ordinal = 1))
    private int backhand$isValidIventorySlot(int original) {
        // return a valid int e.g. between 0 and < 9
        return InventoryPlayerBackhand.isValidSwitch(original) ? 0 : -1;
    }

    // @WrapOperation(
    // method = "processPlayerBlockPlacement",
    // at = @At(
    // value = "INVOKE",
    // target =
    // "Lnet/minecraft/inventory/Container;getSlotFromInventory(Lnet/minecraft/inventory/IInventory;I)Lnet/minecraft/inventory/Slot;"))
    // private Slot battlegear2$captureSlotVariable(Container instance, IInventory j, int i, Operation<Slot> original,
    // @Share("slot") LocalRef<Slot> slotRef) {
    // Slot slot = original.call(instance, j, i);
    // slotRef.set(slot);
    // return slot;
    // }
    //
    // @Inject(
    // method = "processPlayerBlockPlacement",
    // at = @At(
    // value = "FIELD",
    // target = "Lnet/minecraft/entity/player/EntityPlayerMP;isChangingQuantityOnly:Z",
    // shift = At.Shift.AFTER,
    // ordinal = 1),
    // cancellable = true)
    // private void battlegear2$fixNPE(C08PacketPlayerBlockPlacement packetIn, CallbackInfo ci,
    // @Share("slot") LocalRef<Slot> slotRef) {
    // if (slotRef.get() == null) {
    // ci.cancel();
    // }
    // }

    @Redirect(
        method = "processPlayerDigging",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/management/ItemInWorldManager;uncheckedTryHarvestBlock(III)V"))
    private void backhand$playerDigging(ItemInWorldManager instance, int x, int y, int z) {
        instance.theWorld.destroyBlockInWorldPartially(instance.thisPlayerMP.getEntityId(), x, y, z, -1);
        instance.tryHarvestBlock(x, y, z);
    }
}
