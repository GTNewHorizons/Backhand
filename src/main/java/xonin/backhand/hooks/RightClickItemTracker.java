package xonin.backhand.hooks;

public final class RightClickItemTracker {

    private static boolean trackingMainhandUse;
    private static boolean reachedBaseItemRightClick;

    private RightClickItemTracker() {}

    public static void beginMainhandUse() {
        trackingMainhandUse = true;
        reachedBaseItemRightClick = false;
    }

    public static void markBaseItemRightClick() {
        if (trackingMainhandUse) {
            reachedBaseItemRightClick = true;
        }
    }

    public static boolean didCustomItemRightClickHandle() {
        return trackingMainhandUse && !reachedBaseItemRightClick;
    }

    public static void endMainhandUse() {
        trackingMainhandUse = false;
        reachedBaseItemRightClick = false;
    }
}
