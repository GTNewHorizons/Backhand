package xonin.backhand.mixins.early.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import xonin.backhand.api.core.IBackhandPlayer;
import xonin.backhand.client.utils.BackhandRenderHelper;

@Mixin(ModelBiped.class)
public abstract class MixinModelBiped extends ModelBase {

    @Shadow
    public ModelRenderer bipedHead;
    @Shadow
    public ModelRenderer bipedRightArm;
    @Shadow
    public ModelRenderer bipedLeftArm;

    @Inject(
        method = "setRotationAngles",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelBiped;isSneak:Z", shift = At.Shift.BEFORE))
    private void backhand$moveOffHandArm(float f1, float f2, float f3, float f4, float f5, float f6, Entity entity,
        CallbackInfo ci) {
        BackhandRenderHelper.moveOffHandArm(entity, (ModelBiped) (Object) this, f6);
    }

    @ModifyExpressionValue(
        method = "setRotationAngles",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/model/ModelBiped;aimedBow:Z"))
    private boolean backhand$moveOffhandAimedBow(boolean original, @Local(name = "p_78087_3_") float f3,
        @Local(argsOnly = true) Entity entity) {
        if (entity instanceof EntityPlayer player && entity == Minecraft.getMinecraft().thePlayer
            && ((IBackhandPlayer) player).isOffhandItemInUse()) {
            bipedLeftArm.rotateAngleZ = 0.0F;
            bipedRightArm.rotateAngleZ = 0.0F;
            bipedLeftArm.rotateAngleY = 0.1F + bipedHead.rotateAngleY;
            bipedRightArm.rotateAngleY = -0.5F + bipedHead.rotateAngleY;
            bipedLeftArm.rotateAngleX = -((float) Math.PI / 2F) + bipedHead.rotateAngleX;
            bipedRightArm.rotateAngleX = -((float) Math.PI / 2F) + bipedHead.rotateAngleX;
            bipedLeftArm.rotateAngleX -= 0.4F;
            bipedRightArm.rotateAngleX -= 0.4F;
            bipedLeftArm.rotateAngleZ -= MathHelper.cos(f3 * 0.09F) * 0.05F + 0.05F;
            bipedRightArm.rotateAngleZ += MathHelper.cos(f3 * 0.09F) * 0.05F + 0.05F;
            bipedLeftArm.rotateAngleX -= MathHelper.sin(f3 * 0.067F) * 0.05F;
            bipedRightArm.rotateAngleX += MathHelper.sin(f3 * 0.067F) * 0.05F;
            return false;
        }
        return original;
    }

}
