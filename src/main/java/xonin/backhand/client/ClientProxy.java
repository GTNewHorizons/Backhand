package xonin.backhand.client;

import xonin.backhand.CommonProxy;
import xonin.backhand.compat.DashboardCompat;
import xonin.backhand.utils.Mods;

public class ClientProxy extends CommonProxy {

    @Override
    public void load() {
        super.load();
        if (Mods.DASHBOARD.isLoaded()) {
            DashboardCompat.register();
        }
    }
}
