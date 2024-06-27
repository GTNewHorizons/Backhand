package net.tclproject.mysteriumlib.asm.annotations;

// TODO: Add this everywhere
/*
 * Copyright TCLProject - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by TCLProject <endermcraftmail@gmail.com>, December 2020
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * To make a method a fix, you need to add this annotation to it and register the class with the fixes.
 * <p/>
 * The target class is determined by the first argument of the method.
 * If the target method is static, then it becomes null, otherwise - this.
 * <p/>
 * The name of the target method by default is assumed to be the name than that of the fix method,
 * but you can change it by specifying targetMethod.
 * <p/>
 * The arguments of the target method are assumed by the arguments of the fix. You need to specify all the
 * arguments in exactly the same order.
 * <p/>
 * The return type of the target method isn't specified by default. We assume, that there are no methods
 * with the same name and arguments. If you need to specify it though, you can do it through returnedType.
 */
@Target(ElementType.METHOD)
public @interface Fix {

    /**
     * Specifies a condition, on which after the fix method has been called, return will be called.
     * If the target method doesn't return void, by default it will return the value that the fix returned.
     * You can override that using a few parameters in this annotation:
     * anotherMethodReturned, nullReturned and %type%AlwaysReturned.
     */
    EnumReturnSetting returnSetting() default EnumReturnSetting.NEVER;

    /**
     * Specifies when the fix method will be called.
     * FIRST will be inserted first, LAST will be inserted last therefore will be the actual first/last set of
     * instructions.
     */
    FixOrder order() default FixOrder.USUAL;

    /**
     * Specifies the name of the target method.
     * By default the name of the fix method is used.
     * This option is useful, when you need to apply a fix to a constructor or to the initialization of a class.
     * For a constructor, targetMethod must be "<init>", for the initialization of a class - "<clinit>".
     */
    String targetMethod() default "";

    /**
     * Specifies the type returned by the target method.
     * From the point of view of JVM there can be methods, that only differ by the return type.
     * In practice, compilers don't generate such methods, but in some cases, they can exist
     * (for example, ProGuard can make such methods while obfuscating).
     * If the return type isn't specified, the fix will be applied to the first method
     * that matches the name and arguments.
     *
     * The main proposed use of this parameter is with createMethod = true.
     * The created method will by default have the same return type as the fix method,
     * but you can change that with this parameter.
     *
     * You need to specify the full name of the class: java.lang.String, void, int etc.
     */
    String returnedType() default "";

    /**
     * Allows not only to insert fixes into already existing methods, but also add new ones. This can be useful,
     * if you need to override a method of the superclass. Doesn't actually add anything inside the target method except
     * for a super call (if possible) and a return statement, so you have to insert a fix into the created method to add
     * some functionality.
     */
    boolean createNewMethod() default false;

    /**
     * Allows to make the fix mandatory for the game to launch. If the insertion of the fix fails,
     * the game will crash.
     */
    boolean isFatal() default false;

    /**
     * By default the fix is injected into the beginning of the target method.
     * If this will be set to true, it will be injected in the end and before every return call.
     */
    boolean insertOnExit() default false;

    /**
     * By default, the fix is inserted into the beginning of the target method.
     * If you set this, it will be inserted at the said line.
     * The use of this is NOT recommended because it can break very easily,
     * (for example, if some mod *cough* optifine *cough* will replace the class fully).
     * <p/>
     * NOTE: the line numbers in mcp and in minecraft are sometimes different, beware.
     */
    @Deprecated
    int insertOnLine() default -1;

    /**
     * If you specify this name, when return will be called in the target method, this method will be called.
     * It must be in the same class and have the same arguments, as the fix method.
     * In the end, the returned value will be the one that this method will return.
     */
    String anotherMethodReturned() default "";

    /**
     * If true, return in the target method will always return null.
     */
    boolean nullReturned() default false;

    /**
     * If you specify one of those, it will be returned instead of the original value from the target method.
     */

    boolean booleanAlwaysReturned() default false;

    /**
     * If you specify one of those, it will be returned instead of the original value from the target method.
     */
    byte byteAlwaysReturned() default 0;

    /**
     * If you specify one of those, it will be returned instead of the original value from the target method.
     */
    short shortAlwaysReturned() default 0;

    /**
     * If you specify one of those, it will be returned instead of the original value from the target method.
     */
    int intAlwaysReturned() default 0;

    /**
     * If you specify one of those, it will be returned instead of the original value from the target method.
     */
    long longAlwaysReturned() default 0L;

    /**
     * If you specify one of those, it will be returned instead of the original value from the target method.
     */
    float floatAlwaysReturned() default 0.0F;

    /**
     * If you specify one of those, it will be returned instead of the original value from the target method.
     */
    double doubleAlwaysReturned() default 0.0D;

    /**
     * If you specify one of those, it will be returned instead of the original value from the target method.
     */
    char charAlwaysReturned() default 0;

    /**
     * If you specify one of those, it will be returned instead of the original value from the target method.
     */
    String stringAlwaysReturned() default "";
}
