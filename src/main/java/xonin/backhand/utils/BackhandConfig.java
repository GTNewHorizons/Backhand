package xonin.backhand.utils;

import com.gtnewhorizon.gtnhlib.config.Config;

import xonin.backhand.Backhand;

@Config(modid = Backhand.MODID)
public class BackhandConfig {

    public static General general = new General();
    public static Client client = new Client();

    public static class General {

        @Config.Comment("If set to false, an empty offhand will only be rendered when the player is punching with the offhand. False in vanilla.")
        @Config.DefaultBoolean(false)
        public boolean OffhandAttack;

        @Config.Comment("If set to false, disables offhand actions and rendering if there is no offhand item. False in vanilla.")
        @Config.DefaultBoolean(false)
        public boolean EmptyOffhand;

        @Config.Comment("Determines whether you can break blocks with the offhand or not. False in vanilla.")
        @Config.DefaultBoolean(false)
        public boolean OffhandBreakBlocks;

        @Config.Comment("If enabled, arrows in the offhand will be used first when shooting a bow. Compatible with Et-Futurum's tipped arrows! True in vanilla.")
        @Config.DefaultBoolean(true)
        public boolean UseOffhandArrows;

        @Config.Comment("If enabled, bows can be used in the offhand. True in vanilla.")
        @Config.DefaultBoolean(true)
        public boolean UseOffhandBow;

        @Config.Comment("If the main offhand inventory can't be used, this slot in the main inventory will be used as the offhand instead. Slot 9 by default.")
        @Config.DefaultInt(9)
        public int AlternateOffhandSlot;

        @Config.Comment("If enabled, the alternate offhand slot configured above will always be used for the offhand. False by default.")
        @Config.DefaultBoolean(false)
        public boolean UseInventorySlot;

        @Config.Comment("""
            If enabled, a hotswap will be performed every tick if the main hand has no use or is empty.
            This hotswap allows for many more items like fishing rods to be used in the offhand, but may be unstable.
            """)
        @Config.DefaultBoolean(false)
        public boolean OffhandTickHotswap;

        @Config.Comment("""
            These items will be unable to be swapped into the offhand.
            Formatting of an item should be: modid:itemname
            These should all be placed on separate lines between the provided '<' and '>'.
            """)
        @Config.DefaultStringList({})
        public String[] offhandBlacklist;

        @Config.Comment("Picked up items can go into the offhand slot when empty. False in vanilla")
        @Config.DefaultBoolean(false)
        public boolean OffhandPickup;
    }

    public static class Client {

        @Config.Comment("If set to false, an empty offhand will only be rendered when the player is punching with the offhand.")
        @Config.DefaultBoolean(false)
        public boolean RenderEmptyOffhandAtRest;
    }
}
