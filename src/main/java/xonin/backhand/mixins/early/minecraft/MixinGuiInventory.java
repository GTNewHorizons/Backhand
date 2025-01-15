package xonin.backhand.mixins.early.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.inventory.Container;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiInventory.class)
public abstract class MixinGuiInventory extends InventoryEffectRenderer {

    public MixinGuiInventory(Container p_i1089_1_) {
        super(p_i1089_1_);
    }

    @Inject(method = "drawGuiContainerBackgroundLayer", at = @At("TAIL"))
    protected void backhand$drawOffhandSlot(float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        backhand$drawItemStackSlot(guiLeft + 78, guiTop + 63);
    }

    @Unique
    private static void backhand$drawItemStackSlot(int x, int y) {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(Gui.statIcons);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x + 1, y + 1 + 18, 0, 0 * 0.0078125f, 18 * 0.0078125f);
        tessellator.addVertexWithUV(x + 1 + 18, y + 1 + 18, 0, 18 * 0.0078125f, 18 * 0.0078125f);
        tessellator.addVertexWithUV(x + 1 + 18, y + 1, 0, 18 * 0.0078125f, 0 * 0.0078125f);
        tessellator.addVertexWithUV(x + 1, y + 1, 0, 0 * 0.0078125f, 0 * 0.0078125f);
        tessellator.draw();
    }
}
