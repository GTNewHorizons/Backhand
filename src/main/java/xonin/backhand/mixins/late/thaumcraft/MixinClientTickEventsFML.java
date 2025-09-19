package xonin.backhand.mixins.late.thaumcraft;

import static xonin.backhand.api.core.EnumHand.MAIN_HAND;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;

import cpw.mods.fml.common.gameevent.TickEvent;
import thaumcraft.client.lib.ClientTickEventsFML;
import thaumcraft.common.items.relics.ItemSanityChecker;
import thaumcraft.common.items.wands.ItemWandCasting;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.IBackhandPlayer;

@Mixin(value = ClientTickEventsFML.class, remap = false)
public abstract class MixinClientTickEventsFML {

    @Shadow
    protected abstract void renderCastingWandHud(Float partialTicks, EntityPlayer player, long time,
        ItemStack wandstack);

    @Shadow
    protected abstract void renderSanityHud(Float partialTicks, EntityPlayer player, long time);

    @WrapWithCondition(
        method = "playerTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayer;setItemInUse(Lnet/minecraft/item/ItemStack;I)V",
            remap = true))
    private boolean backhand$fixWandContinousFire(EntityPlayer player, ItemStack p_71008_1_, int p_71008_2_) {
        if (((IBackhandPlayer) player).isOffhandItemInUse()) {
            player.setItemInUse(BackhandUtils.getOffhandItem(player), player.getItemInUseCount());
            return false;
        }
        return true;
    }

    @Inject(
        method = "renderTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/InventoryPlayer;getCurrentItem()Lnet/minecraft/item/ItemStack;",
            remap = true,
            ordinal = 0))
    private void backhand$fixWandHudRender(TickEvent.RenderTickEvent event, CallbackInfo ci, @Local long time) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        ItemStack main = MAIN_HAND.getItem(player);
        ItemStack off = BackhandUtils.getOffhandItem(player);

        if (off != null && (main == null
            || !(main.getItem() instanceof ItemWandCasting || main.getItem() instanceof ItemSanityChecker))) {
            if (off.getItem() instanceof ItemWandCasting) {
                renderCastingWandHud(event.renderTickTime, player, time, off);
            } else if (off.getItem() instanceof ItemSanityChecker) {
                renderSanityHud(event.renderTickTime, player, time);
            }
        }
    }
}
