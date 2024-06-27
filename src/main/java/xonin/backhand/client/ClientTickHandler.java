package xonin.backhand.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import invtweaks.InvTweaks;
import xonin.backhand.HookContainerClass;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.packet.OffhandSwapPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Keyboard;
import xonin.backhand.Backhand;

public class ClientTickHandler {

    public static int delay;
    public static boolean prevInvTweaksAutoRefill;
    public static boolean prevInvTweaksBreakRefill;

    public static int invTweaksDelay;
    public static boolean allowSwap = true;

    @SubscribeEvent
    public void onKeyInputEvent(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityClientPlayerMP player = mc.thePlayer;

        if (ClientProxy.swapOffhand.getIsKeyPressed() && Keyboard.isKeyDown(Keyboard.getEventKey()) && allowSwap) {
            allowSwap = false;
            try {
                this.getClass()
                    .getMethod("invTweaksSwapPatch");
                invTweaksSwapPatch();
            } catch (Exception ignored) {}
            ((EntityClientPlayerMP) player).sendQueue.addToSendQueue(new OffhandSwapPacket(player).generatePacket());
        }
    }

    @Optional.Method(modid = "inventorytweaks")
    public void invTweaksSwapPatch() {
        if (invTweaksDelay <= 0) {
            prevInvTweaksAutoRefill = Boolean.parseBoolean(
                InvTweaks.getConfigManager()
                    .getConfig()
                    .getProperty("enableAutoRefill"));
            prevInvTweaksBreakRefill = Boolean.parseBoolean(
                InvTweaks.getConfigManager()
                    .getConfig()
                    .getProperty("autoRefillBeforeBreak"));
            InvTweaks.getConfigManager()
                .getConfig()
                .setProperty("enableAutoRefill", "false");
            InvTweaks.getConfigManager()
                .getConfig()
                .setProperty("autoRefillBeforeBreak", "false");
        }
        invTweaksDelay = 15;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (invTweaksDelay > 0) {
            invTweaksDelay--;
            if (invTweaksDelay == 0) {
                try {
                    this.getClass()
                        .getMethod("restoreInvTweaksConfigs");
                    restoreInvTweaksConfigs();
                } catch (Exception ignored) {}
            }
        }
    }

    @Optional.Method(modid = "inventorytweaks")
    public void restoreInvTweaksConfigs() {
        InvTweaks.getConfigManager()
            .getConfig()
            .setProperty("enableAutoRefill", String.valueOf(prevInvTweaksAutoRefill));
        InvTweaks.getConfigManager()
            .getConfig()
            .setProperty("autoRefillBeforeBreak", String.valueOf(prevInvTweaksBreakRefill));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void clientHelper(TickEvent.PlayerTickEvent event) {
        if (ClientTickHandler.delay > 0) {
            ClientTickHandler.delay--;
        }

        if (!Backhand.OffhandBreakBlocks) {
            return;
        }

        if (!Backhand.EmptyOffhand && BackhandUtils.getOffhandItem(event.player) == null) {
            return;
        }

        if (!Backhand.proxy.isRightClickHeld()) {
            Backhand.proxy.setRightClickCounter(0);
        }

        ItemStack mainHandItem = event.player.getCurrentEquippedItem();
        ItemStack offhandItem = BackhandUtils.getOffhandItem(event.player);

        if (mainHandItem != null && (BackhandUtils.checkForRightClickFunction(mainHandItem) || offhandItem == null)) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();

        if (event.player.worldObj.isRemote && Backhand.proxy.getLeftClickCounter() <= 0
            && mc.objectMouseOver != null
            && mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) {
            if (event.player.capabilities.allowEdit) {
                if (Backhand.proxy.isRightClickHeld() && !(mainHandItem != null && BackhandUtils.isItemBlock(mainHandItem.getItem()))) { // if it's a block and we should try break it
                    MovingObjectPosition mop = HookContainerClass.getRaytraceBlock(event.player);
                    if (offhandItem != null && HookContainerClass.isItemBlock(offhandItem.getItem())) {
                        if (!BackhandUtils.usagePriorAttack(offhandItem) && mop != null) {
                            BackhandClientTickHandler.tryBreakBlockOffhand(mop, offhandItem, mainHandItem, event);
                            Backhand.proxy.setLeftClickCounter(10);
                        } else {
                            mc.playerController.resetBlockRemoving();
                        }
                    } else {
                        if (mop != null && !BackhandUtils.usagePriorAttack(offhandItem) && !HookContainerClass.canBlockBeInteractedWith(mc.theWorld, mop.blockX, mop.blockY, mop.blockZ)) {
                            BackhandClientTickHandler.tryBreakBlockOffhand(mop, offhandItem, mainHandItem, event);
                            Backhand.proxy.setLeftClickCounter(10);
                        } else {
                            mc.playerController.resetBlockRemoving();
                        }
                    }
                } else if (!Backhand.proxy.isLeftClickHeld()) {
                    mc.playerController.resetBlockRemoving();
                }
            }
        }
    }
}
