package xonin.backhand.utils;

import xonin.backhand.api.core.IBattlePlayer;

public enum EnumAnimations {

    OffHandSwing {
        @Override
        public void processAnimation(IBattlePlayer entity) {
            entity.swingOffItem();
        }
    };

    public abstract void processAnimation(IBattlePlayer entity);

}
