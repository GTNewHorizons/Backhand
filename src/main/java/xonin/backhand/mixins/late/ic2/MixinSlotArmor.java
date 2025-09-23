package xonin.backhand.mixins.late.ic2;

import net.minecraft.entity.player.InventoryPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import ic2.core.slot.SlotArmor;

@Mixin(SlotArmor.class)
public class MixinSlotArmor {

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 36), remap = false)
    private static int backhand$init(int constant, InventoryPlayer inventoryPlayer) {
        return inventoryPlayer.getSizeInventory() - 1;
    }

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 3), remap = false)
    private static int backhand$init2(int constant, InventoryPlayer inventoryPlayer) {
        return 0;
    }
}
