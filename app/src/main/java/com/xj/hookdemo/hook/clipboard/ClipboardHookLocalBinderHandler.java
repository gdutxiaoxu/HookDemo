package com.xj.hookdemo.hook.clipboard;

import android.content.ClipData;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author xujun  on 16/7/2018.
 */
public class ClipboardHookLocalBinderHandler implements InvocationHandler{

    private Object localProxyBinder;

    public ClipboardHookLocalBinderHandler(IBinder remoteBinder, Class<?> stubClass) {
        try {
            Method asInterfaceMethod = stubClass.getMethod("asInterface", IBinder.class);
            localProxyBinder = asInterfaceMethod.invoke(null, remoteBinder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Log.d("LocalBinderHandler", method.getName() + "() is invoked");
        String methodName = method.getName();
        if ("setPrimaryClip".equals(methodName)) {
            //这里对setPrimaryClip()进行了拦截
            int argsLength = args.length;
            if (argsLength >= 2 && args[0] instanceof ClipData) {
                ClipData data = (ClipData) args[0];
                String text = data.getItemAt(0).getText().toString();
                text += "   -- Hooked by me";
                args[0] = ClipData.newPlainText(data.getDescription().getLabel(), text);
            }
        }

        return method.invoke(localProxyBinder, args);
    }
}
