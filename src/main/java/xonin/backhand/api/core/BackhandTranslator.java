package xonin.backhand.api.core;

import net.tclproject.mysteriumlib.asm.common.CustomLoadingPlugin;

public class BackhandTranslator {
   public static boolean debug = false;
   public static boolean obfuscatedEnv;

    public static boolean debug = false;
    public static boolean obfuscatedEnv;

    /** @deprecated */
    @Deprecated
    public static String getMapedFieldName(String className, String fieldName, String devName) {
        return getMapedFieldName(fieldName, devName);
    }

    public static String getMapedFieldName(String fieldName, String devName) {
        return CustomLoadingPlugin.isObfuscated() ? fieldName : devName;
    }

    public static String getMapedClassName(String className) {
        return "net/minecraft/" + className.replace(".", "/");
    }

    /** @deprecated */
    @Deprecated
    public static String getMapedMethodName(String className, String methodName, String devName) {
        return getMapedMethodName(methodName, devName);
    }

    public static String getMapedMethodName(String methodName, String devName) {
        return CustomLoadingPlugin.isObfuscated() ? methodName : devName;
    }

    /** @deprecated */
    @Deprecated
    public static String getMapedMethodDesc(String className, String methodName, String devDesc) {
        return devDesc;
    }
}
