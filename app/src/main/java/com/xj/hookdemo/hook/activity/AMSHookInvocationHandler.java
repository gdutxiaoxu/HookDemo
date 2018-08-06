package com.xj.hookdemo.hook.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author xujun
 * @time 4/8/2018 10:29.
 */
public class AMSHookInvocationHandler implements InvocationHandler {

    public static final String ORIGINALLY_INTENT = "originallyIntent";
    private Object mAmsObj;
    private String mPackageName;
    private String cls;

    public AMSHookInvocationHandler(Object amsObj, String packageName, String cls) {
        this.mAmsObj = amsObj;
        this.mPackageName = packageName;
        this.cls = cls;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //  对 startActivity进行Hook
        if (method.getName().equals("startActivity")) {
            int index = 0;
            //  找到我们启动时的intent
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Intent) {
                    index = i;
                    break;
                }
            }

            // 取出在真实的Intent
            Intent originallyIntent = (Intent) args[index];
            Log.i("AMSHookUtil", "AMSHookInvocationHandler:" + originallyIntent.getComponent()
                    .getClassName());
            // 自己伪造一个配置文件已注册过的Activity Intent
            Intent proxyIntent = new Intent();
            //  因为我们调用的Activity没有注册，所以这里我们先偷偷换成已注册。使用一个假的Intent
            ComponentName componentName = new ComponentName(mPackageName, cls);
            proxyIntent.setComponent(componentName);
            // 在这里把未注册的Intent先存起来 一会儿我们需要在Handle里取出来用
            proxyIntent.putExtra(ORIGINALLY_INTENT, originallyIntent);
            args[index] = proxyIntent;
        }
        return method.invoke(mAmsObj, args);
    }
}
