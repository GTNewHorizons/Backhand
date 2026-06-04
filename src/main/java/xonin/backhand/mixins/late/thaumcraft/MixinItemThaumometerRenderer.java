package xonin.backhand.mixins.late.thaumcraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;
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
    private int backhand$skipArm = -1; // -1 = default rendering, 1 = skip right arm, 0 = skip left arm

    @Unique
    private int backhand$armCallCount = 0;

    @Inject(
        method = "renderItem",
        remap = true,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/TextureManager;bindTexture(Lnet/minecraft/util/ResourceLocation;)V"))
    private void backhand$beforeArmLoop(IItemRenderer.ItemRenderType type, ItemStack item, Object[] data,
        CallbackInfo ci) {
        if (type != IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON) return;

        ItemStack mainhand = Minecraft.getMinecraft().entityRenderer.itemRenderer.itemToRender;
        ItemStack offhand = BackhandRenderHelper.itemRenderer.itemToRender;

        boolean offhandThaumometer = ItemStack.areItemStacksEqual(offhand, item);
        boolean mainhandThaumometer = ItemStack.areItemStacksEqual(mainhand, item);

        if (mainhandThaumometer && offhandThaumometer) {
            backhand$skipArm = -1;
        } else if (offhandThaumometer) {
            backhand$skipArm = 1;
        } else if (mainhandThaumometer && offhand != null) {
            backhand$skipArm = 0;
        } else {
            backhand$skipArm = -1;
        }

        GL11.glDepthRange(0.0, 0.1); // forces the Thaumometer to render in front of the other hand

        backhand$armCallCount = 0;
    }

    @Redirect(
        method = "renderItem",
        remap = true,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/RenderPlayer;renderFirstPersonArm(Lnet/minecraft/entity/player/EntityPlayer;)V"))
    private void backhand$redirectArmRender(RenderPlayer renderPlayer, EntityPlayer player) {
        if (backhand$armCallCount != backhand$skipArm) {
            renderPlayer.renderFirstPersonArm(player);
        }
        backhand$armCallCount++;
    }

    @Inject(method = "renderItem", at = @At("RETURN"))
    private void backhand$onReturnCleanup(IItemRenderer.ItemRenderType type, ItemStack item, Object[] data,
        CallbackInfo ci) {
        if (type != IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON) return;
        GL11.glDepthRange(0.0, 1.0);
        backhand$armCallCount = 0;
    }
}
