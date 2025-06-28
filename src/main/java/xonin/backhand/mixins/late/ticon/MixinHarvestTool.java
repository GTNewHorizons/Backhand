package xonin.backhand.mixins.late.ticon;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import tconstruct.library.tools.HarvestTool;
import xonin.backhand.api.core.BackhandUtils;

@Mixin(HarvestTool.class)
public class MixinHarvestTool {

    @Inject(method = "onItemUse", at = @At("HEAD"), cancellable = true)
    private void backhand$onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float clickX, float clickY, float clickZ, CallbackInfoReturnable<Boolean> cir) {
        if (BackhandUtils.getOffhandItem(player) != null) {
            cir.setReturnValue(false);
        }
    }
}
