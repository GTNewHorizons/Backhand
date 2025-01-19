package xonin.backhand.mixins.early.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.client.utils.BackhandRenderHelper;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Inject(
        method = "updateRenderer",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;updateEquippedItem()V"))
    private void backhand$updateOffhandItem(CallbackInfo ci) {
        BackhandUtils.useOffhandItem(
            Minecraft.getMinecraft().thePlayer,
            false,
            BackhandRenderHelper.itemRenderer::updateEquippedItem);
    }
}
