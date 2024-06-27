package xonin.backhand.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import xonin.backhand.CommonProxy;
import xonin.backhand.packet.OffhandAnimationPacket;
import xonin.backhand.utils.EnumAnimations;

public class ClientProxy extends CommonProxy {

    public static final KeyBinding swapOffhand = new KeyBinding(
        "Swap Offhand",
        Keyboard.KEY_F,
        "key.categories.gameplay");
    public static int rightClickCounter = 0;

    public void load() {
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        FMLCommonHandler.instance()
            .bus()
            .register(new ClientTickHandler());
        FMLCommonHandler.instance()
            .bus()
            .register(new BackhandClientTickHandler());

        ClientRegistry.registerKeyBinding(swapOffhand);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    public EntityPlayer getClientPlayer() {
        return Minecraft.getMinecraft().thePlayer;
    }

    @Override
    public void sendAnimationPacket(EnumAnimations animation, EntityPlayer entityPlayer) {
        if (entityPlayer instanceof EntityClientPlayerMP) {
            ((EntityClientPlayerMP) entityPlayer).sendQueue
                .addToSendQueue(new OffhandAnimationPacket(animation, entityPlayer).generatePacket());
        }
    }

    @Override
    public boolean isRightClickHeld() {
        return Minecraft.getMinecraft().gameSettings.keyBindUseItem.getIsKeyPressed();
    }

    @Override
    public int getRightClickCounter() {
        return rightClickCounter;
    }

    @Override
    public int getRightClickDelay() {
        return ClientTickHandler.delay;
    }

    @Override
    public void setRightClickCounter(int i) {
        rightClickCounter = i;
    }

    @Override
    public boolean isLeftClickHeld() {
        return Minecraft.getMinecraft().gameSettings.keyBindAttack.getIsKeyPressed();
    }

    @Override
    public int getLeftClickCounter() {
        return Minecraft.getMinecraft().leftClickCounter;
    }

    @Override
    public void setLeftClickCounter(int i) {
        Minecraft.getMinecraft().leftClickCounter = i;
    }
}
