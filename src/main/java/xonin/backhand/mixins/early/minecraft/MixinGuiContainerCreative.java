package xonin.backhand.mixins.early.minecraft;

import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.Container;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import xonin.backhand.client.utils.BackhandRenderHelper;

@Mixin(GuiContainerCreative.class)
public abstract class MixinGuiContainerCreative extends InventoryEffectRenderer {

    @Unique
    private static final int OFFHAND_SLOT_X = 90;
    @Unique
    private static final int OFFHAND_SLOT_Y = 6;

    public MixinGuiContainerCreative(Container container) {
        super(container);
    }

    /**
     * The vanilla "inventory" tab lays out slots by index assuming a fixed 45-slot layout, which would place our
     * appended offhand slot on top of the first hotbar slot - so give it its own spot next to the armor slots.
     */
    @Inject(
        method = "setCurrentCreativeTab",
        at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 1))
    protected void backhand$repositionOffhandSlot(CreativeTabs p_147050_1_, CallbackInfo ci,
        @Local GuiContainerCreative.ContainerCreative containercreative) {
        GuiContainerCreative.CreativeSlot slot = (GuiContainerCreative.CreativeSlot) containercreative.inventorySlots
            .get(containercreative.inventorySlots.size() - 1);
        slot.xDisplayPosition = OFFHAND_SLOT_X;
        slot.yDisplayPosition = OFFHAND_SLOT_Y;
    }

    // Draws the slot's background square, only reached when the "inventory" tab (with the player preview) is open
    @Inject(
        method = "drawGuiContainerBackgroundLayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/inventory/GuiInventory;func_147046_a(IIIFFLnet/minecraft/entity/EntityLivingBase;)V",
            shift = Shift.AFTER))
    private void backhand$drawOffhandSlot(float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        BackhandRenderHelper.drawItemStackSlot(guiLeft + OFFHAND_SLOT_X - 2, guiTop + OFFHAND_SLOT_Y - 2);
    }
}
