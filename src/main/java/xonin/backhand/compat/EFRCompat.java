package xonin.backhand.compat;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import com.gtnewhorizon.gtnhlib.eventbus.EventBusSubscriber;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.core.handlers.ServerEventHandler;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.utils.Mods;

@SuppressWarnings("unused")
@EventBusSubscriber
public class EFRCompat {

    @EventBusSubscriber.Condition
    public static boolean register() {
        return Mods.ET_FUTURUM.isLoaded();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityHurt(LivingHurtEvent event) {
        if (!(event.entityLiving instanceof EntityPlayer player)) return;
        ItemStack offhand = BackhandUtils.getOffhandItem(player);
        if (offhand == null) return;
        Item totem = ModItems.TOTEM_OF_UNDYING.get();
        if (totem.equals(offhand.getItem())) {
            BackhandUtils.useOffhandItem(player, () -> ServerEventHandler.INSTANCE.handleTotemCheck(player, event));
        }
    }
}
