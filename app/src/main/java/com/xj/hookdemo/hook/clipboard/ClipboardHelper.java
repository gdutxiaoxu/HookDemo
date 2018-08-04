package com.xj.hookdemo.hook.clipboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Created by xujun on 4/8/2018$ 10:25$.
 */
public class ClipboardHelper {

    private static final String TAG = "ClipboardHelper";

    public static void hookClipboardService(final Context context) throws Exception {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        Field mServiceFiled = ClipboardManager.class.getDeclaredField("mService");
        mServiceFiled.setAccessible(true);
        // 第一步：得到系统的 mService
        final Object mService = mServiceFiled.get(clipboardManager);

        // 第二步：初始化动态代理对象
        Class aClass = Class.forName("android.content.IClipboard");
        Object proxyInstance = Proxy.newProxyInstance(context.getClass().getClassLoader(), new
                Class[]{aClass}, new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Log.d(TAG, "invoke(). method:" + method);
                String name = method.getName();
                if (args != null && args.length > 0) {
                    for (Object arg : args) {
                        Log.d(TAG, "invoke: arg=" + arg);
                    }
                }
                if ("setPrimaryClip".equals(name)) {
                    Object arg = args[0];
                    if (arg instanceof ClipData) {
                        ClipData clipData = (ClipData) arg;
                        int itemCount = clipData.getItemCount();
                        for (int i = 0; i < itemCount; i++) {
                            ClipData.Item item = clipData.getItemAt(i);
                            Log.i(TAG, "invoke: item=" + item);
                        }
                    }
                    Toast.makeText(context, "检测到有人设置粘贴板内容", Toast.LENGTH_SHORT).show();
                } else if ("getPrimaryClip".equals(name)) {
                    Toast.makeText(context, "检测到有人要获取粘贴板的内容", Toast.LENGTH_SHORT).show();
                }
                // 操作交由 sOriginService 处理，不拦截通知
                return method.invoke(mService, args);

            }
        });

        // 第三步：偷梁换柱，使用 proxyNotiMng 替换系统的sService
        Field sServiceField = ClipboardManager.class.getDeclaredField("mService");
        sServiceField.setAccessible(true);
        sServiceField.set(clipboardManager, proxyInstance);

    }

    public static void hookClipboardService() throws Exception {

        //通过反射获取剪切板服务的远程Binder对象
        Class serviceManager = Class.forName("android.os.ServiceManager");
        Method getServiceMethod = serviceManager.getMethod("getService", String.class);
        IBinder remoteBinder = (IBinder) getServiceMethod.invoke(null, Context.CLIPBOARD_SERVICE);

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
