package xonin.backhand.hooks;

import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.utils.BackhandConfigClient;

public class TorchHandler {

    public static boolean shouldPlace(ItemStack mainhandStack, ItemStack offhandStack) {
        // No item in offhand
        if (offhandStack == null || offhandStack.stackSize <= 0) return false;

        Item offhandItem = offhandStack.getItem();
        if (offhandItem == null) return false;

        boolean foundItem = false;
        for (int i = 0; i < BackhandConfigClient.torchConfig.torch_items.length; i++) {
            foundItem = offhandItem.delegate.name()
                .equals(BackhandConfigClient.torchConfig.torch_items[i]);
            if (foundItem) break;
        }
        if (!foundItem) return true;

        if (BackhandConfigClient.torchConfig.offhandTorchWithToolOnly) {
            if (mainhandStack == null || mainhandStack.stackSize <= 0) return false;
            Item mainItem = mainhandStack.getItem();
            if (!BackhandUtils.isItemTool(mainItem)) {
                return false;
            }
        }

        if (BackhandConfigClient.torchConfig.noTorchAtAll) return false;

        if (BackhandConfigClient.torchConfig.noLastTorch && offhandStack.stackSize == 1) return false;

        if (BackhandConfigClient.torchConfig.noTorchWithBlock && mainhandStack != null
            && mainhandStack.getItem() instanceof ItemBlock) {
            return false;
        }

        if (BackhandConfigClient.torchConfig.noTorchWithEmpty
            && (mainhandStack == null || mainhandStack.stackSize <= 0)) {
            return false;
        }

        if (BackhandConfigClient.torchConfig.noTorchWithFood && mainhandStack != null
            && mainhandStack.getItem() instanceof ItemFood) {
            return false;
        }

        return true;
    }

}
