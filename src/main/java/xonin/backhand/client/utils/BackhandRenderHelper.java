package xonin.backhand.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

import org.lwjgl.opengl.GL11;

import xonin.backhand.api.core.IBackhandPlayer;
import xonin.backhand.client.renderer.ItemRendererOffhand;

public final class BackhandRenderHelper {

    public static final ItemRendererOffhand itemRenderer = new ItemRendererOffhand(Minecraft.getMinecraft());
    public static float firstPersonFrame;
    public static boolean offhandFPRender;

    @SuppressWarnings("SuspiciousNameCombination")
    public static void moveOffHandArm(Entity entity, ModelBiped biped, float frame) {
        if (entity instanceof IBackhandPlayer player && (player != Minecraft.getMinecraft().thePlayer
            || player.getOffSwingProgress(BackhandRenderHelper.firstPersonFrame) != 0)) {
            float offhandSwing = player.getOffSwingProgress(frame);

            if (offhandSwing > 0.0F) {
                if (biped.bipedBody.rotateAngleY != 0.0F) {
                    biped.bipedLeftArm.rotateAngleY -= biped.bipedBody.rotateAngleY;
                    biped.bipedLeftArm.rotateAngleX -= biped.bipedBody.rotateAngleY;
                }
                biped.bipedBody.rotateAngleY = -MathHelper
                    .sin(MathHelper.sqrt_float(offhandSwing) * (float) Math.PI * 2.0F) * 0.2F;

                biped.bipedLeftArm.rotationPointZ = -MathHelper.sin(biped.bipedBody.rotateAngleY) * 5.0F;
                biped.bipedLeftArm.rotationPointX = MathHelper.cos(biped.bipedBody.rotateAngleY) * 5.0F;
                float f6 = 1.0F - offhandSwing;
                f6 = 1.0F - f6 * f6 * f6;
                double f8 = MathHelper.sin(f6 * (float) Math.PI) * 1.2D;
                double f10 = MathHelper.sin(offhandSwing * (float) Math.PI) * -(biped.bipedHead.rotateAngleX - 0.7F)
                    * 0.75F;
                biped.bipedLeftArm.rotateAngleX -= f8 + f10;
                biped.bipedLeftArm.rotateAngleY += biped.bipedBody.rotateAngleY * 3.0F;
                biped.bipedLeftArm.rotateAngleZ = MathHelper.sin(offhandSwing * (float) Math.PI) * -0.4F;
            }
        }
    }

    public static void renderOffhandItem(ItemRenderer otherItemRenderer, float frame) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityClientPlayerMP player = mc.thePlayer;

        GL11.glPushMatrix();
        GL11.glScalef(-1, 1, 1);

        ItemStack itemToRender = otherItemRenderer.itemToRender;
        float equippedProgress = otherItemRenderer.equippedProgress;
        float prevEquippedProgress = otherItemRenderer.prevEquippedProgress;

        otherItemRenderer.itemToRender = itemRenderer.itemToRender;
        otherItemRenderer.equippedProgress = itemRenderer.equippedProgress;
        otherItemRenderer.prevEquippedProgress = itemRenderer.prevEquippedProgress;

        float f3 = player.prevRenderArmPitch + (player.renderArmPitch - player.prevRenderArmPitch) * frame;
        float f4 = player.prevRenderArmYaw + (player.renderArmYaw - player.prevRenderArmYaw) * frame;
        GL11.glRotatef((player.rotationPitch - f3) * -0.1F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef((player.rotationYaw - f4) * -0.1F, 0.0F, 1.0F, 0.0F);

        otherItemRenderer.renderItemInFirstPerson(frame);

        otherItemRenderer.itemToRender = itemToRender;
        otherItemRenderer.equippedProgress = equippedProgress;
        otherItemRenderer.prevEquippedProgress = prevEquippedProgress;

        GL11.glPopMatrix();
    }
}
