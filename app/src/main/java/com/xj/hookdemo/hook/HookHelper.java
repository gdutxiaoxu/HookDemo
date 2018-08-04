package com.xj.hookdemo.hook;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.xj.hookdemo.hook.activity.AMSInvocationHandler;
import com.xj.hookdemo.hook.activity.ActivityProxyInstrumentation;
import com.xj.hookdemo.hook.activity.ActivityThreadHandlerCallback;
import com.xj.hookdemo.hook.activity.ApplicationInstrumentation;
import com.xj.hookdemo.hook.activity.IActivityManagerHandler;
import com.xj.hookdemo.hook.activity.PMSHandler;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author xujun  on 16/7/2018.
 */
public class HookHelper {

    public static final String EXTRA_TARGET_INTENT = "extra_target_intent";
    private static final String TAG = "HookHelper";


    public static void attachContext() throws Exception {
        Log.i(TAG, "attachContext: ");
        // 先获取到当前的ActivityThread对象
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
        currentActivityThreadMethod.setAccessible(true);
        //currentActivityThread是一个static函数所以可以直接invoke，不需要带实例参数
        Object currentActivityThread = currentActivityThreadMethod.invoke(null);

        // 拿到原始的 mInstrumentation字段
        Field mInstrumentationField = activityThreadClass.getDeclaredField("mInstrumentation");
        mInstrumentationField.setAccessible(true);
        Instrumentation mInstrumentation = (Instrumentation) mInstrumentationField.get(currentActivityThread);
        // 创建代理对象
        Instrumentation evilInstrumentation = new ApplicationInstrumentation(mInstrumentation);
        // 偷梁换柱
        mInstrumentationField.set(currentActivityThread, evilInstrumentation);
    }

    public static void hookAMS() throws Exception {
        int sdkInt = Build.VERSION.SDK_INT;
        Log.d(TAG, "hookAMS: sdkInt=" + sdkInt);
        if (sdkInt >= Build.VERSION_CODES.O) {
            hookAMSAfter26();
        } else {
            hookAmsBefore26();
        }
    }

    public static void hookAmsBefore26() throws Exception {
        // 第一步：获取 IActivityManagerSingleton
        Class<?> forName = Class.forName("android.app.ActivityManagerNative");
        Field defaultField = forName.getDeclaredField("gDefault");
        defaultField.setAccessible(true);
        Object defaultValue = defaultField.get(null);

        Class<?> forName2 = Class.forName("android.util.Singleton");
        Field instanceField = forName2.getDeclaredField("mInstance");
        instanceField.setAccessible(true);
        Object iActivityManagerObject = instanceField.get(defaultValue);

        // 第二步：获取我们的代理对象，这里因为 IActivityManager 是接口，我们使用动态代理的方式
        Class<?> iActivity = Class.forName("android.app.IActivityManager");
        InvocationHandler handler = new AMSInvocationHandler(iActivityManagerObject);
        Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{iActivity}, handler);

