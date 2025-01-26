package xonin.backhand.packet;

import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import xonin.backhand.Backhand;
import xonin.backhand.api.core.BackhandUtils;

public final class BackhandPacketHandler {

    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(Backhand.MODID);

    public static void init() {
        NETWORK.registerMessage(OffhandSyncItemPacket.Handler.class, OffhandSyncItemPacket.class, 0, Side.CLIENT);
        NETWORK.registerMessage(OffhandSyncOffhandUse.Handler.class, OffhandSyncOffhandUse.class, 1, Side.CLIENT);
    }

    public static void sendPacketToPlayer(IMessage packet, EntityPlayer player) {
        if (player instanceof EntityPlayerMP playerMP) {
            NETWORK.sendTo(packet, playerMP);
        }
    }

    public static void sendPacketToAllTracking(Entity entity, IMessage packet) {
        if (!(entity.worldObj instanceof WorldServer world) || !BackhandUtils.isValidPlayer(entity)) return;
        Set<EntityPlayer> trackingPlayer = world.getEntityTracker()
            .getTrackingPlayers(entity);

        for (EntityPlayer player : trackingPlayer) {
            if (BackhandUtils.isValidPlayer(player)) {
                sendPacketToPlayer(packet, player);
            }
        }
    }
}
