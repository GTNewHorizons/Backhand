package xonin.backhand.mixins.early;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xonin.backhand.client.utils.BackhandRenderHelper;

@Mixin(ModelBiped.class)
public abstract class MixinModelBiped {

    @Inject(
        method = "setRotationAngles",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelBiped;isSneak:Z", shift = At.Shift.BEFORE))
    private void backhand$moveOffHandArm(float f1, float f2, float f3, float f4, float f5, float f6, Entity entity,
        CallbackInfo ci) {
        BackhandRenderHelper.moveOffHandArm(entity, (ModelBiped) (Object) this, f6);
    }

    // TODO: Decipher this shit & is it even needed in the first place
    // private static void setRotationAngles(ModelBiped b, float p_78087_1_, float p_78087_2_, float p_78087_3_,
    // float p_78087_4_, float p_78087_5_, float p_78087_6_, Entity p_78087_7_) {
    // b.bipedHead.rotateAngleY = p_78087_4_ / (180F / (float) Math.PI);
    // b.bipedHead.rotateAngleX = p_78087_5_ / (180F / (float) Math.PI);
    // b.bipedHeadwear.rotateAngleY = b.bipedHead.rotateAngleY;
    // b.bipedHeadwear.rotateAngleX = b.bipedHead.rotateAngleX;
    // b.bipedRightArm.rotateAngleX = MathHelper.cos(p_78087_1_ * 0.6662F + (float) Math.PI) * 2.0F
    // * p_78087_2_
    // * 0.5F;
    // b.bipedLeftArm.rotateAngleX = MathHelper.cos(p_78087_1_ * 0.6662F) * 2.0F * p_78087_2_ * 0.5F;
    // b.bipedRightArm.rotateAngleZ = 0.0F;
    // b.bipedLeftArm.rotateAngleZ = 0.0F;
    // b.bipedRightLeg.rotateAngleX = MathHelper.cos(p_78087_1_ * 0.6662F) * 1.4F * p_78087_2_;
    // b.bipedLeftLeg.rotateAngleX = MathHelper.cos(p_78087_1_ * 0.6662F + (float) Math.PI) * 1.4F * p_78087_2_;
    // b.bipedRightLeg.rotateAngleY = 0.0F;
    // b.bipedLeftLeg.rotateAngleY = 0.0F;
    //
    // if (b.isRiding) {
    // b.bipedRightArm.rotateAngleX += -((float) Math.PI / 5F);
    // b.bipedLeftArm.rotateAngleX += -((float) Math.PI / 5F);
    // b.bipedRightLeg.rotateAngleX = -((float) Math.PI * 2F / 5F);
    // b.bipedLeftLeg.rotateAngleX = -((float) Math.PI * 2F / 5F);
    // b.bipedRightLeg.rotateAngleY = ((float) Math.PI / 10F);
    // b.bipedLeftLeg.rotateAngleY = -((float) Math.PI / 10F);
    // }
    //
    // if (b.heldItemLeft != 0) {
    // b.bipedLeftArm.rotateAngleX = b.bipedLeftArm.rotateAngleX * 0.5F
    // - ((float) Math.PI / 10F) * (float) b.heldItemLeft;
    // }
    //
    // if (b.heldItemRight != 0) {
    // b.bipedRightArm.rotateAngleX = b.bipedRightArm.rotateAngleX * 0.5F
    // - ((float) Math.PI / 10F) * (float) b.heldItemRight;
    // }
    //
    // b.bipedRightArm.rotateAngleY = 0.0F;
    // b.bipedLeftArm.rotateAngleY = 0.0F;
    // float f6;
    // float f7;
    //
    // if (b.onGround > -9990.0F) {
    // f6 = b.onGround;
    // b.bipedBody.rotateAngleY = MathHelper.sin(MathHelper.sqrt_float(f6) * (float) Math.PI * 2.0F) * 0.2F;
    // b.bipedRightArm.rotationPointZ = MathHelper.sin(b.bipedBody.rotateAngleY) * 5.0F;
    // b.bipedRightArm.rotationPointX = -MathHelper.cos(b.bipedBody.rotateAngleY) * 5.0F;
    // b.bipedLeftArm.rotationPointZ = -MathHelper.sin(b.bipedBody.rotateAngleY) * 5.0F;
    // b.bipedLeftArm.rotationPointX = MathHelper.cos(b.bipedBody.rotateAngleY) * 5.0F;
    // b.bipedRightArm.rotateAngleY += b.bipedBody.rotateAngleY;
    // b.bipedLeftArm.rotateAngleY += b.bipedBody.rotateAngleY;
    // b.bipedLeftArm.rotateAngleX += b.bipedBody.rotateAngleY;
    // f6 = 1.0F - b.onGround;
    // f6 *= f6;
    // f6 *= f6;
    // f6 = 1.0F - f6;
    // f7 = MathHelper.sin(f6 * (float) Math.PI);
    // float f8 = MathHelper.sin(b.onGround * (float) Math.PI) * -(b.bipedHead.rotateAngleX - 0.7F) * 0.75F;
    // b.bipedRightArm.rotateAngleX = (float) ((double) b.bipedRightArm.rotateAngleX
    // - ((double) f7 * 1.2D + (double) f8));
    // b.bipedRightArm.rotateAngleY += b.bipedBody.rotateAngleY * 2.0F;
    // b.bipedRightArm.rotateAngleZ = MathHelper.sin(b.onGround * (float) Math.PI) * -0.4F;
    // }
    //
    // if (p_78087_7_ instanceof EntityPlayer && (p_78087_7_ != Minecraft.getMinecraft().thePlayer
    // || ((IBackhandPlayer) p_78087_7_).getOffSwingProgress(MysteriumPatchesFixesO.firstPersonFrame) != 0)) {
    // if (onGround2 > -9990.0F) {
    // f6 = onGround2;
    // b.bipedBody.rotateAngleY = MathHelper.sin(MathHelper.sqrt_float(f6) * (float) Math.PI * 2.0F) * 0.2F;
    // b.bipedRightArm.rotationPointZ = MathHelper.sin(b.bipedBody.rotateAngleY) * 5.0F;
    // b.bipedRightArm.rotationPointX = -MathHelper.cos(b.bipedBody.rotateAngleY) * 5.0F;
    // b.bipedLeftArm.rotationPointZ = -MathHelper.sin(b.bipedBody.rotateAngleY) * 5.0F;
    // b.bipedLeftArm.rotationPointX = MathHelper.cos(b.bipedBody.rotateAngleY) * 5.0F;
    // b.bipedRightArm.rotateAngleY += b.bipedBody.rotateAngleY;
    // b.bipedLeftArm.rotateAngleY += b.bipedBody.rotateAngleY;
    // b.bipedLeftArm.rotateAngleX += b.bipedBody.rotateAngleY;
    // f6 = 1.0F - onGround2;
    // f6 *= f6;
    // f6 *= f6;
    // f6 = 1.0F - f6;
    // f7 = MathHelper.sin(f6 * (float) Math.PI);
    // float f8 = MathHelper.sin(onGround2 * (float) Math.PI) * -(b.bipedHead.rotateAngleX - 0.7F) * 0.75F;
    // b.bipedLeftArm.rotateAngleX = (float) ((double) b.bipedLeftArm.rotateAngleX
    // - ((double) f7 * 1.2D + (double) f8));
    // b.bipedLeftArm.rotateAngleY -= b.bipedBody.rotateAngleY * 2.0F;
    // b.bipedLeftArm.rotateAngleZ = -MathHelper.sin(onGround2 * (float) Math.PI) * -0.4F;
    // }
    // }
    //
    // if (b.isSneak) {
    // b.bipedBody.rotateAngleX = 0.5F;
    // b.bipedRightArm.rotateAngleX += 0.4F;
    // b.bipedLeftArm.rotateAngleX += 0.4F;
    // b.bipedRightLeg.rotationPointZ = 4.0F;
    // b.bipedLeftLeg.rotationPointZ = 4.0F;
    // b.bipedRightLeg.rotationPointY = 9.0F;
    // b.bipedLeftLeg.rotationPointY = 9.0F;
    // b.bipedHead.rotationPointY = 1.0F;
    // b.bipedHeadwear.rotationPointY = 1.0F;
    // } else {
    // b.bipedBody.rotateAngleX = 0.0F;
    // b.bipedRightLeg.rotationPointZ = 0.1F;
    // b.bipedLeftLeg.rotationPointZ = 0.1F;
    // b.bipedRightLeg.rotationPointY = 12.0F;
    // b.bipedLeftLeg.rotationPointY = 12.0F;
    // b.bipedHead.rotationPointY = 0.0F;
    // b.bipedHeadwear.rotationPointY = 0.0F;
    // }
    //
    // b.bipedRightArm.rotateAngleZ += MathHelper.cos(p_78087_3_ * 0.09F) * 0.05F + 0.05F;
    // b.bipedLeftArm.rotateAngleZ -= MathHelper.cos(p_78087_3_ * 0.09F) * 0.05F + 0.05F;
    // b.bipedRightArm.rotateAngleX += MathHelper.sin(p_78087_3_ * 0.067F) * 0.05F;
    // b.bipedLeftArm.rotateAngleX -= MathHelper.sin(p_78087_3_ * 0.067F) * 0.05F;
    //
    // if (b.aimedBow) {
    // if (p_78087_7_ instanceof EntityPlayer && p_78087_7_ == Minecraft.getMinecraft().thePlayer
    // && BackhandUtils.getOffhandItem((EntityPlayer) p_78087_7_) != null
    // && ((EntityClientPlayerMP) p_78087_7_).getItemInUse()
    // == BackhandUtils.getOffhandItem((EntityPlayer) p_78087_7_)) {
    // f6 = 0.0F;
    // f7 = 0.0F;
    // b.bipedLeftArm.rotateAngleZ = 0.0F;
    // b.bipedRightArm.rotateAngleZ = 0.0F;
    // b.bipedLeftArm.rotateAngleY = 0.1F + b.bipedHead.rotateAngleY;
    // b.bipedRightArm.rotateAngleY = -0.5F + b.bipedHead.rotateAngleY;
    // b.bipedLeftArm.rotateAngleX = -((float) Math.PI / 2F) + b.bipedHead.rotateAngleX;
    // b.bipedRightArm.rotateAngleX = -((float) Math.PI / 2F) + b.bipedHead.rotateAngleX;
    // b.bipedLeftArm.rotateAngleX -= f6 * 1.2F - f7 * 0.4F;
    // b.bipedRightArm.rotateAngleX -= f6 * 1.2F - f7 * 0.4F;
    // b.bipedLeftArm.rotateAngleZ -= MathHelper.cos(p_78087_3_ * 0.09F) * 0.05F + 0.05F;
    // b.bipedRightArm.rotateAngleZ += MathHelper.cos(p_78087_3_ * 0.09F) * 0.05F + 0.05F;
    // b.bipedLeftArm.rotateAngleX -= MathHelper.sin(p_78087_3_ * 0.067F) * 0.05F;
    // b.bipedRightArm.rotateAngleX += MathHelper.sin(p_78087_3_ * 0.067F) * 0.05F;
    // } else {
    // f6 = 0.0F;
    // f7 = 0.0F;
    // b.bipedRightArm.rotateAngleZ = 0.0F;
    // b.bipedLeftArm.rotateAngleZ = 0.0F;
    // b.bipedRightArm.rotateAngleY = -(0.1F - f6 * 0.6F) + b.bipedHead.rotateAngleY;
    // b.bipedLeftArm.rotateAngleY = 0.1F - f6 * 0.6F + b.bipedHead.rotateAngleY + 0.4F;
    // b.bipedRightArm.rotateAngleX = -((float) Math.PI / 2F) + b.bipedHead.rotateAngleX;
    // b.bipedLeftArm.rotateAngleX = -((float) Math.PI / 2F) + b.bipedHead.rotateAngleX;
    // b.bipedRightArm.rotateAngleX -= f6 * 1.2F - f7 * 0.4F;
    // b.bipedLeftArm.rotateAngleX -= f6 * 1.2F - f7 * 0.4F;
    // b.bipedRightArm.rotateAngleZ += MathHelper.cos(p_78087_3_ * 0.09F) * 0.05F + 0.05F;
    // b.bipedLeftArm.rotateAngleZ -= MathHelper.cos(p_78087_3_ * 0.09F) * 0.05F + 0.05F;
    // b.bipedRightArm.rotateAngleX += MathHelper.sin(p_78087_3_ * 0.067F) * 0.05F;
    // b.bipedLeftArm.rotateAngleX -= MathHelper.sin(p_78087_3_ * 0.067F) * 0.05F;
    // }
    // }
    // }

}
