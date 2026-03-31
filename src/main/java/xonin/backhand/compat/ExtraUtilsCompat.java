package xonin.backhand.compat;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import com.gtnewhorizon.gtnhlib.eventbus.EventBusSubscriber;
import com.rwtema.extrautils.item.ItemHealingAxe;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
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
        EntityPlayer player = event.entityPlayer;
        ItemStack stack = BackhandUtils.getOffhandItem(player);
        if (stack != null && stack.getItem() instanceof ItemHealingAxe) {
            event.setCanceled(true);
        }
    }
}
