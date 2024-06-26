package xonin.backhand;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import mods.battlegear2.BattlemodeHookContainerClass;
import mods.battlegear2.packet.BattlegearPacketHandler;
import xonin.backhand.utils.BackhandConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

@Mod(modid = "backhand", name = "Backhand", version = "$version", guiFactory = "xonin.backhand.client.gui.BackhandGuiFactory")
public class Backhand {
    public static Backhand Instance;

    public static FMLEventChannel Channel;
    public static FMLEventChannel ChannelPlayer;

    @SidedProxy(clientSide = "xonin.backhand.client.ClientProxy",
                serverSide = "xonin.backhand.CommonProxy")
    public static CommonProxy proxy;
    public static BattlegearPacketHandler packetHandler;

    public static boolean OffhandAttack = false;
    public static boolean EmptyOffhand = false;
    public static boolean OffhandBreakBlocks = false;
    public static boolean UseOffhandArrows = true;
    public static boolean UseOffhandBow = true;
    public static boolean OffhandTickHotswap = true;
    public static int AlternateOffhandSlot = 9;
    public static boolean UseInventorySlot = false;
    public static String[] offhandBlacklist;

    public static boolean RenderEmptyOffhandAtRest = false;

    public Backhand() {
        Instance = this;
    }

    @Mod.EventHandler
    public void load(FMLPreInitializationEvent event) {
        Channel = NetworkRegistry.INSTANCE.newEventDrivenChannel("Backhand");
        ChannelPlayer = NetworkRegistry.INSTANCE.newEventDrivenChannel("BackhandPlayer");

        BackhandConfig.getConfig(new Configuration(event.getSuggestedConfigurationFile()));

        proxy.load();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);

        MinecraftForge.EVENT_BUS.register(new ServerEventsHandler());
        FMLCommonHandler.instance().bus().register(new ServerTickHandler());

        MinecraftForge.EVENT_BUS.register(BattlemodeHookContainerClass.INSTANCE);
        FMLCommonHandler.instance().bus().register(BattlemodeHookContainerClass.INSTANCE);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        packetHandler = new BattlegearPacketHandler();
        packetHandler.register();
    }

    public static MinecraftServer getServer(){
        return MinecraftServer.getServer();
    }

    public static boolean isOffhandBlacklisted(ItemStack stack) {
        if (stack == null)
            return false;

        for (String itemName : offhandBlacklist) {
            if (stack.getItem().delegate.name().equals(itemName)) {
                return true;
            }
        }
        return false;
    }
}