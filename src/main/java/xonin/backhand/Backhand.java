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
    dependencies = "required-after:gtnhlib@[0.6.34,)")
public class Backhand {

    public static final String MODID = "backhand";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @Mod.Instance
    public static Backhand Instance;

    @SidedProxy(clientSide = "xonin.backhand.client.ClientProxy", serverSide = "xonin.backhand.CommonProxy")
    public static CommonProxy proxy;

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
        BackhandPacketHandler.init();
    }

    public static MinecraftServer getServer() {
        return FMLCommonHandler.instance()
            .getMinecraftServerInstance();
    }

    public static boolean isOffhandBlacklisted(ItemStack stack) {
        return matchesItemName(stack, BackhandConfig.offhandBlacklist);
    }

    public static boolean doesMainhandUseStopOffhandFallback(ItemStack stack) {
        return matchesItemName(stack, BackhandConfig.mainhandUseStopsOffhandFallback);
    }

    // Entries are "modid:itemname" (any damage value) or "modid:itemname/damage" (that damage value only, using
    // NEI's own notation), since some tools (e.g. GT's meta tools) share one registered item across many unrelated
    // tool types by damage value.
    private static boolean matchesItemName(ItemStack stack, String[] itemNames) {
        if (stack == null || stack.getItem() == null) return false;

        String registryName = stack.getItem().delegate.name();
        for (String itemName : itemNames) {
            int damageSeparator = itemName.indexOf('/');
            if (damageSeparator < 0) {
                if (registryName.equals(itemName)) {
                    return true;
                }
            } else {
                String name = itemName.substring(0, damageSeparator);
                try {
                    int damage = Integer.parseInt(itemName.substring(damageSeparator + 1));
                    if (stack.getItemDamage() == damage && registryName.equals(name)) {
                        return true;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return false;
    }
}
