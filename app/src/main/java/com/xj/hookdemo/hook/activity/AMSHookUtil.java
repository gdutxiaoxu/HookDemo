package com.xj.hookdemo.hook.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author xujun  on 17/7/2018.
 */
public class AMSHookUtil {

    public static final String ORIGINALLY_INTENT = AMSHookInvocationHandler.ORIGINALLY_INTENT;
    private static final String TAG = "AMSHookUtil";

    /**
     * 这里我们通过反射获取到AMS的代理本地代理对象
     * Hook以后动态串改Intent为已注册的来躲避检测
     *
     * @param context             上下文
     * @param isAppCompatActivity 是否是 AppCompatActivity
     */
    public static void hookActivity(Context context, boolean isAppCompatActivity) {
        if (context == null) {
            return;
        }
        try {
            // hook AMS
            hookAMS(context);
            // 在 activity launch 的时候欺骗 AMS
            hookLaunchActivity(context, isAppCompatActivity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getHostClzName(Context context, String pmName) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(pmName, PackageManager
                    .GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
        ActivityInfo[] activities = packageInfo.activities;
        if (activities == null || activities.length == 0) {
            return "";
        }
        ActivityInfo activityInfo = activities[0];
        return activityInfo.name;

    }

    private static String getPMName(Context context) {
        // 获取当前进程已经注册的 activity
        Context applicationContext = context.getApplicationContext();
        return applicationContext.getPackageName();
    }

    private static void hookAMS(Context context) throws ClassNotFoundException,
            NoSuchFieldException, IllegalAccessException {
        // 第一步，  API 26 以后，hook android.app.ActivityManager.IActivityManagerSingleton，
        //  API 25 以前，hook android.app.ActivityManagerNative.gDefault
        Field gDefaultField = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Class<?> activityManager = Class.forName("android.app.ActivityManager");
            gDefaultField = activityManager.getDeclaredField("IActivityManagerSingleton");
        } else {
            Class<?> activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");
            gDefaultField = activityManagerNativeClass.getDeclaredField("gDefault");
        }
        gDefaultField.setAccessible(true);
        Object gDefaultObj = gDefaultField.get(null); //所有静态对象的反射可以通过传null获取。如果是实列必须传实例
        Class<?> singletonClazz = Class.forName("android.util.Singleton");
        Field amsField = singletonClazz.getDeclaredField("mInstance");
        amsField.setAccessible(true);
        Object amsObj = amsField.get(gDefaultObj);

        //
        String pmName = getPMName(context);
        String hostClzName = getHostClzName(context, pmName);

        // 第二步，获取我们的代理对象，这里因为是接口，所以我们使用动态代理的方式
        amsObj = Proxy.newProxyInstance(context.getClass().getClassLoader(), amsObj.getClass()
                .getInterfaces(), new AMSHookInvocationHandler(amsObj, pmName, hostClzName));

        // 第三步：设置为我们的代理对象
        amsField.set(gDefaultObj, amsObj);
    }

    /**
     *
     * @param context
     * @param isAppCompatActivity 表示是否是 AppCompatActivity
     * @throws Exception
     */
    private static void hookLaunchActivity(Context context, boolean isAppCompatActivity) throws
            Exception {
        Class<?> activityThreadClazz = Class.forName("android.app.ActivityThread");
        Field sCurrentActivityThreadField = activityThreadClazz.getDeclaredField("sCurrentActivityThread");
        sCurrentActivityThreadField.setAccessible(true);
        Object sCurrentActivityThreadObj = sCurrentActivityThreadField.get(null);

        Field mHField = activityThreadClazz.getDeclaredField("mH");
        mHField.setAccessible(true);
        Handler mH = (Handler) mHField.get(sCurrentActivityThreadObj);
        Field callBackField = Handler.class.getDeclaredField("mCallback");
        callBackField.setAccessible(true);
        callBackField.set(mH, new ActivityThreadHandlerCallBack(context, isAppCompatActivity));
    }

    public static class ActivityThreadHandlerCallBack implements Handler.Callback {

        private final boolean mIsAppCompatActivity;
        private final Context mContext;

        public ActivityThreadHandlerCallBack(Context context, boolean isAppCompatActivity) {
            mIsAppCompatActivity = isAppCompatActivity;
            mContext = context;
        }

        @Override
        public boolean handleMessage(Message msg) {
            int LAUNCH_ACTIVITY = 0;
            try {
                Class<?> clazz = Class.forName("android.app.ActivityThread$H");
                Field field = clazz.getField("LAUNCH_ACTIVITY");
                LAUNCH_ACTIVITY = field.getInt(null);
            } catch (Exception e) {
            }
            if (msg.what == LAUNCH_ACTIVITY) {
                handleLaunchActivity(mContext, msg, mIsAppCompatActivity);
            }
            return false;
        }
    }

    private static void handleLaunchActivity(Context context, Message msg, boolean
            isAppCompatActivity) {
        try {
            Object obj = msg.obj;
            Field intentField = obj.getClass().getDeclaredField("intent");
            intentField.setAccessible(true);
            Intent proxyIntent = (Intent) intentField.get(obj);
            //拿到之前真实要被启动的Intent 然后把Intent换掉
            Intent originallyIntent = proxyIntent.getParcelableExtra(ORIGINALLY_INTENT);
            if (originallyIntent == null) {
                return;
            }
            proxyIntent.setComponent(originallyIntent.getComponent());

            Log.e(TAG, "handleLaunchActivity:" + originallyIntent.getComponent().getClassName());

            // 如果不需要兼容 AppCompatActivity
            if (!isAppCompatActivity) {
                return;
            }

            //兼容AppCompatActivity
            hookPM(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void hookPM(Context context) throws ClassNotFoundException,
            NoSuchFieldException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        String pmName = getPMName(context);
        String hostClzName = getHostClzName(context, pmName);

        Class<?> forName = Class.forName("android.app.ActivityThread");
        Field field = forName.getDeclaredField("sCurrentActivityThread");
        field.setAccessible(true);
        Object activityThread = field.get(null);
        Method getPackageManager = activityThread.getClass().getDeclaredMethod("getPackageManager");
        Object iPackageManager = getPackageManager.invoke(activityThread);
        PackageManagerHandler handler = new PackageManagerHandler(iPackageManager, pmName, hostClzName);
        Class<?> iPackageManagerIntercept = Class.forName("android.content.pm.IPackageManager");
        Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new
                Class<?>[]{iPackageManagerIntercept}, handler);
        // 获取 sPackageManager 属性
        Field iPackageManagerField = activityThread.getClass().getDeclaredField("sPackageManager");
        iPackageManagerField.setAccessible(true);
        iPackageManagerField.set(activityThread, proxy);
    }

    private static class PackageManagerHandler implements InvocationHandler {
        private final String mPmName;
        private final String mHostClzName;
        private Object mActivityManagerObject;

        PackageManagerHandler(Object mActivityManagerObject, String pmName, String hostClzName) {
            this.mActivityManagerObject = mActivityManagerObject;
            mPmName = pmName;
            mHostClzName = hostClzName;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("getActivityInfo")) {
                ComponentName componentName = new ComponentName(mPmName, mHostClzName);
                args[0] = componentName;
            }
            return method.invoke(mActivityManagerObject, args);
        }
    }

}
