package com.example.administrator.hookdemo;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.example.administrator.hookdemo.hook.AMSHookUtil;

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
            mObject = AMSHookUtil.storeAms();
            mHandler = AMSHookUtil.storeActivityLaunch();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void reset(){
        try {
            AMSHookUtil.resetAms(mObject);
            AMSHookUtil.resetActivityLaunch(mHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
