package mods.battlegear2.utils;

import xonin.backhand.api.core.IBattlePlayer;

public enum EnumBGAnimations {

    OffHandSwing {
        @Override
        public void processAnimation(IBattlePlayer entity) {
            entity.swingOffItem();
        }
    };

    public abstract void processAnimation(IBattlePlayer entity);

}
