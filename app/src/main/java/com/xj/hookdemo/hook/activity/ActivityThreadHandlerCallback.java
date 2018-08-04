package com.xj.hookdemo.hook.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.xj.hookdemo.hook.HookHelper;

import java.lang.reflect.Field;

/**
 * @author xujun  on 17/7/2018.
 */
public class ActivityThreadHandlerCallback implements Handler.Callback {

    private static final String TAG = "ActivityThreadHandlerCa";
    Handler mBase;

    public ActivityThreadHandlerCallback(Handler base) {
        mBase = base;
    }
    @Override
    public boolean handleMessage(Message msg) {
        Log.d(TAG, "handleMessage: msg=" +msg);
        switch (msg.what) {
            // ActivityThread里面 "LAUNCH_ACTIVITY" 这个字段的值是100
            case 100:
                handleLaunchActivity(msg);
                Log.d(TAG, "handleMessage: msg=" +msg);
                break;
        }

//        mBase.handleMessage(msg);
        return false;
    }

    private void handleLaunchActivity(Message msg) {
        Object obj = msg.obj;
        try {
            // 把替身恢复成真身
            Field intent = obj.getClass().getDeclaredField("intent");
            intent.setAccessible(true);
            Intent raw = (Intent) intent.get(obj);
            Intent target = raw.getParcelableExtra(HookHelper.EXTRA_TARGET_INTENT);
            if(target!=null){
                ComponentName component = target.getComponent();
                raw.setComponent(component);
                Log.i(TAG, "handleLaunchActivity: component=" +component);
            }

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
