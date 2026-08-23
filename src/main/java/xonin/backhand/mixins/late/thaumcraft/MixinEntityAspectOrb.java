package xonin.backhand.mixins.late.thaumcraft;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.entities.EntityAspectOrb;
import thaumcraft.common.items.wands.ItemWandCasting;
import xonin.backhand.api.core.BackhandUtils;

@Mixin(value = EntityAspectOrb.class, remap = false)
public abstract class MixinEntityAspectOrb {

    @WrapOperation(
        method = { "onUpdate", "onCollideWithPlayer" },
        at = @At(
            value = "INVOKE",
            target = "Lthaumcraft/common/lib/utils/InventoryUtils;isWandInHotbarWithRoom(Lthaumcraft/api/aspects/Aspect;ILnet/minecraft/entity/player/EntityPlayer;)I",
            remap = false),
        remap = true)
    private int backhand$includeOffhandWand(Aspect aspect, int amount, EntityPlayer player,
        Operation<Integer> original) {
        int slot = original.call(aspect, amount, player);
        if (slot >= 0) {
            return slot;
        }

        ItemStack offhand = BackhandUtils.getOffhandItem(player);
        if (offhand != null && offhand.getItem() instanceof ItemWandCasting wand
            && wand.addVis(offhand, aspect, amount, false) < amount) {
            return BackhandUtils.getOffhandSlot(player);
        }

        return -1;
    }
}
