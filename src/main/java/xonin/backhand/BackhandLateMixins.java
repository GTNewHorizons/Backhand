package xonin.backhand;

import java.util.List;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

import xonin.backhand.mixins.Mixins;

@LateMixin
public class BackhandLateMixins implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins.backhand.late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        return Mixins.getLateMixins(loadedMods);
    }
}
