package xonin.backhand.mixins.early;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import xonin.backhand.api.core.IBackhandPlayer;
import xonin.backhand.client.utils.BackhandClientUtils;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase implements IBackhandPlayer {

    @Shadow
    private ItemStack itemInUse;
    @Shadow
    public InventoryPlayer inventory;
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
    @Unique
    private boolean backhand$isShielding = false;

    private MixinEntityPlayer(World p_i1594_1_) {
        super(p_i1594_1_);
    }

    @Unique
    private int backhand$local$onItemFinish$i;

    @ModifyExpressionValue(
        method = "onItemUseFinish",
        at = @At(value = "FIELD", target = "Lnet/minecraft/item/ItemStack;stackSize:I", ordinal = 0))
    private int backhand$captureLocal$onItemFinish(int i) {
        this.backhand$local$onItemFinish$i = i;
        return i;
    }

    // TODO: move client side stuff to a different class
    @SideOnly(Side.CLIENT)
    @Inject(method = "getItemIcon", at = @At(value = "HEAD"))
    private void backhand2$getItemIcon(ItemStack itemStackIn, int p_70620_2_, CallbackInfoReturnable<IIcon> cir) {
        if (worldObj.isRemote) {
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            if (itemStackIn == player.getCurrentEquippedItem() && player.getCurrentEquippedItem() != null
                && player.getItemInUse() != null
                && player.getCurrentEquippedItem()
                    .getItem() instanceof ItemBow
                && player.getCurrentEquippedItem() != player.getItemInUse()) {
                BackhandClientUtils.disableMainhandAnimation = true;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public float getSwingProgress(float partialTicks) {
        if (BackhandClientUtils.offhandFPRender) {
            return getOffSwingProgress(partialTicks);
        }
        return super.getSwingProgress(partialTicks);
    }

    // TODO: Why are we doing this?
    @ModifyReturnValue(method = "isPlayer", at = @At(value = "RETURN"))
    private boolean backhand$isPlayer(boolean original) {
        return false;
    }

    // @ModifyExpressionValue(
    // method = "onItemUseFinish",
    // at = @At(
    // value = "INVOKE",
    // target =
    // "Lnet/minecraftforge/event/ForgeEventFactory;onItemUseFinish(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;ILnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
    // remap = false))
    // private ItemStack backhand$onItemUseFinish$beforeFinishUse(ItemStack itemStack) {
    // return BattlegearUtils.beforeFinishUseEvent(
    // (EntityPlayer) (Object) this,
    // this.itemInUse,
    // itemStack,
    // this.backhand$local$onItemFinish$i);
    // }
    //
    // @ModifyExpressionValue(
    // method = "onUpdate",
    // at = @At(
    // value = "INVOKE",
    // target = "Lnet/minecraft/entity/player/InventoryPlayer;getCurrentItem()Lnet/minecraft/item/ItemStack;"))
    // private ItemStack backhand$onUpdate$getCurrentItem(ItemStack currentItemStack) {
    // if (BattlegearUtils.isPlayerInBattlemode((EntityPlayer) (Object) this)) {
    // ItemStack itemStack = ((IInventoryPlayerBattle) this.inventory).backhand$getCurrentOffhandWeapon();
    // if (itemInUse == itemStack) {
    // return itemStack;
    // }
    // }
    // return currentItemStack;
    // }
    //
    // /**
    // * @author Alexdoru
    // * @reason IDK it's the original mod that does that
    // */
    // @Overwrite
    // public boolean interactWith(Entity var1) {
    // return BattlegearUtils.interactWith((EntityPlayer) (Object) this, var1);
    // }
    //
    // @Override
    // protected void updateArmSwingProgress() {
    // super.updateArmSwingProgress();
    // this.backhand$prevOffHandSwingProgress = this.backhand$offHandSwingProgress;
    // int var1 = this.getArmSwingAnimationEnd();
    // if (this.backhand$isOffHandSwingInProgress) {
    // ++this.backhand$offHandSwingProgressInt;
    // if (this.backhand$offHandSwingProgressInt >= var1) {
    // this.backhand$offHandSwingProgressInt = 0;
    // this.backhand$isOffHandSwingInProgress = false;
    // }
    // } else {
    // this.backhand$offHandSwingProgressInt = 0;
    // }
    //
    // this.backhand$offHandSwingProgress = (float) this.backhand$offHandSwingProgressInt / (float) var1;
    // if (this.backhand$specialActionTimer > 0) {
    // this.backhand$isOffHandSwingInProgress = false;
    // this.isSwingInProgress = false;
    // this.backhand$offHandSwingProgress = 0.0F;
    // this.backhand$offHandSwingProgressInt = 0;
    // this.swingProgress = 0.0F;
    // this.swingProgressInt = 0;
    // }
    //
    // }
    //
    // @Override
    // public void backhand$swingOffItem() {
    // if (!this.backhand$isOffHandSwingInProgress
    // || this.backhand$offHandSwingProgressInt >= this.getArmSwingAnimationEnd() / 2
    // || this.backhand$offHandSwingProgressInt < 0) {
    // this.backhand$offHandSwingProgressInt = -1;
    // this.backhand$isOffHandSwingInProgress = true;
    // }
    // }
    //
    // @Override
    // public float backhand$getOffSwingProgress(float frame) {
    // float diff = this.backhand$offHandSwingProgress - this.backhand$prevOffHandSwingProgress;
    // if (diff < 0.0F) {
    // ++diff;
    // }
    // return this.backhand$prevOffHandSwingProgress + diff * frame;
    // }
    //
    // public void backhand$attackTargetEntityWithCurrentOffItem(Entity target) {
    // BattlegearUtils.attackTargetEntityWithCurrentOffItem((EntityPlayer) (Object) this, target);
    // }
    //
    // @Override
    // public boolean backhand$isBattlemode() {
    // return BattlegearUtils.isPlayerInBattlemode((EntityPlayer) (Object) this);
    // }
    //
    // @Override
    // public boolean backhand$isBlockingWithShield() {
    // return BattlegearUtils.canBlockWithShield((EntityPlayer) (Object) this) && this.backhand$isShielding;
    // }
    //
    // @Override
    // public void backhand$setBlockingWithShield(boolean block) {
    // this.backhand$isShielding = block && BattlegearUtils.canBlockWithShield((EntityPlayer) (Object) this);
    // }
    //
    // @Override
    // public int backhand$getSpecialActionTimer() {
    // return this.backhand$specialActionTimer;
    // }
    //
    // @Override
    // public void backhand$setSpecialActionTimer(int time) {
    // this.backhand$specialActionTimer = time;
    // }

}
