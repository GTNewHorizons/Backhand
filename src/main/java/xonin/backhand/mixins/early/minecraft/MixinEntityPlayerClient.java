package xonin.backhand.mixins.early.minecraft;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import xonin.backhand.api.core.IBackhandPlayer;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayerClient extends EntityLivingBase implements IBackhandPlayer {

    @Shadow
    public InventoryPlayer inventory;

    public MixinEntityPlayerClient(World p_i1594_1_) {
        super(p_i1594_1_);
    }

    @Override
    public float getSwingProgress(float p_70678_1_) {
        if (isUsingOffhand()) {
            return getOffSwingProgress(p_70678_1_);
        }
        return super.getSwingProgress(p_70678_1_);
    }
}
