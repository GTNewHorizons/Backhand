package xonin.backhand.compat;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import com.gtnewhorizon.gtnhlib.eventbus.EventBusSubscriber;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import jds.bibliocraft.blocks.BlockClipboard;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.utils.Mods;

@EventBusSubscriber(side = Side.CLIENT)
public class BibliocraftCompat {

    @EventBusSubscriber.Condition
    public static boolean register() {
        return Mods.BIBLIOCRAFT.isLoaded();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerInteract(PlayerInteractEvent event) {
        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK
            && BackhandUtils.isUsingOffhand(event.entityPlayer)
            && !event.entityPlayer.isSneaking()
            && event.world.getBlock(event.x, event.y, event.z) instanceof BlockClipboard) {
            event.setCanceled(true);
        }
    }
}
