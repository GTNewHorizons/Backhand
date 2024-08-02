package xonin.backhand;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.OffhandExtendedProperty;
import xonin.backhand.packet.OffhandSyncItemPacket;

public class ServerTickHandler {

    public ItemStack prevStackInSlot;
    public int blacklistDelay = -1;

    public static final HashMap<UUID, List<ItemStack>> tickStartItems = new HashMap<>();

    public static void resetTickingHotswap(EntityPlayer player) {
        List<ItemStack> tickedItems = tickStartItems.get(player.getUniqueID());
        if (tickedItems != null) {
            BackhandUtils.getOffhandEP(player).ignoreSetSlot = false;
            player.setCurrentItemOrArmor(0, tickedItems.get(0));
            BackhandUtils.setPlayerOffhandItem(player, tickedItems.get(1));
            tickStartItems.remove(player.getUniqueID());
        }
    }

    public static void tickHotswap(EntityPlayerMP player) {
        ItemStack mainhand = ItemStack.copyItemStack(player.getCurrentEquippedItem());
        ItemStack offhand = BackhandUtils.getOffhandItem(player);
        if (offhand == null || player.currentWindowId != 0) return;
        UUID key = player.getUniqueID();
        if (!BackhandUtils.checkForRightClickFunction(mainhand)) {
            if (!tickStartItems.containsKey(key)) {
                BackhandUtils.getOffhandEP(player).ignoreSetSlot = true;
            }
            tickStartItems.put(key, Arrays.asList(mainhand, offhand));
            player.setCurrentItemOrArmor(
                0,
                tickStartItems.get(key)
                    .get(1));
            BackhandUtils.getOffhandEP(player).activeSlot = player.inventory.currentItem
                + player.inventory.mainInventory.length;
        }
    }

    @SubscribeEvent
    public void onUpdateWorld(TickEvent.WorldTickEvent event) {
        if (FMLCommonHandler.instance()
            .getEffectiveSide() != Side.SERVER) {
            return;
        }
        if (Backhand.OffhandTickHotswap) {
            for (EntityPlayer player : event.world.playerEntities) {
                if (!(player instanceof EntityPlayerMP playerMP)) continue;
                OffhandExtendedProperty offhandProp = BackhandUtils.getOffhandEP(player);

                if (event.phase == TickEvent.Phase.START && !player.isUsingItem() && offhandProp.hotswapDelay <= 0) {
                    tickHotswap(playerMP);
                } else {
                    if (offhandProp.hotswapDelay > 0) offhandProp.hotswapDelay--;
                    resetTickingHotswap(playerMP);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onUpdatePlayer(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        OffhandExtendedProperty offhandProp = BackhandUtils.getOffhandEP(player);

        if (FMLCommonHandler.instance()
            .getEffectiveSide() != Side.SERVER) {
            if (offhandProp.regularHotSwap) {
                BackhandUtils.swapOffhandItem(player);
                offhandProp.regularHotSwap = false;
            }
            return;
        }

        ItemStack offhand = BackhandUtils.getOffhandItem(player);

        if (event.phase == TickEvent.Phase.END) {
            if (blacklistDelay > 0) {
                blacklistDelay--;
            }
            if (Backhand.isOffhandBlacklisted(offhand)) {
                if (!ItemStack.areItemStacksEqual(offhand, prevStackInSlot)) {
                    blacklistDelay = 10;
                } else if (blacklistDelay == 0) {
                    BackhandUtils.setPlayerOffhandItem(player, null);

                    boolean foundSlot = false;
                    for (int i = 0; i < player.inventory.getSizeInventory() - 4; i++) {
                        if (i == Backhand.AlternateOffhandSlot) continue;
                        if (player.inventory.getStackInSlot(i) == null) {
                            player.inventory.setInventorySlotContents(i, offhand);
                            foundSlot = true;
                            break;
                        }
                    }
                    if (!foundSlot) {
                        player.entityDropItem(offhand, 0);
                    }
                    player.inventoryContainer.detectAndSendChanges();
                }
            }
            prevStackInSlot = offhand;
        }

        if (offhandProp.syncOffhand) {
            if (!tickStartItems.containsKey(player.getUniqueID())) {
                Backhand.packetHandler.sendPacketToAll(new OffhandSyncItemPacket(player).generatePacket());
            }
            offhandProp.syncOffhand = false;
        }

        if (offhandProp.arrowHotSwapped) {
            if (offhand != null && offhand.getItem() != Items.arrow) {
                BackhandUtils.swapOffhandItem(player);
            }
            offhandProp.arrowHotSwapped = false;
        }
        if (offhandProp.regularHotSwap) {
            BackhandUtils.swapOffhandItem(player);
            offhandProp.regularHotSwap = false;
        }

        if (ServerEventsHandler.fireworkHotSwapped > 0) {
            ServerEventsHandler.fireworkHotSwapped--;
        } else if (ServerEventsHandler.fireworkHotSwapped == 0) {
            BackhandUtils.swapOffhandItem(player);
            ServerEventsHandler.fireworkHotSwapped--;
            MinecraftForge.EVENT_BUS.post(
                new PlayerInteractEvent(
                    player,
                    PlayerInteractEvent.Action.RIGHT_CLICK_AIR,
                    (int) player.posX,
                    (int) player.posY,
                    (int) player.posZ,
                    -1,
                    player.worldObj));
            BackhandUtils.swapOffhandItem(player);
        }
    }
}