        // 第三步：偷梁换柱，将我们的 proxy 替换原来的对象
        instanceField.set(defaultValue, proxy);
    }

    public static void hookAMSAfter26() throws Exception {
        // 第一步：获取 IActivityManagerSingleton
        Class<?> aClass = Class.forName("android.app.ActivityManager");
        Field declaredField = aClass.getDeclaredField("IActivityManagerSingleton");
        declaredField.setAccessible(true);
        Object value = declaredField.get(null);

        Class<?> singletonClz = Class.forName("android.util.Singleton");
        Field instanceField = singletonClz.getDeclaredField("mInstance");
        instanceField.setAccessible(true);
        Object iActivityManagerObject = instanceField.get(value);

        // 第二步：获取我们的代理对象，这里因为 IActivityManager 是接口，我们使用动态代理的方式
        Class<?> iActivity = Class.forName("android.app.IActivityManager");
        InvocationHandler handler = new AMSInvocationHandler(iActivityManagerObject);
        Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new
                Class<?>[]{iActivity}, handler);

        // 第三步：偷梁换柱，将我们的 proxy 替换原来的对象
        instanceField.set(value, proxy);

    }



    public static void hookOnClickListener(View view) throws Exception {
        // 第一步：反射得到 ListenerInfo 对象
        Method getListenerInfo = View.class.getDeclaredMethod("getListenerInfo");
        getListenerInfo.setAccessible(true);
        Object listenerInfo = getListenerInfo.invoke(view);
        // 第二步：得到原始的 OnClickListener事件方法
        Class<?> listenerInfoClz = Class.forName("android.view.View$ListenerInfo");
        Field mOnClickListener = listenerInfoClz.getDeclaredField("mOnClickListener");
        mOnClickListener.setAccessible(true);
        View.OnClickListener originOnClickListener = (View.OnClickListener) mOnClickListener.get(listenerInfo);
        // 第三步：用Hook代理类 替换原始的 OnClickListener
        View.OnClickListener hookedOnClickListener = new HookedClickListenerProxy(originOnClickListener);
        mOnClickListener.set(listenerInfo, hookedOnClickListener);
    }

    public static void replaceInstrumentation(Activity activity) throws Exception {
        Class<?> k = Activity.class;
        //通过Activity.class 拿到 mInstrumentation字段
        Field field = k.getDeclaredField("mInstrumentation");
        field.setAccessible(true);
        //根据activity内mInstrumentation字段 获取Instrumentation对象
        Instrumentation instrumentation = (Instrumentation) field.get(activity);
        //创建代理对象
        Instrumentation instrumentationProxy = new ActivityProxyInstrumentation(instrumentation);
        //进行替换
        field.set(activity, instrumentationProxy);
    }

    public void hookPMS(Context context) {
        try {
            // 获取全局的ActivityThread对象
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            Object currentActivityThread = currentActivityThreadMethod.invoke(null);//得到ActivityThread对象

            // 获取ActivityThread里面原始的 sPackageManager
            Field sPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager");

            sPackageManagerField.setAccessible(true);
            Object sPackageManager = sPackageManagerField.get(currentActivityThread);

            // 准备好代理对象, 用来替换原始的对象
            Class<?> iPackageManagerInterface = Class.forName("android.content.pm.IPackageManager");
            Object proxy = Proxy.newProxyInstance(iPackageManagerInterface.getClassLoader(),
                    new Class<?>[]{iPackageManagerInterface},
                    new PMSHandler(sPackageManager));

            // 1. 替换掉ActivityThread里面的 sPackageManager 字段
            sPackageManagerField.set(currentActivityThread, proxy);

            // 2. 替换 ApplicationPackageManager里面的 mPM对象
            PackageManager pm = context.getPackageManager();
            Field mPmField = pm.getClass().getDeclaredField("mPM");
            mPmField.setAccessible(true);
            mPmField.set(pm, proxy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 主要完成的操作是  "把真正要启动的Activity临时替换为在AndroidManifest.xml中声明的替身Activity"
     */
    public static void hookActivityManagerNative() throws Exception {
        Field gDefaultField =null;
        if (Build.VERSION.SDK_INT >= 26) {
            Class<?> activityManager = Class.forName("android.app.ActivityManager");
            gDefaultField = activityManager.getDeclaredField("IActivityManagerSingleton");
        }else{
            Class<?> activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");
            gDefaultField = activityManagerNativeClass.getDeclaredField("gDefault");
        }
        gDefaultField.setAccessible(true);
        Object gDefault = gDefaultField.get(null);
        // gDefault是一个 android.util.Singleton对象; 我们取出这个单例里面的字段
        Class<?> singleton = Class.forName("android.util.Singleton");
        Field mInstanceField = singleton.getDeclaredField("mInstance");
        mInstanceField.setAccessible(true);
        // ActivityManagerNative 的gDefault对象里面原始的 IActivityManager对象
        Object rawIActivityManager = mInstanceField.get(gDefault);
        // 创建一个这个对象的代理对象, 然后替换这个字段, 让我们的代理对象帮忙干活
        Class<?> iActivityManagerInterface = Class.forName("android.app.IActivityManager");
        Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { iActivityManagerInterface }, new IActivityManagerHandler(rawIActivityManager));
        mInstanceField.set(gDefault, proxy);
    }


    public static void hookActivityThreadHandler() throws Exception {

        // 先获取到当前的ActivityThread对象
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Field currentActivityThreadField = activityThreadClass.getDeclaredField("sCurrentActivityThread");
        currentActivityThreadField.setAccessible(true);
        Object currentActivityThread = currentActivityThreadField.get(null);

        // 由于ActivityThread一个进程只有一个,我们获取这个对象的mH
        Field mHField = activityThreadClass.getDeclaredField("mH");
        mHField.setAccessible(true);
        Handler mH = (Handler) mHField.get(currentActivityThread);
        Field mCallBackField = Handler.class.getDeclaredField("mCallback");
        mCallBackField.setAccessible(true);

        mCallBackField.set(mH, new ActivityThreadHandlerCallback(mH));

    }



}
