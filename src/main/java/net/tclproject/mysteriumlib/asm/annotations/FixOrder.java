package net.tclproject.mysteriumlib.asm.annotations;

public enum FixOrder {
    FIRST, // Gets called first
    BEFORE_USUAL,
    USUAL,
    AFTER_USUAL,
    LAST // Gets called last

}
