package xonin.backhand.mixins.early.minecraft;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.IBackhandPlayer;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayerClient extends EntityLivingBase implements IBackhandPlayer {

    @Shadow
    private ItemStack itemInUse;

    @Shadow
    private int itemInUseCount;

    public MixinEntityPlayerClient(World p_i1594_1_) {
        super(p_i1594_1_);
    }

    @Override
    public float getSwingProgress(float p_70678_1_) {
        if (isUsingOffhand()) {
            return getOffSwingProgress(p_70678_1_);
        }
        return super.getSwingProgress(p_70678_1_);
    }

    @ModifyReturnValue(
        method = "getItemIcon",
        at = { @At(value = "RETURN", ordinal = 0), @At(value = "RETURN", ordinal = 1),
            @At(value = "RETURN", ordinal = 2) })
    private IIcon backhand$setItemInUse(IIcon original, @Local(argsOnly = true) ItemStack stack,
        @Local(argsOnly = true) int renderPass) {
        EntityPlayer player = (EntityPlayer) (Object) this;
        ItemStack offhand = BackhandUtils.getOffhandItem(player);
        if (offhand == null) return original;
        if (!isOffhandItemInUse() && stack == offhand) {
            return stack.getItem()
                .getIcon(stack, renderPass, player, itemInUse, itemInUseCount);
        }
        return original;
    }
}
