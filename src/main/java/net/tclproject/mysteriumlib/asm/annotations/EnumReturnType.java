package net.tclproject.mysteriumlib.asm.annotations;

/** Specifies what the fix method returns. */
public enum EnumReturnType {

    /**
     * Returns void.
     * Only use if the target method returns void.
     */
    VOID,

    /**
     * Returns a pre-established primitive value.
     * Only use if the target method returns a primitive.
     */
    PRIMITIVE_CONSTANT,

    /**
     * Returns null.
     * Only use if the target method returns an object.
     */
    NULL,

    /**
     * Returns the primitive or object, that the fix method returned.
     * Can use in all cases except for when the target method returns void.
     */
    FIX_METHOD_RETURN_VALUE,

    /**
     * Calls a different method in the same class with the same parameters that the fix method has, but with a different
     * name.
     * Returns the value that the called method returned.
     */
    ANOTHER_METHOD_RETURN_VALUE
}
