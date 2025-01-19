package xonin.backhand.mixins.early.minecraft;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.authlib.GameProfile;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.IBackhandPlayer;

@Mixin(EntityOtherPlayerMP.class)
public abstract class MixinEntityOtherPlayerMP extends AbstractClientPlayer implements IBackhandPlayer {

    private MixinEntityOtherPlayerMP(World p_i45074_1_, GameProfile p_i45074_2_) {
        super(p_i45074_1_, p_i45074_2_);
    }

    @Redirect(
        method = "onUpdate",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            args = "array=get",
            target = "Lnet/minecraft/entity/player/InventoryPlayer;mainInventory:[Lnet/minecraft/item/ItemStack;"))
    private ItemStack backhand$isItemInUseHook(ItemStack[] array, int index) {
        if (isOffhandItemInUse()) return BackhandUtils.getOffhandItem(this);
        return array[index];
    }
}
