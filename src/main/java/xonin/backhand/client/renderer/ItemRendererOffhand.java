package xonin.backhand.client.renderer;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;

import org.lwjgl.opengl.GL11;

import xonin.backhand.api.core.BackhandUtils;

public class ItemRendererOffhand extends ItemRenderer {

    public ItemRendererOffhand(Minecraft mc) {
        super(mc);
    }

    public void renderOffhandItemIn3rdPerson(EntityPlayer player, ModelBiped modelBipedMain, float frame) {
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

            if (player.fishEntity != null) {
                offhandItem = new ItemStack(Items.stick);
            }

            EnumAction enumaction = null;

            if (player.getItemInUseCount() > 0) {
                enumaction = offhandItem.getItemUseAction();
            }

            IItemRenderer customRenderer = MinecraftForgeClient
                .getItemRenderer(offhandItem, IItemRenderer.ItemRenderType.EQUIPPED);
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
                    this.renderItem(player, offhandItem, k);
                }
            } else {
                k = offhandItem.getItem()
                    .getColorFromItemStack(offhandItem, 0);
                float f11 = (float) (k >> 16 & 255) / 255.0F;
                f12 = (float) (k >> 8 & 255) / 255.0F;
                f3 = (float) (k & 255) / 255.0F;
                GL11.glColor4f(f11, f12, f3, 1.0F);
                this.renderItem(player, offhandItem, 0);
            }

            GL11.glPopMatrix();
        }
    }

    public void updateEquippedItem() {
        this.prevEquippedProgress = this.equippedProgress;
        EntityClientPlayerMP player = this.mc.thePlayer;
        ItemStack itemstack = BackhandUtils.getOffhandItem(player);
        boolean flag = itemstack == this.itemToRender;

        if (itemstack != null && this.itemToRender != null
            && itemstack != this.itemToRender
            && itemstack.getItem() == this.itemToRender.getItem()
            && itemstack.getItemDamage() == this.itemToRender.getItemDamage()) {
            this.itemToRender = itemstack;
            flag = true;
        }

        float f = 0.4F;
        float f1 = flag ? 1.0F : 0.0F;
        float f2 = f1 - this.equippedProgress;

        if (f2 < -f) {
            f2 = -f;
        }

        if (f2 > f) {
            f2 = f;
        }

        this.equippedProgress += f2 / 2.0F;

        if (this.equippedProgress < 0.1F) {
            this.itemToRender = itemstack;
        }
    }
}
