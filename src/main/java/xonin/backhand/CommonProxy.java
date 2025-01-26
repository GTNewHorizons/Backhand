package xonin.backhand;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import com.gtnewhorizon.gtnhlib.eventbus.EventBusSubscriber;
import com.gtnewhorizon.gtnhlib.keybind.SyncedKeybind;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.packet.BackhandPacketHandler;
import xonin.backhand.packet.OffhandSyncItemPacket;

@EventBusSubscriber
public class CommonProxy {

    public static final SyncedKeybind SWAP_KEY = SyncedKeybind
        .createConfigurable("backhand.swap_offhand", "key.categories.gameplay", 33)
        .registerGlobalListener(CommonProxy::swapOffhand);

    public void load() {}

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.player instanceof EntityPlayerMP playerMP)) {
            return;
        }
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
        if (BackhandUtils.isValidPlayer(event.entityPlayer) && BackhandUtils.isValidPlayer(event.target)) {
            BackhandPacketHandler
                .sendPacketToPlayer(new OffhandSyncItemPacket((EntityPlayer) event.target), event.entityPlayer);
        }
    }

    private static void swapOffhand(EntityPlayerMP player, SyncedKeybind keybind) {
        ItemStack offhandItem = BackhandUtils.getOffhandItem(player);
        if (Backhand.isOffhandBlacklisted(player.getCurrentEquippedItem())
            || Backhand.isOffhandBlacklisted(offhandItem)) {
            return;
        }

        BackhandUtils.setPlayerOffhandItem(player, player.getCurrentEquippedItem());
        player.setCurrentItemOrArmor(0, offhandItem);
        player.inventoryContainer.detectAndSendChanges();
    }
}
