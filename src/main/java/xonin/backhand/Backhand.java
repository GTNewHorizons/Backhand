package xonin.backhand;

import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import xonin.backhand.packet.BackhandPacketHandler;
import xonin.backhand.utils.BackhandConfig;

@Mod(
    modid = Backhand.MODID,
    name = "Backhand",
    version = Tags.VERSION,
    dependencies = "required-after:gtnhlib@[0.3.2,)",
    guiFactory = "xonin.backhand.client.gui.BackhandGuiFactory")
public class Backhand {

    public static final String MODID = "backhand";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @Mod.Instance
    public static Backhand Instance;

    @SidedProxy(clientSide = "xonin.backhand.client.ClientProxy", serverSide = "xonin.backhand.CommonProxy")
    public static CommonProxy proxy;
    public static BackhandPacketHandler packetHandler;

    public static boolean OffhandAttack;
    public static boolean EmptyOffhand;
    public static boolean OffhandBreakBlocks;
    public static boolean UseOffhandArrows;
    public static boolean UseOffhandBow;
    public static boolean OffhandTickHotswap;
    public static int AlternateOffhandSlot;
    public static boolean UseInventorySlot;
    public static String[] offhandBlacklist;

    public static boolean RenderEmptyOffhandAtRest;

    public static boolean isEFRLoaded;

    @Mod.EventHandler
    public void load(FMLPreInitializationEvent event) {
        isEFRLoaded = Loader.isModLoaded("etfuturum");
        BackhandConfig.getConfig(new Configuration(event.getSuggestedConfigurationFile()));

        proxy.load();

        MinecraftForge.EVENT_BUS.register(new ServerEventsHandler());
        FMLCommonHandler.instance()
            .bus()
            .register(new ServerTickHandler());

        MinecraftForge.EVENT_BUS.register(HookContainerClass.INSTANCE);
        FMLCommonHandler.instance()
            .bus()
            .register(HookContainerClass.INSTANCE);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        packetHandler = new BackhandPacketHandler();
        packetHandler.register();
    }

    @Mod.EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
        proxy.onServerStopping(event);
    }

    public static MinecraftServer getServer() {
        return FMLCommonHandler.instance()
            .getMinecraftServerInstance();
    }

    public static boolean isOffhandBlacklisted(ItemStack stack) {
        if (stack == null) return false;

        for (String itemName : offhandBlacklist) {
            if (stack.getItem().delegate.name()
                .equals(itemName)) {
                return true;
            }
        }
        return false;
    }
}
