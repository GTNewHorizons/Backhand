package xonin.backhand.api.core;

import net.minecraft.launchwrapper.Launch;

public class BackhandTranslator {

    static {
        obfuscatedEnv = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
    }

    public static boolean obfuscatedEnv;

    public static String getMapedClassName(String className) {
        return "net/minecraft/" + className.replace(".", "/");
    }

    /** @deprecated */
    @Deprecated
    public static String getMapedMethodName(String className, String methodName, String devName) {
        return getMapedMethodName(methodName, devName);
    }

    public static String getMapedMethodName(String methodName, String devName) {
        return obfuscatedEnv ? devName : methodName;
    }

    /** @deprecated */
    @Deprecated
    public static String getMapedMethodDesc(String className, String methodName, String devDesc) {
        return devDesc;
    }
}
