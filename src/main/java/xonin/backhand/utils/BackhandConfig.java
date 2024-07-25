package xonin.backhand.utils;

import java.util.Arrays;

import net.minecraftforge.common.config.Configuration;

import xonin.backhand.Backhand;

public class BackhandConfig {

    public static Configuration config;

    public static void getConfig(Configuration conf) {
        config = conf;
        config.load();
        loadConfigFields();
    }

    public static void loadConfigFields() {
        String category = Configuration.CATEGORY_GENERAL;

        /*
         * =============================================================================================================
         * =
         * GENERAL CONFIGS
         * ============================================================================================================
         */
        Backhand.OffhandAttack = config.get(
            category,
            "Attack with offhand",
            false,
            "If set to false, an empty offhand will only be rendered when the player is punching with the offhand. False in vanilla.")
            .getBoolean();

        Backhand.EmptyOffhand = config.get(
            category,
            "Allow empty offhand",
            false,
            "If set to false, disables offhand actions and rendering if there is no offhand item. False in vanilla.")
            .getBoolean();

        Backhand.OffhandBreakBlocks = config
            .get(
                category,
                "Offhand breaks blocks",
                false,
                "Determines whether you can break blocks with the offhand or not. False in vanilla.")
            .getBoolean();

        Backhand.UseOffhandArrows = config.get(
            category,
            "Use offhand arrows",
            true,
            "If enabled, arrows in the offhand will be used first when shooting a bow. Compatible with Et-Futurum's tipped arrows! True in vanilla.")
            .getBoolean();

        Backhand.UseOffhandBow = config
            .get(category, "Use offhand bow", true, "If enabled, bows can be used in the offhand. True in vanilla.")
            .getBoolean();

        Backhand.AlternateOffhandSlot = config.get(
            category,
            "Alternate Inventory Slot",
            9,
            "If the main offhand inventory can't be used, this slot in the main inventory will be used as the offhand instead. Slot 9 by default.")
            .getInt();

        Backhand.UseInventorySlot = config.get(
            category,
            "Use inventory slot",
            false,
            "If enabled, the alternate offhand slot configured above will always be used for the offhand. False by default.")
            .getBoolean();

        Backhand.OffhandTickHotswap = config.get(category, "Offhand Tick Hotswap", true, """
            If enabled, a hotswap will be performed every tick if the main hand has no use or is empty.
            This hotswap allows for many more items like fishing rods to be used in the offhand, but may be unstable.
            """)
            .getBoolean();

        Backhand.offhandBlacklist = config.get(category, "Blacklisted items", new String[0], """
            These items will be unable to be swapped into the offhand.
            Formatting of an item should be: modid:itemname
            These should all be placed on separate lines between the provided '<' and '>'.
            """)
            .getStringList();
        Arrays.sort(Backhand.offhandBlacklist);

        /*
         * =============================================================================================================
         * =
         * RENDERING CONFIGS
         * ============================================================================================================
         */
        category = "rendering";
        config.addCustomCategoryComment(
            category,
            "This category is client side, you don't have to sync its values with server in multiplayer.");
        Backhand.RenderEmptyOffhandAtRest = config
            .get(
                category,
                "Render empty offhand at rest",
                false,
                "If set to false, an empty offhand will only be rendered when the player is punching with the offhand.")
            .getBoolean();

        config.save();
    }

    public static void refreshConfig() {
        try {
            config.getCategory("rendering")
                .get("Render empty offhand at rest")
                .set(Backhand.RenderEmptyOffhandAtRest);
            config.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
