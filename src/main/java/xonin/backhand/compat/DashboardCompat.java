package xonin.backhand.compat;

import com.gtnewhorizons.dashboard.api.HudBounds;
import com.gtnewhorizons.dashboard.api.HudElement;
import com.gtnewhorizons.dashboard.api.HudRegistry;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import xonin.backhand.client.ClientEventHandler;

/**
 * Lets players move, resize and hide the off-hand slot in Dashboard's HUD editor. The only class using Dashboard, so
 * only call it while Dashboard is loaded.
 */
@SideOnly(Side.CLIENT)
public final class DashboardCompat {

    private static final HudElement OFFHAND_SLOT = new HudElement(
        "backhand:offhand_slot",
        (screenWidth, screenHeight) -> new HudBounds(
            ClientEventHandler.getSlotX(screenWidth),
            ClientEventHandler.getSlotY(screenHeight),
            ClientEventHandler.SLOT_SIZE,
            ClientEventHandler.SLOT_SIZE));

    private DashboardCompat() {}

    public static void register() {
        OFFHAND_SLOT.setPreview(
            (screenWidth, screenHeight) -> HudRegistry
                .render(OFFHAND_SLOT, () -> ClientEventHandler.renderSlotBackground(screenWidth, screenHeight)));
        HudRegistry.register(OFFHAND_SLOT);
    }

    /** False means the player hid the slot, so skip drawing it and do not call {@link #endOffhandSlot()}. */
    public static boolean beginOffhandSlot(int screenWidth, int screenHeight) {
        return HudRegistry.beginRender(OFFHAND_SLOT, screenWidth, screenHeight);
    }

    public static void endOffhandSlot() {
        HudRegistry.endRender();
    }
}
