package xonin.backhand;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.packet.OffhandSyncItemPacket;
import xonin.backhand.packet.OffhandWorldHotswapPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.*;

public class ServerTickHandler {

    public ItemStack prevStackInSlot;
    public int blacklistDelay = -1;

    public static HashMap<UUID,List<ItemStack>> tickStartItems = new HashMap<>();

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public void onUpdateWorld(TickEvent.WorldTickEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide() != Side.SERVER) {
            return;
        }

        if (Backhand.OffhandTickHotswap) {
            List<EntityPlayer> players = event.world.playerEntities;
            for (EntityPlayer player : players) {
                ItemStack mainhand = player.getCurrentEquippedItem() == null ? null : player.getCurrentEquippedItem().copy();
                ItemStack offhand = BackhandUtils.getOffhandItem(player) == null ? null : BackhandUtils.getOffhandItem(player).copy();
                if (offhand == null) {
                    continue;
                }

                if (event.phase == TickEvent.Phase.START && !player.isUsingItem()) {
                    if (!BackhandUtils.checkForRightClickFunction(mainhand)) {
                        if (!tickStartItems.containsKey(player.getUniqueID())) {
                            Backhand.packetHandler.sendPacketToPlayer(
                                    new OffhandWorldHotswapPacket(true).generatePacket(), (EntityPlayerMP) player
                            );
                        }
                        tickStartItems.put(player.getUniqueID(), Arrays.asList(mainhand, offhand));
                        player.setCurrentItemOrArmor(0, tickStartItems.get(player.getUniqueID()).get(1));
                    }
                } else {
                    ServerTickHandler.resetTickingHotswap(player);
                }
            }
        }
    }

    public static void resetTickingHotswap(EntityPlayer player) {
        if (tickStartItems.containsKey(player.getUniqueID())) {
            player.setCurrentItemOrArmor(0, tickStartItems.get(player.getUniqueID()).get(0));
            BackhandUtils.setPlayerOffhandItem(player, tickStartItems.get(player.getUniqueID()).get(1));
            tickStartItems.remove(player.getUniqueID());
            Backhand.packetHandler.sendPacketToPlayer(
                    new OffhandWorldHotswapPacket(false).generatePacket(), (EntityPlayerMP) player
            );
        }
    }

    @SubscribeEvent(
            priority = EventPriority.HIGHEST
    )
    public void onUpdatePlayer(TickEvent.PlayerTickEvent event)
    {
        EntityPlayer player = event.player;
        if (FMLCommonHandler.instance().getEffectiveSide() != Side.SERVER) {
            if (ServerEventsHandler.regularHotSwap) {
                BackhandUtils.swapOffhandItem(player);
                ServerEventsHandler.regularHotSwap = false;
            }
            return;
        }

        ItemStack offhand = BackhandUtils.getOffhandItem(player);

        if (event.phase == TickEvent.Phase.END) {
            if (blacklistDelay > 0) {
                blacklistDelay--;
            }
            if (Backhand.isOffhandBlacklisted(offhand)) {
                if (!ItemStack.areItemStacksEqual(offhand,prevStackInSlot)) {
                    blacklistDelay = 10;
                } else if (blacklistDelay == 0) {
                    BackhandUtils.setPlayerOffhandItem(player,null);

                    boolean foundSlot = false;
                    for (int i = 0; i < player.inventory.getSizeInventory() - 4; i++) {
                        if (i == Backhand.AlternateOffhandSlot)
                            continue;
                        if (player.inventory.getStackInSlot(i) == null) {
                            player.inventory.setInventorySlotContents(i,offhand);
                            foundSlot = true;
                            break;
                        }
                    }
                    if (!foundSlot) {
                        player.entityDropItem(offhand,0);
                    }
                    player.inventoryContainer.detectAndSendChanges();
                }
            }
            prevStackInSlot = offhand;
        }

        if (BackhandUtils.getOffhandEP(player).syncOffhand) {
            if (!tickStartItems.containsKey(player.getUniqueID())) {
                Backhand.packetHandler.sendPacketToAll(new OffhandSyncItemPacket(player).generatePacket());
            }
            BackhandUtils.getOffhandEP(player).syncOffhand = false;
        }

        if (ServerEventsHandler.arrowHotSwapped) {
            if (offhand != null && offhand.getItem() != Items.arrow) {
                BackhandUtils.swapOffhandItem(player);
            }
            ServerEventsHandler.arrowHotSwapped = false;
        }
        if (ServerEventsHandler.regularHotSwap) {
            BackhandUtils.swapOffhandItem(player);
            ServerEventsHandler.regularHotSwap = false;
        }

        if (ServerEventsHandler.fireworkHotSwapped > 0) {
            ServerEventsHandler.fireworkHotSwapped--;
        } else if (ServerEventsHandler.fireworkHotSwapped == 0) {
            BackhandUtils.swapOffhandItem(player);
            ServerEventsHandler.fireworkHotSwapped--;
            MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent(player, PlayerInteractEvent.Action.RIGHT_CLICK_AIR,
                    (int)player.posX, (int)player.posY, (int)player.posZ, -1, player.worldObj));
            BackhandUtils.swapOffhandItem(player);
        }
    }
}
