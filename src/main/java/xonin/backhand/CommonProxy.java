package xonin.backhand;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import com.gtnewhorizon.gtnhlib.eventbus.EventBusSubscriber;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.packet.OffhandConfigSyncPacket;
import xonin.backhand.packet.OffhandSyncItemPacket;

@EventBusSubscriber
public class CommonProxy {

    public void load() {}

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.player instanceof EntityPlayerMP playerMP)) {
            return;
        }
        Backhand.packetHandler.sendPacketToPlayer(new OffhandConfigSyncPacket().generatePacket(), playerMP);
        ItemStack offhandItem = BackhandUtils.getOffhandItem(playerMP);
        if (Backhand.isOffhandBlacklisted(offhandItem)) {
            BackhandUtils.setPlayerOffhandItem(playerMP, null);
            if (!playerMP.inventory.addItemStackToInventory(offhandItem)) {
                event.player.entityDropItem(offhandItem, 0);
            }
        }
    }

    @SubscribeEvent
    public static void addTracking(net.minecraftforge.event.entity.player.PlayerEvent.StartTracking event) {
        if (event.entityPlayer instanceof EntityPlayerMP playerMP && BackhandUtils.isValidPlayer(event.target)) {
            Backhand.packetHandler
                .sendPacketToPlayer(new OffhandSyncItemPacket((EntityPlayer) event.target).generatePacket(), playerMP);
        }
    }
}
