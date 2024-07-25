package net.tclproject.mysteriumlib.asm.fixes;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.WorldServer;
import net.tclproject.mysteriumlib.asm.annotations.EnumReturnSetting;
import net.tclproject.mysteriumlib.asm.annotations.Fix;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import invtweaks.InvTweaksContainerManager;
import invtweaks.InvTweaksContainerSectionManager;
import invtweaks.api.container.ContainerSection;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.client.ClientEventHandler;

public class MysteriumPatchesFixesO {

    @Fix(returnSetting = EnumReturnSetting.ON_TRUE)
    @SideOnly(Side.CLIENT)
    public static boolean addBlockHitEffects(EffectRenderer er, int x, int y, int z, MovingObjectPosition target) {
        if (ClientEventHandler.cancelone) {
            return true;
        }
        return false;
    }

    @Fix(returnSetting = EnumReturnSetting.ALWAYS)
    public static void processUseEntity(NetHandlerPlayServer netServer, C02PacketUseEntity p_147340_1_) {
        WorldServer worldserver = netServer.serverController.worldServerForDimension(netServer.playerEntity.dimension);
        Entity entity = p_147340_1_.func_149564_a(worldserver);
        netServer.playerEntity.func_143004_u();

        boolean swappedOffhand = BackhandUtils
            .checkForRightClickFunction(BackhandUtils.getOffhandItem(netServer.playerEntity))
            && !BackhandUtils.checkForRightClickFunction(netServer.playerEntity.getCurrentEquippedItem())
            && p_147340_1_.func_149565_c() == C02PacketUseEntity.Action.INTERACT;
        if (swappedOffhand) {
            BackhandUtils.swapOffhandItem(netServer.playerEntity);
        }

        if (entity != null) {
            boolean flag = netServer.playerEntity.canEntityBeSeen(entity);
            double d0 = 36.0D;

            if (!flag) {
                d0 = 9.0D;
            }

            if (netServer.playerEntity.getDistanceSqToEntity(entity) < d0) {
                if (p_147340_1_.func_149565_c() == C02PacketUseEntity.Action.INTERACT) {
                    netServer.playerEntity.interactWith(entity);
                } else if (p_147340_1_.func_149565_c() == C02PacketUseEntity.Action.ATTACK) {
                    if (entity instanceof EntityItem || entity instanceof EntityXPOrb
                        || entity instanceof EntityArrow
                        || entity == netServer.playerEntity) {
                        netServer.kickPlayerFromServer("Attempting to attack an invalid entity");
                        netServer.serverController.logWarning(
                            "Player " + netServer.playerEntity.getCommandSenderName()
                                + " tried to attack an invalid entity");
                        if (swappedOffhand) {
                            BackhandUtils.swapOffhandItem(netServer.playerEntity);
                        }
                    }

                    netServer.playerEntity.attackTargetEntityWithCurrentItem(entity);
                }
            }
        }

        if (swappedOffhand) {
            BackhandUtils.swapOffhandItem(netServer.playerEntity);
        }
    }

    private static final MethodHandle fieldGetSection;
    private static final MethodHandle fieldGetContainerMgr;

    static {
        MethodHandle fs, fs2, fg, fg2, fg3;
        Field f, f2, f3, f4;
        try {

            f2 = InvTweaksContainerSectionManager.class.getDeclaredField("containerMgr");
            f3 = InvTweaksContainerSectionManager.class.getDeclaredField("section");

            f2.setAccessible(true);
            f3.setAccessible(true);

            fg = MethodHandles.publicLookup()
                .unreflectGetter(f2);
            fg2 = MethodHandles.publicLookup()
                .unreflectGetter(f3);
        } catch (Exception | NoClassDefFoundError e) {
            f = null;
            fs = null;
            fg = null;
            fg2 = null;
        }
        fieldGetContainerMgr = fg;
        fieldGetSection = fg2;

        // System.out.println("Loaded Mod Compatibility!");
    }

    @Optional.Method(modid = "inventorytweaks")
    public static ContainerSection getContainerSection(InvTweaksContainerSectionManager itcm) {
        ContainerSection section;
        try {
            section = (ContainerSection) fieldGetSection.invokeExact((InvTweaksContainerSectionManager) itcm);
        } catch (Throwable e) {
            /*
             * System.out.
             * println("The 'Inventory Tweaks' mod compatibility hasn't been loaded due to not being able to find ContainerSection. "
             * +
             * "If you don't have the 'Inventory Tweaks' mod installed, you can ignore this error.");
             */
            section = null;
        }
        return section;
    }

    @Optional.Method(modid = "inventorytweaks")
    public static InvTweaksContainerManager getContainerManager(InvTweaksContainerSectionManager itcm) {
        InvTweaksContainerManager manager;
        try {
            manager = (InvTweaksContainerManager) fieldGetContainerMgr
                .invokeExact((InvTweaksContainerSectionManager) itcm);
        } catch (Throwable e) {
            /*
             * System.out.
             * println("The 'Inventory Tweaks' mod compatibility hasn't been loaded due to not being able to find InvTweaksContainerManager. "
             * +
             * "If you don't have the 'Inventory Tweaks' mod installed, you can ignore this error.");
             */
            manager = null;
        }
        return manager;
    }

    // Todo: Angelica is a thing so this is probably not needed

    // @Optional.Method(modid = "Optifine")
    // @SideOnly(Side.CLIENT)
    // @Fix(insertOnExit = true, returnSetting = EnumReturnSetting.ALWAYS)
    // public static int getLightLevel(Entity entity, @ReturnedValue int returned) {
    // if (entity instanceof EntityPlayer) {
    // EntityPlayer player = (EntityPlayer) entity;
    // ItemStack offhand = BackhandUtils.getOffhandItem(player);
    // try {
    // Method getLightLevel = Class.forName("DynamicLights")
    // .getMethod("getLightLevel", ItemStack.class);
    // int levelMain = (int) getLightLevel.invoke(null, offhand);
    // ItemStack stackHead = player.getEquipmentInSlot(4);
    // int levelHead = (int) getLightLevel.invoke(null, stackHead);
    // return Math.max(levelMain, levelHead);
    // } catch (Exception ignored) {}
    // }
    // return returned;
    // }

}
