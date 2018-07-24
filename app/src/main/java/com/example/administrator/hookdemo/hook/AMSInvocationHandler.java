package com.example.administrator.hookdemo.hook;

import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author xujun  on 16/7/2018.
 */
public class AMSInvocationHandler implements InvocationHandler {

    private static final String TAG = "AMSInvocationHandler";

    Object iamObject;

    public AMSInvocationHandler(Object iamObject) {
        this.iamObject = iamObject;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //            Log.e(TAG, method.getName());
        if ("startActivity".equals(method.getName())) {
            Log.e(TAG, "要开始启动了 啦啦啦啦啦啦  ");
            Log.e(TAG, "method=" + method);
            for (Object object : args) {
                Log.d(TAG, "invoke: object=" + object);
            }
        }
        return method.invoke(iamObject, args);
    }
}
