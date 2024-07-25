package net.tclproject.mysteriumlib.asm.fixes;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import cpw.mods.fml.common.Optional;
import invtweaks.InvTweaksContainerManager;
import invtweaks.InvTweaksContainerSectionManager;
import invtweaks.api.container.ContainerSection;

public class MysteriumPatchesFixesO {

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
