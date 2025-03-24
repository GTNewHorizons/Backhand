package xonin.backhand.utils;

import com.gtnewhorizon.gtnhlib.config.Config;

import xonin.backhand.Backhand;

@Config(modid = Backhand.MODID, category = "client")
@Config.Comment("Configs that only affect the client and have no change on the server")
public class BackhandConfigClient {

    @Config.Comment("If set to false, an empty offhand will only be rendered when the player is punching with the offhand.")
    @Config.DefaultBoolean(false)
    public static boolean RenderEmptyOffhandAtRest;

    @Config.Comment("If set to false, the offhand hotbar slot will only be rendered when the offhand is not empty.")
    @Config.DefaultBoolean(false)
    public static boolean RenderOffhandHotbarSlotWhenEmpty;

    @Config.RangeInt(min = -2000, max = 2000)
    @Config.DefaultInt(0)
    public static int offhandHotbarSlotXOffset;

    @Config.RangeInt(min = 0, max = 1000)
    @Config.DefaultInt(0)
    public static int offhandHotbarSlotYOffset;
}
