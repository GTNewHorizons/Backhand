package xonin.backhand.mixins.late.bibliocraft;

import net.minecraft.entity.player.InventoryPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import jds.bibliocraft.blocks.ContainerArmor;

@Mixin(ContainerArmor.class)
public class MixinContainerArmor {

    @ModifyConstant(
        method = "bindPlayerInventory",
        constant = { @Constant(intValue = 39), @Constant(intValue = 38), @Constant(intValue = 37),
            @Constant(intValue = 36), },
        remap = false)
    private int backhand$bindPlayerInventory(int constant, InventoryPlayer inventoryPlayer) {
        return inventoryPlayer.getSizeInventory() - 1 - (39 - constant);
    }
}
