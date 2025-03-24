package xonin.backhand.mixins.late.terrafirmacraft;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.client.utils.BackhandRenderHelper;
import xonin.backhand.utils.Mods;

@Mixin(
    value = { com.bioxx.tfc.Core.Player.PlayerInventory.class, com.dunk.tfc.Core.Player.PlayerInventory.class },
    remap = false)
public abstract class MixinPlayerInventory {

    @Unique
    private static final int SLOT_X_POSITION = Mods.TFCPLUS.isLoaded() ? 8 : 152;
    @Unique
    private static final int SLOT_Y_POSITION = Mods.TFCPLUS.isLoaded() ? 62 : 65;

    @Shadow
    protected static Slot addSlotToContainer(Container container, Slot par1Slot) {
        return null;
    }

    @ModifyConstant(method = "addExtraEquipables", constant = @Constant(intValue = 36))
    private static int backhand$armorSlotFix(int constant, @Local(argsOnly = true) InventoryPlayer inventory) {
        return inventory.mainInventory.length;
    }

    @Inject(
        method = "buildInventoryLayout(Lnet/minecraft/inventory/Container;Lnet/minecraft/entity/player/InventoryPlayer;IIZZ)V",
        at = @At("TAIL"))
    private static void backhand$armorSlotFix(Container container, InventoryPlayer inventory, int x, int y,
        boolean freezeSlot, boolean toolBarAfterMainInv, CallbackInfo ci) {
        // noinspection ResultOfMethodCallIgnored
        addSlotToContainer(
            container,
            new Slot(inventory, BackhandUtils.getOffhandSlot(inventory.player), SLOT_X_POSITION, SLOT_Y_POSITION));
    }

    @Inject(method = "drawInventory", at = @At("TAIL"))
    private static void backhand$renderOffhandSlot(GuiContainer container, int screenWidth, int screenHeight,
        int upperGuiHeight, CallbackInfo ci) {
        BackhandRenderHelper
            .drawItemStackSlot(container.guiLeft + SLOT_X_POSITION - 2, container.guiTop + SLOT_Y_POSITION - 2);
    }
}
