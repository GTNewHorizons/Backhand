package xonin.backhand.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import cpw.mods.fml.client.FMLClientHandler;
import xonin.backhand.Backhand;
import xonin.backhand.client.gui.controls.GuiToggleButton;
import xonin.backhand.utils.BackhandConfig;

public class BackhandConfigGui extends GuiScreen {

    private final GuiScreen parent;

    public BackhandConfigGui(GuiScreen parent) {
        this.parent = parent;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initGui() {
        this.buttonList.add(new GuiButton(1, this.width / 2 - 75, this.height - 38, I18n.format("gui.done")));
        this.buttonList.add(
            new GuiToggleButton(
                10,
                this.width / 2 - 75,
                this.height / 2 - 12,
                I18n.format("backhandconfig.offhandRest") + ":" + Backhand.RenderEmptyOffhandAtRest,
                this.fontRendererObj));

        for (Object obj : this.buttonList) {
            ((GuiButton) obj).xPosition = this.width / 2 - ((GuiButton) obj).getButtonWidth() / 2;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            if (button.id == 1) {
                FMLClientHandler.instance()
                    .showGuiScreen(parent);
            }
            if (button.id == 10) {
                Backhand.RenderEmptyOffhandAtRest = !Backhand.RenderEmptyOffhandAtRest;
            }
            if (button instanceof GuiToggleButton) {
                ((GuiToggleButton) button).toggleDisplayString();
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawGradientRect(0, 40, this.width, this.height - 60, -1072689136, -804253680);
        super.drawScreen(mouseX, mouseY, partialTicks);
        String configTitle = I18n.format("backhandconfig.title");
        this.fontRendererObj.drawString(
            configTitle,
            this.width / 2 - this.fontRendererObj.getStringWidth(configTitle) / 2,
            20,
            0xFFFFFF);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        BackhandConfig.refreshConfig();
    }
}
