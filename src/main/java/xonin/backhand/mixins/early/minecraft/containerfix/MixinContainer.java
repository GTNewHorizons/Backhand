package xonin.backhand.mixins.early.minecraft.containerfix;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.hooks.containerfix.IContainerHook;

@SuppressWarnings("unused")
@Mixin(Container.class)
public class MixinContainer implements IContainerHook {

    @Unique
    private boolean backhand$openedWithBackhand;

    @Override
    public final boolean backhand$wasOpenedWithBackhand() {
        return backhand$openedWithBackhand;
    }

    @Override
    public final void backhand$setOpenedWithBackhand() {
        backhand$openedWithBackhand = true;
    }

    @Inject(method = "addCraftingToCrafters", at = @At("HEAD"))
    private void backhand$test(ICrafting p_75132_1_, CallbackInfo ci) {
        if (p_75132_1_ instanceof EntityPlayerMP player) {
            if (BackhandUtils.isUsingOffhand(player)) {
                backhand$setOpenedWithBackhand();
            }
        }
    }
}
