package com.xj.hookdemo.utils;

import android.content.Context;
import android.os.IBinder;

import com.xj.hookdemo.hook.clipboard.ClipboardHookRemoteBinderHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Created by xujun on 24/7/2018$ 20:00$.
 */
public class BinderHook {

    public static void hookClipboardService(String serviceName) throws Exception{

        //通过反射获取剪切板服务的远程Binder对象
        Class serviceManager = Class.forName("android.os.ServiceManager");
        Method getServiceMethod = serviceManager.getMethod("getService", String.class);
        IBinder remoteBinder = (IBinder) getServiceMethod.invoke(null,serviceName);

        //新建一个我们需要的Binder，动态代理原来的Binder对象
        IBinder hookBinder = (IBinder) Proxy.newProxyInstance(serviceManager.getClassLoader(),
                new Class[]{IBinder.class}, new ClipboardHookRemoteBinderHandler(remoteBinder));

        //通过反射获取ServiceManger存储Binder对象的缓存集合,把我们新建的代理Binder放进缓存
        Field sCacheField = serviceManager.getDeclaredField("sCache");
        sCacheField.setAccessible(true);
        Map<String, IBinder> sCache = (Map<String, IBinder>) sCacheField.get(null);
        sCache.put(Context.CLIPBOARD_SERVICE, hookBinder);

    }
}
