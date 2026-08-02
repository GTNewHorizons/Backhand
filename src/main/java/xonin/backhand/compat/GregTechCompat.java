package xonin.backhand.compat;

import net.minecraft.item.ItemStack;

import gregtech.common.tools.ToolVajra;

public class GregTechCompat {

    /**
     * Whether this stack is a GregTech Vajra, whose right click always looks "handled" to Backhand.
     */
    public static boolean isVajra(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ToolVajra;
    }
}
