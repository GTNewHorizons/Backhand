package xonin.backhand.mixins.early.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.IBackhandPlayer;
import xonin.backhand.client.ClientEventHandler;
import xonin.backhand.client.utils.BackhandClientUtils;
import xonin.backhand.client.utils.BackhandRenderHelper;
import xonin.backhand.utils.BackhandConfig;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    @Inject(method = "renderItemInFirstPerson", at = @At("RETURN"))
    private void backhand$renderItemInFirstPerson(float frame, CallbackInfo ci) {
        if (BackhandClientUtils.offhandFPRender) return;

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        ClientEventHandler.renderingPlayer = player;

        ItemStack mainhandItem = player.getCurrentEquippedItem();
        ItemStack offhandItem = BackhandUtils.getOffhandItem(player);
        if (!BackhandConfig.general.EmptyOffhand && !BackhandConfig.client.RenderEmptyOffhandAtRest
            && offhandItem == null) {
            return;
        }
        if (offhandItem == null && !BackhandConfig.client.RenderEmptyOffhandAtRest
            && ((IBackhandPlayer) player).getOffSwingProgress(frame) == 0) {
            return;
        }
        if (mainhandItem != null && mainhandItem.getItem() instanceof ItemMap) {
            return;
        }

        BackhandClientUtils.firstPersonFrame = frame;
        BackhandRenderHelper.itemRenderer.updateEquippedItem();
        BackhandClientUtils.offhandFPRender = true;
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_FRONT);
        BackhandRenderHelper.renderOffhandItem((ItemRenderer) (Object) this, frame);
        GL11.glCullFace(GL11.GL_BACK);
        BackhandClientUtils.offhandFPRender = false;
    }
}
