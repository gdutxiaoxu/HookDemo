package com.xj.hookdemo;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.xj.hookdemo.hook.HookResetUtils;

/**
 * @author xujun  on 17/7/2018.
 */
public class App extends Application {

    private static Object mObject;
    private static Handler mHandler;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            mObject = HookResetUtils.storeAms();
            mHandler = HookResetUtils.storeActivityLaunch();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void reset(){
        try {
            HookResetUtils.resetAms(mObject);
            HookResetUtils.resetActivityLaunch(mHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
