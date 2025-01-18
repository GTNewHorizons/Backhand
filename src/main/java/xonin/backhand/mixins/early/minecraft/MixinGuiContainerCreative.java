package xonin.backhand.mixins.early.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.creativetab.CreativeTabs;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import xonin.backhand.api.core.BackhandUtils;

@Mixin(GuiContainerCreative.class)
public abstract class MixinGuiContainerCreative {

    @Inject(
        method = "setCurrentCreativeTab",
        at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 1))
    protected void backhand$removeOffhandSlot(CreativeTabs p_147050_1_, CallbackInfo ci,
        @Local GuiContainerCreative.ContainerCreative container) {
        GuiContainerCreative.CreativeSlot slot = (GuiContainerCreative.CreativeSlot) container.inventorySlots
            .get(BackhandUtils.getOffhandSlot(Minecraft.getMinecraft().thePlayer));
        slot.xDisplayPosition = -2000;
        slot.yDisplayPosition = -2000;
    }
}
