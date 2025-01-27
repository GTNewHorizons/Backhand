package xonin.backhand.client;

import static xonin.backhand.utils.Mods.DOUBLE_WIDE_SURPRISE;

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

import com.gtnewhorizon.gtnhlib.eventbus.EventBusSubscriber;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import invtweaks.InvTweaks;
import xonin.backhand.CommonProxy;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.client.utils.BackhandRenderHelper;
import xonin.backhand.utils.BackhandConfig;
import xonin.backhand.utils.Mods;

@EventBusSubscriber(side = Side.CLIENT)
public class ClientEventHandler {

    public static boolean prevInvTweaksAutoRefill;
    public static boolean prevInvTweaksBreakRefill;

    public static int invTweaksDelay;

    public static int renderPass;

    @SubscribeEvent
    public static void renderHotbarOverlay(RenderGameOverlayEvent event) {
        if (event.type == RenderGameOverlayEvent.ElementType.HOTBAR) {
            Minecraft mc = Minecraft.getMinecraft();
            renderHotbar(
                mc.ingameGUI,
                event.resolution.getScaledWidth(),
                event.resolution.getScaledHeight(),
                event.partialTicks);
        }
    }

    @SubscribeEvent
    public static void onKeyInputEvent(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen == null && CommonProxy.SWAP_KEY.isKeyDown(mc.thePlayer)) {
            invTweaksSwapPatch();
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (invTweaksDelay > 0) {
            invTweaksDelay--;
            if (invTweaksDelay == 0) {
                restoreInvTweaksConfigs();
            }
        }
    }

    private static void renderHotbar(GuiIngame gui, int width, int height, float partialTicks) {
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

        int offset = DOUBLE_WIDE_SURPRISE.isLoaded() ? 212 : 125;
        gui.drawTexturedModalRect(width / 2 - offset, height - 22, 0, 0, 11, 22);
        gui.drawTexturedModalRect(width / 2 - offset + 11, height - 22, 182 - 11, 0, 11, 22);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableGUIStandardItemLighting();

        int x = width / 2 - offset + 3;
        int z = height - 16 - 3;
        renderOffhandInventorySlot(x, z, partialTicks);

        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);

        mc.mcProfiler.endSection();
    }

    private static void renderOffhandInventorySlot(int p_73832_2_, int p_73832_3_, float p_73832_4_) {
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

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        renderPass = event.renderPass;
    }

    /**
     * Bend the models when the item in left hand is used
     * And stop the right hand inappropriate bending
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void renderPlayerLeftItemUsage(RenderLivingEvent.Pre event) {
        if (event.entity instanceof EntityPlayer entityPlayer) {
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
    public static void resetPlayerLeftHand(RenderPlayerEvent.Post event) {
        event.renderer.modelArmorChestplate.heldItemLeft = event.renderer.modelArmor.heldItemLeft = event.renderer.modelBipedMain.heldItemLeft = 0;
    }

    @SubscribeEvent
    public static void render3rdPersonOffhand(RenderPlayerEvent.Specials.Post event) {
        if (!BackhandConfig.EmptyOffhand && BackhandUtils.getOffhandItem(event.entityPlayer) == null) {
            return;
        }

        GL11.glPushMatrix();
        ModelBiped biped = event.renderer.modelBipedMain;
        BackhandRenderHelper.renderOffhandItemIn3rdPerson(event.entityPlayer, biped, event.partialRenderTick);
        GL11.glPopMatrix();
    }

    public static void restoreInvTweaksConfigs() {
        if (!Mods.INV_TWEAKS.isLoaded()) return;
        InvTweaks.getConfigManager()
            .getConfig()
            .setProperty("enableAutoRefill", String.valueOf(prevInvTweaksAutoRefill));
        InvTweaks.getConfigManager()
            .getConfig()
            .setProperty("autoRefillBeforeBreak", String.valueOf(prevInvTweaksBreakRefill));
    }

    public static void invTweaksSwapPatch() {
        if (!Mods.INV_TWEAKS.isLoaded()) return;
        if (invTweaksDelay <= 0) {
            prevInvTweaksAutoRefill = Boolean.parseBoolean(
                InvTweaks.getConfigManager()
                    .getConfig()
                    .getProperty("enableAutoRefill"));
            prevInvTweaksBreakRefill = Boolean.parseBoolean(
                InvTweaks.getConfigManager()
                    .getConfig()
                    .getProperty("autoRefillBeforeBreak"));
            InvTweaks.getConfigManager()
                .getConfig()
                .setProperty("enableAutoRefill", "false");
            InvTweaks.getConfigManager()
                .getConfig()
                .setProperty("autoRefillBeforeBreak", "false");
        }
        invTweaksDelay = 15;
    }
}
