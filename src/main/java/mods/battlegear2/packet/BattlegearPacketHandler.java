package mods.battlegear2.packet;

import java.util.Hashtable;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import xonin.backhand.Backhand;

public final class BattlegearPacketHandler {

    public Map<String, AbstractMBPacket> map = new Hashtable<>();
    public Map<String, FMLEventChannel> channels = new Hashtable<>();

    public BattlegearPacketHandler() {
        map.put(BattlegearSyncItemPacket.packetName, new BattlegearSyncItemPacket());
        map.put(BattlegearAnimationPacket.packetName, new BattlegearAnimationPacket());
        map.put(OffhandPlaceBlockPacket.packetName, new OffhandPlaceBlockPacket());
        map.put(OffhandToServerPacket.packetName, new OffhandToServerPacket());
        map.put(OffhandSwapPacket.packetName, new OffhandSwapPacket());
        map.put(OffhandSwapClientPacket.packetName, new OffhandSwapClientPacket());
        map.put(OffhandAttackPacket.packetName, new OffhandAttackPacket());
        map.put(OffhandWorldHotswapPacket.packetName, new OffhandWorldHotswapPacket());
        map.put(OffhandConfigSyncPacket.packetName, new OffhandConfigSyncPacket());
    }

    public void register() {
        FMLEventChannel eventChannel;
        for (String channel : map.keySet()) {
            eventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(channel);
            eventChannel.register(this);
            channels.put(channel, eventChannel);
        }
    }

    @SubscribeEvent
    public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
        map.get(event.packet.channel())
            .process(event.packet.payload(), ((NetHandlerPlayServer) event.handler).playerEntity);
    }

    @SubscribeEvent
    public void onClientPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
        map.get(event.packet.channel())
            .process(event.packet.payload(), Backhand.proxy.getClientPlayer());
    }

    public void sendPacketToPlayer(FMLProxyPacket packet, EntityPlayerMP player) {
        if (FMLCommonHandler.instance()
            .getEffectiveSide() == Side.SERVER) {
            channels.get(packet.channel())
                .sendTo(packet, player);
        }
    }

    public void sendPacketToServer(FMLProxyPacket packet) {
        packet.setTarget(Side.SERVER);
        channels.get(packet.channel())
            .sendToServer(packet);
    }

    public void sendPacketAround(Entity entity, double range, FMLProxyPacket packet) {
        if (FMLCommonHandler.instance()
            .getEffectiveSide() == Side.SERVER) {
            channels.get(packet.channel())
                .sendToAllAround(
                    packet,
                    new NetworkRegistry.TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, range));
        }
    }

    public void sendPacketToAll(FMLProxyPacket packet) {
        if (FMLCommonHandler.instance()
            .getEffectiveSide() == Side.SERVER) {
            channels.get(packet.channel())
                .sendToAll(packet);
        }
    }
}
