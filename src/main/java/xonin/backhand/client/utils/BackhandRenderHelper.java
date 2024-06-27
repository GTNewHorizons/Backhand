package xonin.backhand.client.utils;

import java.nio.FloatBuffer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import xonin.backhand.api.core.IBackhandPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

public final class BackhandRenderHelper {

    public static final float RENDER_UNIT = 1F / 16F;// 0.0625

    private static final ResourceLocation ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");

    public static final float[] arrowX = new float[64];
    public static final float[] arrowY = new float[arrowX.length];
    public static final float[] arrowDepth = new float[arrowX.length];
    public static final float[] arrowPitch = new float[arrowX.length];
    public static final float[] arrowYaw = new float[arrowX.length];

    static {
        for (int i = 0; i < arrowX.length; i++) {
            double r = Math.random() * 5;
            double theta = Math.random() * Math.PI * 2;

            arrowX[i] = (float) (r * Math.cos(theta));
            arrowY[i] = (float) (r * Math.sin(theta));
            arrowDepth[i] = (float) (Math.random() * 0.5 + 0.5F);

            arrowPitch[i] = (float) (Math.random() * 50 - 25);
            arrowYaw[i] = (float) (Math.random() * 50 - 25);
        }
    }

    private static FloatBuffer colorBuffer = GLAllocation.createDirectFloatBuffer(16);
    public static boolean renderingItem;
    private static final Vec3 field_82884_b = Vec3.createVectorHelper(0.20000000298023224D, 1.0D, -0.699999988079071D)
        .normalize();
    private static final Vec3 field_82885_c = Vec3.createVectorHelper(-0.20000000298023224D, 1.0D, 0.699999988079071D)
        .normalize();

    public static void moveOffHandArm(Entity entity, ModelBiped biped, float frame) {
        if (entity instanceof IBackhandPlayer) {
            IBackhandPlayer player = (IBackhandPlayer) entity;
            float offhandSwing = 0.0F;
            offhandSwing = player.getOffSwingProgress(frame);

            if (offhandSwing > 0.0F) {
                if (biped.bipedBody.rotateAngleY != 0.0F) {
                    biped.bipedLeftArm.rotateAngleY -= biped.bipedBody.rotateAngleY;
                    biped.bipedLeftArm.rotateAngleX -= biped.bipedBody.rotateAngleY;
                }
                biped.bipedBody.rotateAngleY = -MathHelper
                    .sin(MathHelper.sqrt_float(offhandSwing) * (float) Math.PI * 2.0F) * 0.2F;

                // biped.bipedRightArm.rotationPointZ = MathHelper.sin(biped.bipedBody.rotateAngleY) * 5.0F;
                // biped.bipedRightArm.rotationPointX = -MathHelper.cos(biped.bipedBody.rotateAngleY) * 5.0F;

                biped.bipedLeftArm.rotationPointZ = -MathHelper.sin(biped.bipedBody.rotateAngleY) * 5.0F;
                biped.bipedLeftArm.rotationPointX = MathHelper.cos(biped.bipedBody.rotateAngleY) * 5.0F;

                // biped.bipedRightArm.rotateAngleY += biped.bipedBody.rotateAngleY;
                // biped.bipedRightArm.rotateAngleX += biped.bipedBody.rotateAngleY;
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

    public static void applyColorFromItemStack(ItemStack itemStack, int pass) {
        int col = itemStack.getItem()
            .getColorFromItemStack(itemStack, pass);
        float r = (float) (col >> 16 & 255) / 255.0F;
        float g = (float) (col >> 8 & 255) / 255.0F;
        float b = (float) (col & 255) / 255.0F;
        GL11.glColor4f(r, g, b, 1.0F);
    }

    @SideOnly(Side.CLIENT)
    public static void renderEnchantmentEffects(Tessellator tessellator) {
        GL11.glDepthFunc(GL11.GL_EQUAL);
        GL11.glDisable(GL11.GL_LIGHTING);
        Minecraft.getMinecraft().renderEngine.bindTexture(ITEM_GLINT);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
        float f7 = 0.76F;
        GL11.glColor4f(0.5F * f7, 0.25F * f7, 0.8F * f7, 1.0F);
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glPushMatrix();
        float f8 = 0.125F;
        GL11.glScalef(f8, f8, f8);
        float f9 = (float) (Minecraft.getSystemTime() % 3000L) / 3000.0F * 8.0F;
        GL11.glTranslatef(f9, 0.0F, 0.0F);
        GL11.glRotatef(-50.0F, 0.0F, 0.0F, 1.0F);
        ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, RENDER_UNIT);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glScalef(f8, f8, f8);
        f9 = (float) (Minecraft.getSystemTime() % 4873L) / 4873.0F * 8.0F;
        GL11.glTranslatef(-f9, 0.0F, 0.0F);
        GL11.glRotatef(10.0F, 0.0F, 0.0F, 1.0F);
        ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, RENDER_UNIT);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
    }

    public static void renderTexturedQuad(int x, int y, float z, int width, int height) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((double) (x + 0), (double) (y + height), (double) z, 0D, 1D);
        tessellator.addVertexWithUV((double) (x + width), (double) (y + height), (double) z, 1D, 1D);
        tessellator.addVertexWithUV((double) (x + width), (double) (y + 0), (double) z, 1D, 0D);
        tessellator.addVertexWithUV((double) (x + 0), (double) (y + 0), (double) z, 0D, 0D);
        tessellator.draw();
    }
}
