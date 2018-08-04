package com.xj.hookdemo.hook.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import com.xj.hookdemo.BuildConfig;
import com.xj.hookdemo.activityhook.EmptyActivity;
import com.xj.hookdemo.hook.HookHelper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author xujun  on 17/7/2018.
 */
public class IActivityManagerHandler implements InvocationHandler {

    private static final String TAG = "IActivityManagerHandler";
    Object mBase;

    public IActivityManagerHandler(Object base) {
        mBase = base;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("startActivity".equals(method.getName())) {
            Intent raw;
            int index = 0;
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Intent) {
                    index = i;
                    break;
                }
            }
            raw = (Intent) args[index];
            Intent newIntent = new Intent();
            String stubPackage = BuildConfig.APPLICATION_ID;
            ComponentName componentName = new ComponentName(stubPackage, EmptyActivity.class.getName());
            newIntent.setComponent(componentName);
            newIntent.putExtra(HookHelper.EXTRA_TARGET_INTENT, raw);
            args[index] = newIntent;
            Log.d(TAG, "hook success");
            return method.invoke(mBase, args);

        }
        return method.invoke(mBase, args);
    }
}