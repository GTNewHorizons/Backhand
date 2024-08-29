package xonin.backhand.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import xonin.backhand.Backhand;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.client.renderer.RenderOffhandPlayer;

public class ClientEventHandler {

    public static RenderOffhandPlayer renderOffhandPlayer = new RenderOffhandPlayer();
    public static EntityPlayer renderingPlayer;

    @SubscribeEvent
    public void renderHotbarOverlay(RenderGameOverlayEvent event) {
        if (event.type == RenderGameOverlayEvent.ElementType.HOTBAR) {
            Minecraft mc = Minecraft.getMinecraft();
            renderHotbar(
                mc.ingameGUI,
                event.resolution.getScaledWidth(),
                event.resolution.getScaledHeight(),
                event.partialTicks);
        }
    }

    protected void renderHotbar(GuiIngame gui, int width, int height, float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack itemstack = BackhandUtils.getOffhandItem(mc.thePlayer);
        if (itemstack == null) {
            return;
        }

        mc.mcProfiler.startSection("actionBar");

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(new ResourceLocation("textures/gui/widgets.png"));

        gui.drawTexturedModalRect(width / 2 - 125, height - 22, 0, 0, 11, 22);
        gui.drawTexturedModalRect(width / 2 - 125 + 11, height - 22, 182 - 11, 0, 11, 22);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableGUIStandardItemLighting();

        int x = width / 2 - 122;
        int z = height - 16 - 3;
        renderOffhandInventorySlot(x, z, partialTicks);

        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);

        mc.mcProfiler.endSection();
    }

    protected void renderOffhandInventorySlot(int p_73832_2_, int p_73832_3_, float p_73832_4_) {
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack itemstack = BackhandUtils.getOffhandItem(mc.thePlayer);

        if (itemstack != null) {
            float f1 = itemstack.animationsToGo - p_73832_4_;

            if (f1 > 0.0F) {
                GL11.glPushMatrix();
                float f2 = 1.0F + f1 / 5.0F;
                GL11.glTranslatef(p_73832_2_ + 8, p_73832_3_ + 12, 0.0F);
                GL11.glScalef(1.0F / f2, (f2 + 1.0F) / 2.0F, 1.0F);
                GL11.glTranslatef((-(p_73832_2_ + 8)), (-(p_73832_3_ + 12)), 0.0F);
            }

            RenderItem.getInstance()
                .renderItemAndEffectIntoGUI(mc.fontRenderer, mc.getTextureManager(), itemstack, p_73832_2_, p_73832_3_);

            if (f1 > 0.0F) {
                GL11.glPopMatrix();
            }

            RenderItem.getInstance()
                .renderItemOverlayIntoGUI(mc.fontRenderer, mc.getTextureManager(), itemstack, p_73832_2_, p_73832_3_);
        }
    }

    public static int renderPass;

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) {
        renderPass = event.renderPass;
    }

    /**
     * Bend the models when the item in left hand is used
     * And stop the right hand inappropriate bending
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void renderPlayerLeftItemUsage(RenderLivingEvent.Pre event) {
        if (event.entity instanceof EntityPlayer entityPlayer) {
            renderingPlayer = entityPlayer;
            ItemStack offhand = BackhandUtils.getOffhandItem(entityPlayer);
            if (offhand != null && event.renderer instanceof RenderPlayer renderer) {
                renderer.modelArmorChestplate.heldItemLeft = renderer.modelArmor.heldItemLeft = renderer.modelBipedMain.heldItemLeft = 1;
                if (entityPlayer.getItemInUseCount() > 0 && entityPlayer.getItemInUse() == offhand) {
                    EnumAction enumaction = offhand.getItemUseAction();
                    if (enumaction == EnumAction.block) {
                        renderer.modelArmorChestplate.heldItemLeft = renderer.modelArmor.heldItemLeft = renderer.modelBipedMain.heldItemLeft = 3;
                    } else if (enumaction == EnumAction.bow) {
                        renderer.modelArmorChestplate.aimedBow = renderer.modelArmor.aimedBow = renderer.modelBipedMain.aimedBow = true;
                    }
                    ItemStack mainhand = entityPlayer.inventory.getCurrentItem();
                    renderer.modelArmorChestplate.heldItemRight = renderer.modelArmor.heldItemRight = renderer.modelBipedMain.heldItemRight = mainhand
                        != null ? 1 : 0;
                }
            }
        }
    }

    /**
     * Reset models to default values
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void resetPlayerLeftHand(RenderPlayerEvent.Post event) {
        event.renderer.modelArmorChestplate.heldItemLeft = event.renderer.modelArmor.heldItemLeft = event.renderer.modelBipedMain.heldItemLeft = 0;
    }

    @SubscribeEvent
    public void render3rdPersonOffhand(RenderPlayerEvent.Specials.Post event) {
        if (!Backhand.EmptyOffhand && BackhandUtils.getOffhandItem(event.entityPlayer) == null) {
            return;
        }

        GL11.glPushMatrix();
        ModelBiped biped = event.renderer.modelBipedMain;
        RenderOffhandPlayer.itemRenderer.updateEquippedItem();
        renderOffhandPlayer.updateFovModifierHand();
        RenderOffhandPlayer.itemRenderer
            .renderOffhandItemIn3rdPerson(event.entityPlayer, biped, event.partialRenderTick);
        GL11.glPopMatrix();
    }
}
