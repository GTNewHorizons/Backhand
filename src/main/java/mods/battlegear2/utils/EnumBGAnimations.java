package mods.battlegear2.utils;

import mods.battlegear2.api.core.IBattlePlayer;

public enum EnumBGAnimations {

    OffHandSwing {

        @Override
        public void processAnimation(IBattlePlayer entity) {
            entity.swingOffItem();
        }
    };

    public abstract void processAnimation(IBattlePlayer entity);

}
