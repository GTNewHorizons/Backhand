package xonin.backhand.utils;

import xonin.backhand.api.core.IBackhandPlayer;

public enum EnumAnimations {

    OffHandSwing {

        @Override
        public void processAnimation(IBackhandPlayer entity) {
            entity.swingOffItem();
        }
    };

    public abstract void processAnimation(IBackhandPlayer entity);

}
