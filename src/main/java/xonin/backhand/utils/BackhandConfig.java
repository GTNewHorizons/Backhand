package xonin.backhand.utils;

import java.util.Arrays;

public class BackhandConfig {

    private static Configuration file;
    public static String[] blacklistedItems = new String[0];
    private static String[] comments = new String[1];

    public static void getConfig(Configuration config) {
        file = config;

        loadConfigFields(config);

        file.getConfigFile()
            .delete();
        file.load();

        loadConfigFields(file);

        file.save();
    }

    public static void loadConfigFields(Configuration config) {
        file = config;
        StringBuilder sb;
        String category = Configuration.CATEGORY_GENERAL;

        /*
         * =============================================================================================================
         * =
         * GENERAL CONFIGS
         * ============================================================================================================
         */
        sb = new StringBuilder();
        sb.append(
            "If set to false, an empty offhand will only be rendered when the player is punching with the offhand. False in vanilla.");
        Backhand.OffhandAttack = config.get(category, "Attack with offhand", Backhand.OffhandAttack, sb.toString())
            .getBoolean();

        sb = new StringBuilder();
        sb.append(
            "If set to false, disables offhand actions and rendering if there is no offhand item. False in vanilla.");
        Backhand.EmptyOffhand = config.get(category, "Allow empty offhand", Backhand.EmptyOffhand, sb.toString())
            .getBoolean();

        sb = new StringBuilder();
        sb.append("Determines whether you can break blocks with the offhand or not. False in vanilla.");
        Backhand.OffhandBreakBlocks = config
            .get(category, "Offhand breaks blocks", Backhand.OffhandBreakBlocks, sb.toString())
            .getBoolean();

        sb = new StringBuilder();
        sb.append(
            "If enabled, arrows in the offhand will be used first when shooting a bow. Compatible with Et-Futurum's tipped arrows! True in vanilla.");
        Backhand.UseOffhandArrows = config.get(category, "Use offhand arrows", Backhand.UseOffhandArrows, sb.toString())
            .getBoolean();

        sb = new StringBuilder();
        sb.append("If enabled, bows can be used in the offhand. True in vanilla.");
        Backhand.UseOffhandBow = config.get(category, "Use offhand bow", Backhand.UseOffhandBow, sb.toString())
            .getBoolean();

        sb = new StringBuilder();
        sb.append(
            "If the main offhand inventory can't be used, this slot in the main inventory will be used as the offhand instead. Slot 9 by default.");
        Backhand.AlternateOffhandSlot = config
            .get(category, "Alternate Inventory Slot", Backhand.AlternateOffhandSlot, sb.toString())
            .getInt();

        sb = new StringBuilder();
        sb.append(
            "If enabled, the alternate offhand slot configured above will always be used for the offhand. False by default.");
        Backhand.UseInventorySlot = config.get(category, "Use inventory slot", Backhand.UseInventorySlot, sb.toString())
            .getBoolean();

        sb = new StringBuilder();
        sb.append("If enabled, a hotswap will be performed every tick if the main hand has no use or is empty.\n");
        sb.append(
            "This hotswap allows for many more items like fishing rods to be used in the offhand, but may be unstable.");
        Backhand.OffhandTickHotswap = config
            .get(category, "Offhand Tick Hotswap", Backhand.OffhandTickHotswap, sb.toString())
            .getBoolean();

        sb = new StringBuilder();
        sb.append("These items will be unable to be swapped into the offhand.\n");
        sb.append("Formatting of an item should be: modid:itemname\n");
        sb.append("These should all be placed on separate lines between the provided \'<\' and \'>\'.");
        blacklistedItems = config.get(category, "Blacklisted items", new String[0], sb.toString())
            .getStringList();
        Arrays.sort(blacklistedItems);
        Backhand.offhandBlacklist = blacklistedItems;

        /*
         * =============================================================================================================
         * =
         * RENDERING CONFIGS
         * ============================================================================================================
         */
        category = "Rendering";
        config.addCustomCategoryComment(
            category,
            "This category is client side, you don't have to sync its values with server in multiplayer.");
        sb = new StringBuilder();
        sb.append(
            "If set to false, an empty offhand will only be rendered when the player is punching with the offhand.");
        comments[0] = sb.toString();
        Backhand.RenderEmptyOffhandAtRest = config
            .get(category, "Render empty offhand at rest", Backhand.RenderEmptyOffhandAtRest, comments[0])
            .getBoolean();

        file.save();
    }

    public static void refreshConfig() {
        try {
            file.get("Rendering", "Render empty offhand at rest", new String[0], comments[0])
                .set(Backhand.RenderEmptyOffhandAtRest);
            file.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
