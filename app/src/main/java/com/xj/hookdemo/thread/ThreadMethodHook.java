package com.xj.hookdemo.thread;

import android.support.v7.widget.DialogTitle;
import android.util.Log;

import de.robv.android.xposed.XC_MethodHook;

class ThreadMethodHook extends XC_MethodHook {

    private static final String TAG = "Hook.ThreadMethodHook";

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        super.beforeHookedMethod(param);
        Thread t = (Thread) param.thisObject;
        Log.i(TAG, "thread:" + t + ", started..");
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        super.afterHookedMethod(param);
        Thread t = (Thread) param.thisObject;
        Log.i(TAG, "thread:" + t + ", exit..");
    }
}

