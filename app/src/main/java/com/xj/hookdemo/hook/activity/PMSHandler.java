package com.xj.hookdemo.hook.activity;

import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author xujun  on 17/7/2018.
 */
public class PMSHandler implements InvocationHandler {

    private final Object mOrigin;

    private static final String TAG = "PMSHandler";

    public PMSHandler(Object origin) {
        mOrigin = origin;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Log.i(TAG, "invoke: method=" +method);
        return method.invoke(mOrigin, args);
    }
}
