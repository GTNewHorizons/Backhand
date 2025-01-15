package xonin.backhand.mixins.late.etfuturum;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.core.handlers.ServerEventHandler;
import xonin.backhand.api.core.BackhandUtils;

@Mixin(ServerEventHandler.class)
public abstract class MixinServerEventHandler {

    @Shadow(remap = false)
    public abstract void handleTotemCheck(EntityLivingBase entity, LivingHurtEvent event);

    @Unique
    private boolean backhand$skipCheck;

    @Inject(
        method = "handleTotemCheck",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/EntityLivingBase;getHeldItem()Lnet/minecraft/item/ItemStack;",
            ordinal = 0),
        remap = false,
        cancellable = true)
    private void backhand$useOffhandTotem(EntityLivingBase entity, LivingHurtEvent event, CallbackInfo ci) {
        if (backhand$skipCheck || !(entity instanceof EntityPlayer player)) return;
        ItemStack offhand = BackhandUtils.getOffhandItem(player);
        if (offhand == null) return;
        ItemStack mainhand = player.getHeldItem();
        Item totem = ModItems.TOTEM_OF_UNDYING.get();

        if (totem.equals(offhand.getItem()) && (mainhand == null || totem.equals(mainhand.getItem()))) {
            ci.cancel();
            backhand$skipCheck = true;
            BackhandUtils.useOffhandItem(player, () -> handleTotemCheck(entity, event));
            backhand$skipCheck = false;
        }
    }
}
