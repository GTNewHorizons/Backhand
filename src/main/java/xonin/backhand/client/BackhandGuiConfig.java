package xonin.backhand.client;

import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.SimpleGuiConfig;
import net.minecraft.client.gui.GuiScreen;
import xonin.backhand.Backhand;
import xonin.backhand.utils.BackhandConfig;
import xonin.backhand.utils.BackhandConfigClient;

public class BackhandGuiConfig extends SimpleGuiConfig {
    public BackhandGuiConfig(GuiScreen parent) throws ConfigException {
        super(parent, Backhand.MODID, "Backhand", true, BackhandConfig.class, BackhandConfigClient.class);
    }
}
