package xonin.backhand.mixins.early.minecraft;

import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.inventory.Container;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xonin.backhand.api.core.BackhandSlot;
import xonin.backhand.client.utils.BackhandRenderHelper;
import xonin.backhand.utils.BackhandConfigClient;

@Mixin(GuiInventory.class)
public abstract class MixinGuiInventory extends InventoryEffectRenderer {

    public MixinGuiInventory(Container p_i1089_1_) {
        super(p_i1089_1_);
    }

    @Inject(method = "drawGuiContainerBackgroundLayer", at = @At("TAIL"))
    protected void backhand$drawOffhandSlot(float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        // The slot is created once when joining a world, so keep it in sync if the config changes afterwards
        for (Object slotObject : inventorySlots.inventorySlots) {
            if (slotObject instanceof BackhandSlot offhandSlot) {
                offhandSlot.xDisplayPosition = 80 + BackhandConfigClient.offhandInventorySlotXOffset;
                offhandSlot.yDisplayPosition = 65 + BackhandConfigClient.offhandInventorySlotYOffset;
            }
        }
        BackhandRenderHelper.drawItemStackSlot(
            guiLeft + 78 + BackhandConfigClient.offhandInventorySlotXOffset,
            guiTop + 63 + BackhandConfigClient.offhandInventorySlotYOffset);
    }
}
