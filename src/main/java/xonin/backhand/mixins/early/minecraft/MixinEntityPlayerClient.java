package xonin.backhand.mixins.early.minecraft;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;

import xonin.backhand.api.core.IBackhandPlayer;
import xonin.backhand.client.utils.BackhandRenderHelper;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayerClient extends EntityLivingBase implements IBackhandPlayer {

    private MixinEntityPlayerClient(World p_i1594_1_) {
        super(p_i1594_1_);
    }

    @Override
    public float getSwingProgress(float partialTicks) {
        if (BackhandRenderHelper.offhandFPRender) {
            return getOffSwingProgress(partialTicks);
        }
        return super.getSwingProgress(partialTicks);
    }
}
