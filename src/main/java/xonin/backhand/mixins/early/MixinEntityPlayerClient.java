package xonin.backhand.mixins.early;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import xonin.backhand.api.core.IBackhandPlayer;
import xonin.backhand.client.utils.BackhandClientUtils;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayerClient extends EntityLivingBase implements IBackhandPlayer {

    private MixinEntityPlayerClient(World p_i1594_1_) {
        super(p_i1594_1_);
    }

    @Inject(method = "getItemIcon", at = @At(value = "HEAD"))
    private void backhand2$getItemIcon(ItemStack itemStackIn, int p_70620_2_, CallbackInfoReturnable<IIcon> cir) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (itemStackIn == player.getCurrentEquippedItem() && player.getCurrentEquippedItem() != null
            && player.getItemInUse() != null
            && player.getCurrentEquippedItem()
                .getItem() instanceof ItemBow
            && player.getCurrentEquippedItem() != player.getItemInUse()) {
            BackhandClientUtils.disableMainhandAnimation = true;
        }
    }

    @Override
    public float getSwingProgress(float partialTicks) {
        if (BackhandClientUtils.offhandFPRender) {
            return getOffSwingProgress(partialTicks);
        }
        return super.getSwingProgress(partialTicks);
    }
}
