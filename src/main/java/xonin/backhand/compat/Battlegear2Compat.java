package xonin.backhand.compat;

import net.minecraft.item.ItemStack;

import mods.battlegear2.api.core.BattlegearUtils;
import xonin.backhand.utils.Mods;

public class Battlegear2Compat {

    /**
     * Whether Battlegear2 considers this stack a weapon (and would thus try to put the player into its parry stance
     * on right click) - covers vanilla swords, Tinkers' Construct swords (which implement Battlegear2's
     * IBattlegearWeapon directly instead of extending ItemSword), and anything else registered with Battlegear2's
     * WeaponRegistry.
     */
    public static boolean isWeapon(ItemStack stack) {
        return Mods.BATTLEGEAR2.isLoaded() && stack != null && BattlegearUtils.isWeapon(stack);
    }
}
