package xonin.backhand.client.utils;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;

import org.lwjgl.opengl.GL11;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.IBackhandPlayer;
import xonin.backhand.compat.IOffhandRenderOptOut;

public final class BackhandRenderHelper {

    public static final ItemRenderer itemRenderer = new ItemRenderer(Minecraft.getMinecraft());
    public static float firstPersonFrame;

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

    public static void renderOffhandItemIn3rdPerson(EntityPlayer player, ModelBiped modelBipedMain, float frame) {
        ItemStack offhandItem = BackhandUtils.getOffhandItem(player);
        float f2;
        float f4;

        if (offhandItem != null) {
            GL11.glPushMatrix();
            modelBipedMain.bipedLeftArm.postRender(0.0625F);
            GL11.glTranslatef(
                -modelBipedMain.bipedLeftArm.rotationPointX * 0.0625F,
                -modelBipedMain.bipedLeftArm.rotationPointY * 0.0625F,
                -modelBipedMain.bipedLeftArm.rotationPointZ * 0.0625F);
            GL11.glScalef(-1, 1, 1);
            GL11.glTranslatef(
                -modelBipedMain.bipedLeftArm.rotationPointX * 0.0625F,
                modelBipedMain.bipedLeftArm.rotationPointY * 0.0625F,
                -modelBipedMain.bipedLeftArm.rotationPointZ * 0.0625F);

            GL11.glTranslatef(-0.0625F, 0.4375F, 0.0625F);

            if (player.fishEntity != null && offhandItem.getItem() == Items.fishing_rod) {
                offhandItem = new ItemStack(Items.stick);
            }

            EnumAction enumaction = null;

            if (player.getItemInUseCount() > 0) {
                enumaction = offhandItem.getItemUseAction();
            }

            IItemRenderer customRenderer = MinecraftForgeClient
                .getItemRenderer(offhandItem, IItemRenderer.ItemRenderType.EQUIPPED);
            if (customRenderer instanceof IOffhandRenderOptOut) {
                GL11.glPopMatrix();
                return;
            }
            boolean is3D = (customRenderer != null && customRenderer.shouldUseRenderHelper(
                IItemRenderer.ItemRenderType.EQUIPPED,
                offhandItem,
                IItemRenderer.ItemRendererHelper.BLOCK_3D));

            if (is3D || offhandItem.getItem() instanceof ItemBlock && RenderBlocks.renderItemIn3d(
                Block.getBlockFromItem(offhandItem.getItem())
                    .getRenderType())) {
                f2 = 0.5F;
                GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
                f2 *= 0.75F;
                GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
                GL11.glScalef(-f2, -f2, f2);
            } else if (offhandItem.getItem() == Items.bow) {
                f2 = 0.625F;
                GL11.glTranslatef(0.0F, 0.125F, 0.3125F);
                GL11.glRotatef(-20.0F, 0.0F, 1.0F, 0.0F);
                GL11.glScalef(f2, -f2, f2);
                GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            } else if (offhandItem.getItem()
                .isFull3D()) {
                    f2 = 0.625F;

                    if (offhandItem.getItem()
                        .shouldRotateAroundWhenRendering()) {
                        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
                        GL11.glTranslatef(0.0F, -0.125F, 0.0F);
                    }

                    if (player.getItemInUseCount() > 0 && enumaction == EnumAction.block) {
                        GL11.glTranslatef(0.05F, 0.0F, -0.1F);
                        GL11.glRotatef(-50.0F, 0.0F, 1.0F, 0.0F);
                        GL11.glRotatef(-10.0F, 1.0F, 0.0F, 0.0F);
                        GL11.glRotatef(-60.0F, 0.0F, 0.0F, 1.0F);
                    }

                    GL11.glTranslatef(0.0F, 0.1875F, 0.0F);
                    GL11.glScalef(f2, -f2, f2);
                    GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
                    GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
                } else {
                    f2 = 0.375F;
                    GL11.glTranslatef(0.25F, 0.1875F, -0.1875F);
                    GL11.glScalef(f2, f2, f2);
                    GL11.glRotatef(60.0F, 0.0F, 0.0F, 1.0F);
                    GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
                    GL11.glRotatef(20.0F, 0.0F, 0.0F, 1.0F);
                }

            float f3;
            int k;
            float f12;

            if (offhandItem.getItem()
                .requiresMultipleRenderPasses()) {
                for (k = 0; k < offhandItem.getItem()
                    .getRenderPasses(offhandItem.getItemDamage()); ++k) {
                    int i = offhandItem.getItem()
                        .getColorFromItemStack(offhandItem, k);
                    f12 = (float) (i >> 16 & 255) / 255.0F;
                    f3 = (float) (i >> 8 & 255) / 255.0F;
                    f4 = (float) (i & 255) / 255.0F;
                    GL11.glColor4f(f12, f3, f4, 1.0F);
                    itemRenderer.renderItem(player, offhandItem, k);
                }
            } else {
                k = offhandItem.getItem()
                    .getColorFromItemStack(offhandItem, 0);
                float f11 = (float) (k >> 16 & 255) / 255.0F;
                f12 = (float) (k >> 8 & 255) / 255.0F;
                f3 = (float) (k & 255) / 255.0F;
                GL11.glColor4f(f11, f12, f3, 1.0F);
                itemRenderer.renderItem(player, offhandItem, 0);
            }

            GL11.glPopMatrix();
        }
    }

    public static void drawItemStackSlot(int x, int y) {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(Gui.statIcons);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x + 1, y + 1 + 18, 0, 0 * 0.0078125f, 18 * 0.0078125f);
        tessellator.addVertexWithUV(x + 1 + 18, y + 1 + 18, 0, 18 * 0.0078125f, 18 * 0.0078125f);
        tessellator.addVertexWithUV(x + 1 + 18, y + 1, 0, 18 * 0.0078125f, 0 * 0.0078125f);
        tessellator.addVertexWithUV(x + 1, y + 1, 0, 0 * 0.0078125f, 0 * 0.0078125f);
        tessellator.draw();
    }
}
