package xonin.backhand.compat;

import net.minecraft.item.ItemStack;

import mods.battlegear2.api.core.BattlegearUtils;

public class Battlegear2Compat {

    /**
     * Whether Battlegear2 considers this stack a weapon and would put the player into its parry stance on right
     * click - covers vanilla/Tinkers' Construct swords and anything registered with its WeaponRegistry.
     */
    public static boolean isWeapon(ItemStack stack) {
        return stack != null && BattlegearUtils.isWeapon(stack);
    }
}
