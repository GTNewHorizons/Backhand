package xonin.backhand.mixins.early.minecraft;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import xonin.backhand.api.core.BackhandUtils;

@Mixin(RenderPlayer.class)
public class MixinRenderPlayer {

    @Shadow
    public ModelBiped modelBipedMain;

    @WrapOperation(
        method = "renderEquippedItems(Lnet/minecraft/client/entity/AbstractClientPlayer;F)V",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/entity/AbstractClientPlayer;fishEntity:Lnet/minecraft/entity/projectile/EntityFishHook;"))
    private EntityFishHook backhand$checkOffhandInteract(AbstractClientPlayer player,
        Operation<EntityFishHook> original, @Local(name = "itemstack1") ItemStack stack) {
        ItemStack offhand = BackhandUtils.getOffhandItem(player);
        if (stack.getItem() != Items.fishing_rod && offhand != null && offhand.getItem() == Items.fishing_rod) {
            return null;
        }
        return original.call(player);
    }

    @WrapOperation(
        method = "shouldRenderPass(Lnet/minecraft/client/entity/AbstractClientPlayer;IF)I",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/client/ForgeHooksClient;getArmorModel(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;ILnet/minecraft/client/model/ModelBiped;)Lnet/minecraft/client/model/ModelBiped;",
            remap = false))
    private ModelBiped backhand$copyHeldItemToCustomArmorModel(EntityLivingBase entity, ItemStack stack, int slot,
        ModelBiped _default, Operation<ModelBiped> original) {
        ModelBiped model = original.call(entity, stack, slot, _default);
        if (model != _default) {
            model.heldItemLeft = modelBipedMain.heldItemLeft;
            model.heldItemRight = modelBipedMain.heldItemRight;
            model.aimedBow = modelBipedMain.aimedBow;
            model.isSneak = modelBipedMain.isSneak;
        }
        return model;
    }
}
