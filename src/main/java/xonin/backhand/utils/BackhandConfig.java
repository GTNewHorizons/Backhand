package xonin.backhand.utils;

import com.gtnewhorizon.gtnhlib.config.Config;

import xonin.backhand.Backhand;

@Config(modid = Backhand.MODID)
@Config.Comment("Configs that will be synced with the server's config if playing in multiplayer")
public class BackhandConfig {

    @Config.Comment("If set to false, an empty offhand will only be rendered when the player is punching with the offhand. False in vanilla.")
    @Config.DefaultBoolean(false)
    public static boolean OffhandAttack;

    @Config.Comment("If set to false, disables offhand actions and rendering if there is no offhand item. False in vanilla.")
    @Config.DefaultBoolean(false)
    public static boolean EmptyOffhand;

    @Config.Comment("Determines whether you can break blocks with the offhand or not. False in vanilla.")
    @Config.DefaultBoolean(false)
    public static boolean OffhandBreakBlocks;

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
