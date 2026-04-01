package xonin.backhand.compat;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import com.gtnewhorizon.gtnhlib.eventbus.EventBusSubscriber;
import com.rwtema.extrautils.item.ItemHealingAxe;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ganymedes01.etfuturum.api.StrippedLogRegistry;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.utils.Mods;

@EventBusSubscriber
public class ExtraUtilsCompat {

    @EventBusSubscriber.Condition
    public static boolean register() {
        return Mods.ET_FUTURUM.isLoaded() && Mods.EXTRA_UTILITIES.isLoaded();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerInteract(PlayerInteractEvent event) {
        if (event.world == null) return;
        // only handle right clicks
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;
        // only stop offhand attempts
        if (!BackhandUtils.isUsingOffhand(event.entityPlayer)) return;

        Block targetBlock = event.world.getBlock(event.x, event.y, event.z);
        if (targetBlock == Blocks.air) return;
        int meta = event.world.getBlockMetadata(event.x, event.y, event.z);

        EntityPlayer player = event.entityPlayer;
        ItemStack stack = BackhandUtils.getOffhandItem(player);
        if (stack != null && stack.getItem() instanceof ItemHealingAxe
            && StrippedLogRegistry.hasLog(targetBlock, meta)) {
            event.setCanceled(true);
        }
    }
}
