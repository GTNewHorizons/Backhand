package net.tclproject.mysteriumlib.asm.annotations;

/** Determines if return gets called after a fix method. */
public enum EnumReturnSetting {

    /**
     * return never gets called.
     */
    NEVER(false),

    /**
     * return always gets called.
     */
    ALWAYS(false),

    /**
     * return gets called, if the fix method returned true.
     * You can't use this, if the fix method doesn't return a boolean.
     */
    ON_TRUE(true),

    /**
     * return gets called, if the fix method returned null.
     * You can't use this, if the fix method returns a primitive or void.
     */
    ON_NULL(true),

    /**
     * return gets called, if the fix method returned null.
     * You can't use this, if the fix method returns a primitive or void.
     */
    ON_NOT_NULL(true);

    public final boolean conditionRequiredToReturn;

    EnumReturnSetting(boolean conditionRequiredToReturn) {
        this.conditionRequiredToReturn = conditionRequiredToReturn;
    }
}
