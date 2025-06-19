package xonin.backhand.mixins.late.draconicevolution;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.brandon3055.brandonscore.client.gui.guicomponents.GUIBase;
import com.brandon3055.draconicevolution.client.gui.componentguis.GUIToolConfig;

@Mixin(value = GUIToolConfig.class, remap = false)
public abstract class MixinGuiToolConfig extends GUIBase {

    @Shadow
    public EntityPlayer player;

    public MixinGuiToolConfig(Container container, int xSize, int ySize) {
        super(container, xSize, ySize);
    }

    @ModifyConstant(method = "addDependentComponents", constant = @Constant(intValue = 39))
    private int backhand$addDependentComponents(int constant) {
        return player.inventory.getSizeInventory() - 1;
    }
}
