package xonin.backhand.packet;

import java.util.Hashtable;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.WorldServer;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public final class BackhandPacketHandler {

    public Map<String, AbstractPacket> map = new Hashtable<>();
    public Map<String, FMLEventChannel> channels = new Hashtable<>();

    public BackhandPacketHandler() {
        map.put(OffhandSyncItemPacket.packetName, new OffhandSyncItemPacket());
        map.put(OffhandAnimationPacket.packetName, new OffhandAnimationPacket());
        map.put(OffhandSwapPacket.packetName, new OffhandSwapPacket());
        map.put(OffhandSwapClientPacket.packetName, new OffhandSwapClientPacket());
        map.put(OffhandConfigSyncPacket.packetName, new OffhandConfigSyncPacket());
        map.put(OffhandSyncOffhandUse.packetName, new OffhandSyncOffhandUse());
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
    @SideOnly(Side.CLIENT)
    public void onClientPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
        map.get(event.packet.channel())
            .process(event.packet.payload(), Minecraft.getMinecraft().thePlayer);
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

    public void sendPacketToAllTracking(Entity entity, FMLProxyPacket packet) {
        if (!(entity.worldObj instanceof WorldServer world)) return;
        world.getEntityTracker()
            .func_151247_a(entity, packet);
    }
}
