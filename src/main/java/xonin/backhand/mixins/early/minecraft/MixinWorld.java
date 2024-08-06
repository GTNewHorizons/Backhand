package xonin.backhand.mixins.early.minecraft;

import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import xonin.backhand.client.world.DummyWorld;

@Mixin(World.class)
public abstract class MixinWorld {

    @Inject(method = "doesBlockHaveSolidTopSurface", at = @At(value = "HEAD"), cancellable = true)
    private static void backhand$allowPlacementInDummyWorld(IBlockAccess worldIn, int x, int y, int z,
        CallbackInfoReturnable<Boolean> cir) {
        if (worldIn instanceof DummyWorld) {
            cir.setReturnValue(true);
        }
    }
}
