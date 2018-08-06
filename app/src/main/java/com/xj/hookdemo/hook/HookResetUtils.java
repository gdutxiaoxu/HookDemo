package com.xj.hookdemo.hook;

import android.os.Build;
import android.os.Handler;

import java.lang.reflect.Field;

/**
 * Created by xujun on 6/8/2018$ 11:04$.
 */
public class HookResetUtils {

    public static void resetAms(Object amsObj) throws Exception {
        Field gDefaultField = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Class<?> activityManager = Class.forName("android.app.ActivityManager");
            gDefaultField = activityManager.getDeclaredField("IActivityManagerSingleton");
        } else {
            Class<?> activityManagerNativeClass = Class.forName("android.app" +
                    ".ActivityManagerNative");
            gDefaultField = activityManagerNativeClass.getDeclaredField("gDefault");
        }
        gDefaultField.setAccessible(true);
        Object gDefaultObj = gDefaultField.get(null); //所有静态对象的反射可以通过传null获取。如果是实列必须传实例
        Class<?> singletonClazz = Class.forName("android.util.Singleton");
        Field amsField = singletonClazz.getDeclaredField("mInstance");
        amsField.setAccessible(true);
        amsField.set(gDefaultObj, amsObj);
    }

    public static void resetActivityLaunch(Object mH) throws Exception {
        Class<?> activityThreadClazz = Class.forName("android.app.ActivityThread");
        Field sCurrentActivityThreadField = activityThreadClazz.getDeclaredField
                ("sCurrentActivityThread");
        sCurrentActivityThreadField.setAccessible(true);
        Object sCurrentActivityThreadObj = sCurrentActivityThreadField.get(null);
        Field mHField = activityThreadClazz.getDeclaredField("mH");
        mHField.setAccessible(true);
        Field callBackField = Handler.class.getDeclaredField("mCallback");
        callBackField.setAccessible(true);
        callBackField.set(sCurrentActivityThreadObj,mH);
    }

    public static Object storeAms() throws Exception {
        Field gDefaultField = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Class<?> activityManager = Class.forName("android.app.ActivityManager");
            gDefaultField = activityManager.getDeclaredField("IActivityManagerSingleton");
        } else {
            Class<?> activityManagerNativeClass = Class.forName("android.app" +
                    ".ActivityManagerNative");
            gDefaultField = activityManagerNativeClass.getDeclaredField("gDefault");
        }
        gDefaultField.setAccessible(true);
        Object gDefaultObj = gDefaultField.get(null); //所有静态对象的反射可以通过传null获取。如果是实列必须传实例
        Class<?> singletonClazz = Class.forName("android.util.Singleton");
        Field amsField = singletonClazz.getDeclaredField("mInstance");
        amsField.setAccessible(true);
        return amsField.get(gDefaultObj);


    }

    public static Handler storeActivityLaunch() throws Exception {
        Class<?> activityThreadClazz = Class.forName("android.app.ActivityThread");
        Field sCurrentActivityThreadField = activityThreadClazz.getDeclaredField
                ("sCurrentActivityThread");
        sCurrentActivityThreadField.setAccessible(true);
        Object sCurrentActivityThreadObj = sCurrentActivityThreadField.get(null);
        Field mHField = activityThreadClazz.getDeclaredField("mH");
        mHField.setAccessible(true);
        return (Handler) mHField.get(sCurrentActivityThreadObj);

    }
}
