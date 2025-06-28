package xonin.backhand.utils;

import com.gtnewhorizon.gtnhlib.config.Config;

import xonin.backhand.Backhand;

@Config(modid = Backhand.MODID)
@Config.Comment("Configs that will be synced with the server's config if playing in multiplayer")
public class BackhandConfig {

    @Config.Comment("""
        These items will be unable to be swapped into the offhand.
        Formatting of an item should be: modid:itemname
        These should all be placed on separate lines between the provided '<' and '>'.
        """)
    @Config.DefaultStringList({})
    public static String[] offhandBlacklist;

    @Config.Comment("Picked up items can go into the offhand slot when empty. False in vanilla")
    @Config.DefaultBoolean(false)
    public static boolean OffhandPickup;
}
