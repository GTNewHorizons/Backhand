package xonin.backhand.client;

import net.minecraft.client.gui.GuiScreen;

import com.gtnewhorizon.gtnhlib.config.SimpleGuiFactory;

public class BackhandGuiFactory implements SimpleGuiFactory {

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return BackhandGuiConfig.class;
    }
}
