package xonin.backhand.mixins.early.minecraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import xonin.backhand.Backhand;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.packet.OffhandSyncItemPacket;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity {

    @Unique
    private ItemStack backhand$previousOffhandStack;

    public MixinEntityLivingBase(World worldIn) {
        super(worldIn);
    }

    @Inject(
        method = "onUpdate",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;areItemStacksEqual(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z",
            ordinal = 0))
    private void backhand$isItemInUseHook(CallbackInfo ci, @Local(name = "j") int index) {
        if (!((EntityLivingBase) (Object) this instanceof EntityPlayer player) || index > 0) return;
        ItemStack offhand = BackhandUtils.getOffhandItem(player);
        if (ItemStack.areItemStacksEqual(backhand$previousOffhandStack, offhand)) return;
        backhand$previousOffhandStack = offhand;
        Backhand.packetHandler.sendPacketToAllTracking(player, new OffhandSyncItemPacket(player).generatePacket());
    }
}
