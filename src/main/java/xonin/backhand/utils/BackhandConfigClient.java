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

    @Config.Comment("Tweaks when holding a torch in the offhand")
    @Config.Name("Offhand Torch Tweaks")
    public static TorchConfig torchConfig = new TorchConfig();

    public static class TorchConfig {

        @Config.Comment("""
            These items will be count as torches
            Formatting of an item should be: modid:itemname
            These should all be placed on separate lines between the provided '<' and '>'.
            """)
        @Config.DefaultStringList({ "minecraft:torch", "minecraft:redstone_torch" })
        public String[] torch_items;

        @Config.Name("No Offhand Torch With Block")
        @Config.Comment("Don't place torches from the offhand if the main hand has a block that can be placed")
        @Config.DefaultBoolean(false)
        public boolean noTorchWithBlock;

        @Config.Name("No Offhand Torch With Empty Hand")
        @Config.Comment("Don't place torches from the offhand if the main hand is empty")
        @Config.DefaultBoolean(false)
        public boolean noTorchWithEmpty;

        @Config.Name("No Offhand Torch With Food")
        @Config.Comment("Don't place torches from the offhand if the main hand has food")
        @Config.DefaultBoolean(false)
        public boolean noTorchWithFood;

        @Config.Name("No Offhand Torch At All")
        @Config.Comment({ "Don't place torches from the offhand at all", "Note: This overrides all other options" })
        @Config.DefaultBoolean(false)
        public boolean noTorchAtAll;

        @Config.Name("Offhand Torch With Tool Only")
        @Config.Comment({ "Don't place torches from the offhand unless the main hand contains a tool",
            "Vanilla tools include swords, hoes, axes, and pickaxes",
            "Modded tools include Tinker's Construct tools (not weapons)" })
        @Config.DefaultBoolean(false)
        public boolean offhandTorchWithToolOnly;

        @Config.Name("Don't Use Last Offhand Torch")
        @Config.Comment("Don't place torches from the offhand if it is the last torch in your offhand")
        @Config.DefaultBoolean(false)
        public boolean noLastTorch;

    }
}
