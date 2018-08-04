package com.xj.hookdemo.hook.clipboard;

import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author xujun  on 16/7/2018.
 */
public class ClipboardHookRemoteBinderHandler implements InvocationHandler {

    private IBinder remoteBinder;
    private Class iInterface;
    private Class stubClass;

    public ClipboardHookRemoteBinderHandler(IBinder remoteBinder) {
        this.remoteBinder = remoteBinder;
        try {
            this.iInterface = Class.forName("android.content.IClipboard");
            this.stubClass = Class.forName("android.content.IClipboard$Stub");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Log.d("RemoteBinderHandler", method.getName() + "() is invoked");
        if ("queryLocalInterface".equals(method.getName())) {
            //这里不能拦截具体的服务的方法，因为这是一个远程的Binder，还没有转化为本地Binder对象
            //所以先拦截我们所知的queryLocalInterface方法，返回一个本地Binder对象的代理
            return Proxy.newProxyInstance(remoteBinder.getClass().getClassLoader(),
                    new Class[]{this.iInterface},
                    new ClipboardHookLocalBinderHandler(remoteBinder, stubClass));
        }

        return method.invoke(remoteBinder, args);
    }
}
