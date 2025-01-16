package xonin.backhand.mixins.early.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.IBackhandPlayer;
import xonin.backhand.api.core.IOffhandInventory;
import xonin.backhand.client.utils.BackhandRenderHelper;
import xonin.backhand.utils.BackhandConfig;
import xonin.backhand.utils.BackhandConfigClient;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    @Inject(method = "renderItemInFirstPerson", at = @At("RETURN"))
    private void backhand$renderItemInFirstPerson(float frame, CallbackInfo ci) {
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        if (player.inventory.currentItem == IOffhandInventory.OFFHAND_HOTBAR_SLOT) return;

        ItemStack mainhandItem = player.getCurrentEquippedItem();
        ItemStack offhandItem = BackhandUtils.getOffhandItem(player);
        if (!BackhandConfig.EmptyOffhand && !BackhandConfigClient.RenderEmptyOffhandAtRest && offhandItem == null) {
            return;
        }
        if (offhandItem == null && !BackhandConfigClient.RenderEmptyOffhandAtRest
            && ((IBackhandPlayer) player).getOffSwingProgress(frame) == 0) {
            return;
        }
        if (mainhandItem != null && mainhandItem.getItem() instanceof ItemMap) {
            return;
        }

        BackhandRenderHelper.firstPersonFrame = frame;
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_FRONT);
        GL11.glPushMatrix();
        GL11.glScalef(-1, 1, 1);
        float f3 = player.prevRenderArmPitch + (player.renderArmPitch - player.prevRenderArmPitch) * frame;
        float f4 = player.prevRenderArmYaw + (player.renderArmYaw - player.prevRenderArmYaw) * frame;
        GL11.glRotatef((player.rotationPitch - f3) * -0.1F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef((player.rotationYaw - f4) * -0.1F, 0.0F, 1.0F, 0.0F);
        BackhandUtils
            .useOffhandItem(player, false, () -> BackhandRenderHelper.itemRenderer.renderItemInFirstPerson(frame));
        GL11.glPopMatrix();
        GL11.glCullFace(GL11.GL_BACK);
    }

}
