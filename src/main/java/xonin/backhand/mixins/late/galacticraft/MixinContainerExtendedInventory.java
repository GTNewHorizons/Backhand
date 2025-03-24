package xonin.backhand.mixins.late.galacticraft;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.llamalad7.mixinextras.sugar.Local;

import micdoodle8.mods.galacticraft.core.inventory.ContainerExtendedInventory;
import micdoodle8.mods.galacticraft.core.inventory.SlotArmorGC;

@Mixin(ContainerExtendedInventory.class)
public abstract class MixinContainerExtendedInventory extends Container {

    @Shadow(remap = false)
    public InventoryPlayer inventoryPlayer;

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lmicdoodle8/mods/galacticraft/core/inventory/ContainerExtendedInventory;addSlotToContainer(Lnet/minecraft/inventory/Slot;)Lnet/minecraft/inventory/Slot;",
            ordinal = 2))
    private Slot backhand$armorSlotFix(ContainerExtendedInventory instance, Slot slot, @Local(name = "i") int i) {
        return addSlotToContainer(
            new SlotArmorGC(
                inventoryPlayer.player,
                inventoryPlayer,
                inventoryPlayer.getSizeInventory() - 1 - i,
                61,
                8 + i * 18,
                i));
    }
}
