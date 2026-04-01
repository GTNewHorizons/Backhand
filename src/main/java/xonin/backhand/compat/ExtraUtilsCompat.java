package xonin.backhand.compat;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import com.gtnewhorizon.gtnhlib.eventbus.EventBusSubscriber;
import com.rwtema.extrautils.item.ItemHealingAxe;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ganymedes01.etfuturum.api.StrippedLogRegistry;
import ganymedes01.etfuturum.api.mappings.RegistryMapping;
import xonin.backhand.utils.Mods;

@EventBusSubscriber
public class ExtraUtilsCompat {

    @EventBusSubscriber.Condition
    public static boolean register() {
        return Mods.ET_FUTURUM.isLoaded() && Mods.EXTRA_UTILITIES.isLoaded();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerInteract(PlayerInteractEvent event) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;
        EntityPlayer player = event.entityPlayer;

        // Not using BackhandUtils.getOffhandItem since this event gets fired twice: once with the offhand, and another
        // item with the mainhand. So we can just block one of the events
        ItemStack stack = player.getHeldItem();
        if (stack != null && stack.getItem() instanceof ItemHealingAxe) {
            Block block = event.world.getBlock(event.x, event.y, event.z);
            int meta = event.world.getBlockMetadata(event.x, event.y, event.z);
            RegistryMapping<Block> mapping = StrippedLogRegistry.getLog(block, meta);
            if (mapping != null) {
                event.setCanceled(true);
            }
        }
    }
}
