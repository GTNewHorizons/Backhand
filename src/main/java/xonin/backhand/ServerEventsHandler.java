package xonin.backhand;

import net.minecraft.block.BlockDispenser;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistrySimple;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import ganymedes01.etfuturum.ModItems;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.OffhandExtendedProperty;

public class ServerEventsHandler {

    public static int fireworkHotSwapped = -1;

    @SubscribeEvent
    public void onPlayerInteractNonVanilla(PlayerInteractEvent event) {
        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            EntityPlayer player = event.entityPlayer;
            ItemStack mainhandItem = player.getHeldItem();
            ItemStack offhandItem = BackhandUtils.getOffhandItem(player);
            if ((mainhandItem == null
                || mainhandItem.getItem() != Items.fireworks && !BackhandUtils.usagePriorAttack(mainhandItem))
                && offhandItem != null
                && offhandItem.getItem() == Items.fireworks) {
                fireworkHotSwapped = 1;
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (!(event.entityLiving instanceof EntityPlayer player)) return;

        if (!BackhandUtils.hasOffhandInventory(player)) {
            ItemStack offhandItem = BackhandUtils.getOffhandItem(player);
            player.func_146097_a(offhandItem, true, false);
            BackhandUtils.setPlayerOffhandItem(player, null);
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.entityLiving instanceof EntityPlayer player) || event.entityLiving.getHealth() - event.ammount > 0)
            return;

        ItemStack offhandItem = BackhandUtils.getOffhandItem(player);
        ItemStack mainhandItem = player.getCurrentEquippedItem();
        OffhandExtendedProperty offhandProp = BackhandUtils.getOffhandEP(player);

        if (offhandItem == null) {
            return;
        }

        if (Backhand.isEFRLoaded) {
            Item totem = ModItems.TOTEM_OF_UNDYING.get();

            if (offhandItem.getItem() == totem && (mainhandItem == null || mainhandItem.getItem() != totem)) {
                BackhandUtils.swapOffhandItem(player, 5);
                offhandProp.regularHotSwap = true;
            }
        }
    }

    @SubscribeEvent
    public void onItemUseStart(PlayerUseItemEvent.Start event) {
        EntityPlayer player = event.entityPlayer;
        ItemStack offhandItem = BackhandUtils.getOffhandItem(player);
        ItemStack mainhandItem = player.getCurrentEquippedItem();
        boolean mainhandUse = BackhandUtils.checkForRightClickFunction(mainhandItem);

        if (offhandItem != null && !mainhandUse) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onArrowNock(ArrowNockEvent event) {
        if (!Backhand.UseOffhandArrows) {
            return;
        }
        EntityPlayer player = event.entityPlayer;
        ItemStack offhandItem = BackhandUtils.getOffhandItem(player);
        if (offhandItem != null) {
            if (!((RegistrySimple) BlockDispenser.dispenseBehaviorRegistry).containsKey(offhandItem.getItem())) {
                return;
            }

            event.setCanceled(true);
            player.setItemInUse(
                event.result,
                event.result.getItem()
                    .getMaxItemUseDuration(event.result));
        }
    }

    @SubscribeEvent
    public void onItemFinish(PlayerUseItemEvent.Finish event) {
        EntityPlayer player = event.entityPlayer;
        ItemStack offhandItem = BackhandUtils.getOffhandItem(player);
        ItemStack mainhandItem = player.getCurrentEquippedItem();
        OffhandExtendedProperty offhandProp = BackhandUtils.getOffhandEP(player);
        boolean mainhandUse = BackhandUtils.checkForRightClickFunction(mainhandItem);
        if (offhandItem == null || mainhandUse || offhandProp == null) {
            return;
        }
        if (FMLCommonHandler.instance()
            .getEffectiveSide() == Side.SERVER && !ServerTickHandler.tickStartItems.containsKey(player.getUniqueID())) {
            BackhandUtils.swapOffhandItem(player);
            offhandProp.regularHotSwap = true;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemStop(PlayerUseItemEvent.Stop event) {
        EntityPlayer player = event.entityPlayer;
        ItemStack mainhandItem = player.getCurrentEquippedItem();
        OffhandExtendedProperty offhandProp = BackhandUtils.getOffhandEP(player);
        boolean skip = BackhandUtils.checkForRightClickFunction(mainhandItem)
            || BackhandUtils.getOffhandItem(player) == null;

        if (!skip && !ServerTickHandler.tickStartItems.containsKey(player.getUniqueID())
            && !offhandProp.regularHotSwap) {
            BackhandUtils.swapOffhandItem(player);
            offhandProp.regularHotSwap = true;
            return;
        }

        if (!Backhand.UseOffhandArrows || !(event.item.getItem() instanceof ItemBow)) {
            return;
        }

        ItemStack offhandItem = BackhandUtils.getOffhandItem(player);
        if (offhandItem != null) {
            if (!((RegistrySimple) BlockDispenser.dispenseBehaviorRegistry).containsKey(offhandItem.getItem())) {
                return;
            }

            if (offhandItem.getItem() != Items.arrow) {
                offhandProp.regularHotSwap = true;
                BackhandUtils.swapOffhandItem(player, 5);
            }
        }
    }
}
