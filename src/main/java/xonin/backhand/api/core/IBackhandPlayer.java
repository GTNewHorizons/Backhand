package xonin.backhand.api.core;

import net.minecraft.entity.Entity;

/**
 * Interface added to EntityPlayer to support offhand management
 * 
 * @author GotoLink
 */
public interface IBackhandPlayer {

    /**
     * A copied animation for the offhand, similar to EntityPlayer#swingItem()
     */
    void swingOffItem();

    /**
     * The partial render progress for the offhand swing animation
     */
    float getOffSwingProgress(float frame);

    /**
     * Hotswap the EntityPlayer current item to offhand, behaves like
     * EntityPlayer#attackTargetEntityWithCurrentItem(Entity)
     * 
     * @param target to attack
     */
    void attackTargetEntityWithCurrentOffItem(Entity target);
}
