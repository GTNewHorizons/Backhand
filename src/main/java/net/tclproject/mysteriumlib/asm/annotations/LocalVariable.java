package net.tclproject.mysteriumlib.asm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Intercepts a local variable. Use as follows: @LocalVariable(variable_number).
 * The names of the variables might not save in code, that's why we need a number.
 * In order to find out the number, you can use methods in the MetaReader class,
 * specifically getLocalVariables.
 */
@Target(ElementType.PARAMETER)
public @interface LocalVariable {

    int number();
}
