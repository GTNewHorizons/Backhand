package xonin.backhand.mixins.late.thaumcraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import thaumcraft.client.renderers.item.ItemThaumometerRenderer;
import xonin.backhand.client.utils.BackhandRenderHelper;

@Mixin(value = ItemThaumometerRenderer.class, remap = false)
public class MixinItemThaumometerRenderer {

    @Unique
    private int skipArm = -1; // -1 = render both, 0 = skip left, 1 = skip right

    @Unique
    private int armCallCount = 0;

    @Inject(
        method = "renderItem",
        remap = true,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/TextureManager;bindTexture(Lnet/minecraft/util/ResourceLocation;)V"))
    private void beforeArmLoop(IItemRenderer.ItemRenderType type, ItemStack item, Object[] data, CallbackInfo ci) {
        if (type != IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON) return;

        ItemStack mainhand = Minecraft.getMinecraft().entityRenderer.itemRenderer.itemToRender;
        ItemStack offhand = BackhandRenderHelper.itemRenderer.itemToRender;

        boolean offhandMatch = ItemStack.areItemStacksEqual(offhand, item);
        boolean mainhandMatch = ItemStack.areItemStacksEqual(mainhand, item);

        if (offhandMatch && mainhandMatch) {
            skipArm = -1;
        } else if (offhandMatch) {
            skipArm = 1;
        } else if (offhand != null) {
            skipArm = 0;
        } else {
            skipArm = -1;
        }

        armCallCount = 0;
    }

    @Redirect(
        method = "renderItem",
        remap = true,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/RenderPlayer;renderFirstPersonArm(Lnet/minecraft/entity/player/EntityPlayer;)V"))
    private void redirectArmRender(RenderPlayer renderPlayer, EntityPlayer player) {
        if (armCallCount != skipArm) {
            renderPlayer.renderFirstPersonArm(player);
        }
        armCallCount++;
    }

    @Inject(method = "renderItem", at = @At("RETURN"))
    private void onReturnCleanup(IItemRenderer.ItemRenderType type, ItemStack item, Object[] data, CallbackInfo ci) {
        skipArm = -1;
        armCallCount = 0;
    }
}
