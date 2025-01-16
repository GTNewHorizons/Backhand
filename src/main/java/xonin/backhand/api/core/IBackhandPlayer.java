package xonin.backhand.api.core;

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

    void setUsingOffhand(boolean usingOffhand);

    boolean isUsingOffhand();

}
