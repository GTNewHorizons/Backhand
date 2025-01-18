package xonin.backhand.mixins.early.minecraft;

import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.creativetab.CreativeTabs;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

@Mixin(GuiContainerCreative.class)
public abstract class MixinGuiContainerCreative {

    @Inject(
        method = "setCurrentCreativeTab",
        at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 1))
    protected void backhand$removeOffhandSlot(CreativeTabs p_147050_1_, CallbackInfo ci,
        @Local GuiContainerCreative.ContainerCreative container) {
        GuiContainerCreative.CreativeSlot slot = (GuiContainerCreative.CreativeSlot) container.inventorySlots
            .get(container.inventorySlots.size() - 1);
        slot.xDisplayPosition = -2000;
        slot.yDisplayPosition = -2000;
    }
}
