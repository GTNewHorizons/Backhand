package xonin.backhand.utils;

import com.gtnewhorizon.gtnhlib.config.Config;
import xonin.backhand.Backhand;


@Config(modid = Backhand.MODID, category = "client")
@Config.Comment("Configs that only affect the client and have no change on the server")
public class BackhandConfigClient {
    @Config.Comment("If set to false, an empty offhand will only be rendered when the player is punching with the offhand.")
    @Config.DefaultBoolean(false)
    public static boolean RenderEmptyOffhandAtRest;
}
