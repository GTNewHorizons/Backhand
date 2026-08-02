package xonin.backhand.mixins.early.minecraft;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xonin.backhand.Backhand;
import xonin.backhand.CommonProxy;
import xonin.backhand.api.core.BackhandUtils;

@Mixin(GuiContainer.class)
public abstract class MixinGuiContainer extends GuiScreen {

    @Shadow
    private Slot theSlot;

    @Shadow
    protected abstract void handleMouseClick(Slot slotIn, int slotId, int clickedButton, int clickType);

    @Inject(method = "keyTyped", at = @At("HEAD"), cancellable = true)
    private void backhand$swapHoveredStackToOffhand(char typedChar, int keyCode, CallbackInfo ci) {
        if (keyCode != CommonProxy.SWAP_KEY.getKeyCode() || mc.thePlayer.inventory.getItemStack() != null
            || theSlot == null
            || (!theSlot.getHasStack() && BackhandUtils.getOffhandItem(mc.thePlayer) == null)) {
            return;
        }

        ItemStack stack = theSlot.getStack();
        if (Backhand.isOffhandBlacklisted(stack)) {
            return;
        }

        handleMouseClick(theSlot, theSlot.slotNumber, BackhandUtils.getOffhandSlot(mc.thePlayer), 2);
        ci.cancel();
    }
}
