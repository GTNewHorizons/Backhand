package xonin.backhand;

import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import xonin.backhand.packet.BackhandPacketHandler;
import xonin.backhand.utils.BackhandConfig;
import xonin.backhand.utils.BackhandConfigClient;

@Mod(
    modid = Backhand.MODID,
    name = "Backhand",
    version = Tags.VERSION,
    dependencies = "required-after:gtnhlib@[0.6.3,)")
public class Backhand {

    public static final String MODID = "backhand";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @Mod.Instance
    public static Backhand Instance;

    @SidedProxy(clientSide = "xonin.backhand.client.ClientProxy", serverSide = "xonin.backhand.CommonProxy")
    public static CommonProxy proxy;
    public static BackhandPacketHandler packetHandler;

    @Mod.EventHandler
    public void load(FMLPreInitializationEvent event) {
        try {
            ConfigurationManager.registerConfig(BackhandConfig.class);
            ConfigurationManager.registerConfig(BackhandConfigClient.class);
        } catch (ConfigException e) {
            LOGGER.warn("Unable to register config", e);
        }

        proxy.load();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        packetHandler = new BackhandPacketHandler();
        packetHandler.register();
    }

    public static MinecraftServer getServer() {
        return FMLCommonHandler.instance()
            .getMinecraftServerInstance();
    }

    public static boolean isOffhandBlacklisted(ItemStack stack) {
        if (stack == null) return false;

        for (String itemName : BackhandConfig.offhandBlacklist) {
            if (stack.getItem().delegate.name()
                .equals(itemName)) {
                return true;
            }
        }
        return false;
    }
}
