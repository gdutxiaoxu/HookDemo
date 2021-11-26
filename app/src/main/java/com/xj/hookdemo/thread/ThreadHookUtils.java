package com.xj.hookdemo.thread;

import android.util.Log;

import de.robv.android.xposed.DexposedBridge;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class ThreadHookUtils {

    private static final String TAG = "hook.ThreadHookUtils";

    public static void hook(){
        DexposedBridge.hookAllConstructors(Thread.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Thread thread = (Thread) param.thisObject;
                Class<?> clazz = thread.getClass();
                if (clazz != Thread.class) {
                    Log.d(TAG, "found class extend Thread:" + clazz);
                    DexposedBridge.findAndHookMethod(clazz, "run", new ThreadMethodHook());
                }
                Log.d(TAG, "Thread: " + thread.getName() + " class:" + thread.getClass() +  " is created.");
            }
        });
        DexposedBridge.findAndHookMethod(Thread.class, "run", new ThreadMethodHook());
    }


}
