package xonin.backhand.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import xonin.backhand.api.core.BackhandUtils;

public enum EnumHand {

    MAIN_HAND,
    OFF_HAND;

    public ItemStack getItem(EntityPlayer player) {
        return switch (this) {
            case MAIN_HAND -> player.inventory.getCurrentItem();
            case OFF_HAND -> BackhandUtils.getOffhandItem(player);
        };
    }

    public static final EnumHand[] HANDS = { EnumHand.MAIN_HAND, EnumHand.OFF_HAND };
    public static final EnumHand[] HANDS_REV = { EnumHand.OFF_HAND, EnumHand.MAIN_HAND };
}
