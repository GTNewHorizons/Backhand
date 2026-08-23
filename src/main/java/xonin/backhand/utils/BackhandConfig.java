package xonin.backhand.utils;

import com.gtnewhorizon.gtnhlib.config.Config;

import xonin.backhand.Backhand;

@Config(modid = Backhand.MODID)
@Config.Comment("Configs that will be synced with the server's config if playing in multiplayer")
public class BackhandConfig {

    @Config.Sync
    @Config.Comment("If set to false, an empty offhand will only be rendered when the player is punching with the offhand. False in vanilla.")
    @Config.DefaultBoolean(false)
    public static boolean OffhandAttack;

    @Config.Sync
    @Config.Comment("If set to false, disables offhand actions and rendering if there is no offhand item. False in vanilla.")
    @Config.DefaultBoolean(false)
    public static boolean EmptyOffhand;

    @Config.Sync
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

    @Config.Sync
    @Config.Comment("""
        Main hand items listed here stop Backhand from trying the offhand fallback after right click.
        This does not stop the item from being held in the offhand; use offhandBlacklist for that.
        Only use this for items that always act unconditionally on right click (e.g. always opening a GUI):
        tools whose action depends on the targeted block (e.g. a wrench) are already detected generically
        and should NOT be added here, since this always blocks the offhand regardless of what was targeted.
        Formatting of an item should be: modid:itemname, or modid:itemname:damage for one damage value only.
        """)
    @Config.DefaultStringList({ "matter-manipulator:itemMatterManipulator0",
        "matter-manipulator:itemMatterManipulator1", "matter-manipulator:itemMatterManipulator2",
        "matter-manipulator:itemMatterManipulator3", "Forestry:beealyzer", "Forestry:solderingIron",
        "Forestry:apiaristBag", "Forestry:lepidopteristBag", "Forestry:minerBag", "Forestry:minerBagT2",
        "Forestry:diggerBag", "Forestry:diggerBagT2", "Forestry:foresterBag", "Forestry:foresterBagT2",
        "Forestry:hunterBag", "Forestry:hunterBagT2", "Forestry:adventurerBag", "Forestry:adventurerBagT2",
        "Forestry:builderBag", "Forestry:builderBagT2", "Forestry:coinBag", "Forestry:coinBagT2",
        "DraconicEvolution:magnet", "appliedenergistics2:item.ToolNetworkTool",
        "appliedenergistics2:item.ToolAdvancedNetworkTool" })
    public static String[] mainhandUseStopsOffhandFallback;

    @Config.Comment("Picked up items can go into the offhand slot when empty. False in vanilla")
    @Config.DefaultBoolean(false)
    public static boolean OffhandPickup;
}
