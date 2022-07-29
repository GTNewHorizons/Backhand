package xonin.backhand.client;

import cpw.mods.fml.common.FMLCommonHandler;
import mods.battlegear2.client.BattlegearClientTickHandler;
import mods.battlegear2.packet.BattlegearAnimationPacket;
import mods.battlegear2.utils.EnumBGAnimations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import xonin.backhand.CommonProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {
    public static final KeyBinding swapOffhand = new KeyBinding("Swap Offhand", Keyboard.KEY_F, "key.categories.gameplay");

    public void load() {
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        FMLCommonHandler.instance().bus().register(new ClientTickHandler());

        FMLCommonHandler.instance().bus().register(new BattlegearClientTickHandler());
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
    public void sendAnimationPacket(EnumBGAnimations animation, EntityPlayer entityPlayer) {
        if (entityPlayer instanceof EntityClientPlayerMP) {
            ((EntityClientPlayerMP) entityPlayer).sendQueue.addToSendQueue(
                    new BattlegearAnimationPacket(animation, entityPlayer).generatePacket());
        }
    }

    @Override
    public boolean isRightClickHeld() {
        return Minecraft.getMinecraft().gameSettings.keyBindUseItem.getIsKeyPressed();
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
