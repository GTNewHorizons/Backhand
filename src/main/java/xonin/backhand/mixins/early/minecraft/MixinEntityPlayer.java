package xonin.backhand.mixins.early.minecraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.IBackhandPlayer;
import xonin.backhand.api.core.InventoryPlayerBackhand;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase implements IBackhandPlayer {

    @Shadow
    private ItemStack itemInUse;
    @Shadow
    private int itemInUseCount;
    @Shadow
    public InventoryPlayer inventory = new InventoryPlayerBackhand((EntityPlayer) (Object) this);
    @Unique
    private float backhand$offHandSwingProgress = 0F;
    @Unique
    private float backhand$prevOffHandSwingProgress = 0F;
    @Unique
    private int backhand$offHandSwingProgressInt = 0;
    @Unique
    private boolean backhand$isOffHandSwingInProgress = false;
    @Unique
    private int backhand$specialActionTimer = 0;

    private MixinEntityPlayer(World p_i1594_1_) {
        super(p_i1594_1_);
    }

    // TODO: Why are we doing this?
    @ModifyReturnValue(method = "isPlayer", at = @At(value = "RETURN"))
    private boolean backhand$isPlayer(boolean original) {
        return false;
    }

    @ModifyExpressionValue(
        method = "onItemUseFinish",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/event/ForgeEventFactory;onItemUseFinish(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;ILnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
            remap = false))
    private ItemStack backhand$onItemUseFinish$beforeFinishUse(ItemStack itemStack) {
        return BackhandUtils.beforeFinishUseEvent(
            (EntityPlayer) (Object) this,
            this.itemInUse,
            this.itemInUseCount,
            itemStack,
            this.itemInUse.stackSize);
    }

    @ModifyExpressionValue(
        method = "onUpdate",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/InventoryPlayer;getCurrentItem()Lnet/minecraft/item/ItemStack;"))
    private ItemStack backhand$onUpdate$getCurrentItem(ItemStack original) {
        ItemStack itemStack = BackhandUtils.getOffhandItem((EntityPlayer) (Object) this);
        if (itemInUse == itemStack) {
            return itemStack;
        }

        return original;
    }

    @Override
    public void attackTargetEntityWithCurrentOffItem(Entity target) {
        BackhandUtils.attackTargetEntityWithCurrentOffItem((EntityPlayer) (Object) this, target);
    }

    @Override
    public void swingOffItem() {
        if (!this.backhand$isOffHandSwingInProgress
            || this.backhand$offHandSwingProgressInt >= this.getArmSwingAnimationEnd() / 2
            || this.backhand$offHandSwingProgressInt < 0) {
            this.backhand$offHandSwingProgressInt = -1;
            this.backhand$isOffHandSwingInProgress = true;
        }

    }

    @Override
    public float getOffSwingProgress(float frame) {
        float diff = this.backhand$offHandSwingProgress - this.backhand$prevOffHandSwingProgress;
        if (diff < 0.0F) {
            ++diff;
        }

        return this.backhand$prevOffHandSwingProgress + diff * frame;
    }

    @Override
    protected void updateArmSwingProgress() {
        super.updateArmSwingProgress();
        this.backhand$prevOffHandSwingProgress = this.backhand$offHandSwingProgress;
        int var1 = this.getArmSwingAnimationEnd();
        if (this.backhand$isOffHandSwingInProgress) {
            ++this.backhand$offHandSwingProgressInt;
            if (this.backhand$offHandSwingProgressInt >= var1) {
                this.backhand$offHandSwingProgressInt = 0;
                this.backhand$isOffHandSwingInProgress = false;
            }
        } else {
            this.backhand$offHandSwingProgressInt = 0;
        }

        this.backhand$offHandSwingProgress = (float) this.backhand$offHandSwingProgressInt / (float) var1;
        if (this.backhand$specialActionTimer > 0) {
            this.backhand$isOffHandSwingInProgress = false;
            this.isSwingInProgress = false;
            this.backhand$offHandSwingProgress = 0.0F;
            this.backhand$offHandSwingProgressInt = 0;
            this.swingProgress = 0.0F;
            this.swingProgressInt = 0;
        }

    }
}
