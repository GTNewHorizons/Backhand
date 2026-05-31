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
import xonin.backhand.api.core.BackhandUtils;

@Mixin(value = ItemThaumometerRenderer.class, remap = false)
public class MixinItemThaumometerRenderer {

    @Unique
    private int skipArm = -1; // -1 = render both, 0 = skip left, 1 = skip right

    @Unique
    private int armCallCount = 0;

    @Inject(
        method = "renderItem",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glScalef(FFF)V", ordinal = 0))
    private void beforeArmLoop(IItemRenderer.ItemRenderType type, ItemStack item, Object[] data, CallbackInfo ci) {
        if (type != IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON) return;

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        ItemStack offhand = BackhandUtils.getOffhandItem(player);
        ItemStack mainhand = player.getHeldItem();

        boolean otherSlotOccupied = offhand != null && mainhand != null;

        // If the other slot has something in it, always render only the right
        // arm (var9=1). Backhand mirrors the whole scene for offhand items,
        // so this naturally becomes the left arm when in the offhand.
        skipArm = otherSlotOccupied ? 0 : -1;
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
