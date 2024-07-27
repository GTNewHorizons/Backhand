package xonin.backhand.mixins.early;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import xonin.backhand.api.core.BackhandUtils;

@Mixin(EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP extends EntityPlayer {

    public MixinEntityPlayerMP(World p_i45324_1_, GameProfile p_i45324_2_) {
        super(p_i45324_1_, p_i45324_2_);
    }

    @Inject(method = "sendContainerAndContentsToPlayer", at = @At(value = "TAIL"))
    private void backhand$syncOffhand(Container p_71110_1_, List<ItemStack> p_71110_2_, CallbackInfo ci) {
        BackhandUtils.getOffhandEP(this).syncOffhand = true;
    }
}
