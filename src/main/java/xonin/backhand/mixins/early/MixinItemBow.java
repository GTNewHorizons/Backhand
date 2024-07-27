package xonin.backhand.mixins.early;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.util.IIcon;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import xonin.backhand.client.utils.BackhandClientUtils;

@Mixin(ItemBow.class)
public abstract class MixinItemBow extends Item {

    @SideOnly(Side.CLIENT)
    @ModifyReturnValue(method = "getItemIconForUseDuration", at = @At("RETURN"))
    private IIcon backhand$cancelAnimation(IIcon original) {
        if (BackhandClientUtils.disableMainhandAnimation) {
            BackhandClientUtils.disableMainhandAnimation = false;
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            return getIcon(player.getCurrentEquippedItem(), 0, player, player.getItemInUse(), 0);
        }
        return original;
    }
}
