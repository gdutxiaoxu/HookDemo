package com.xj.hookdemo.hook.activity;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * @author xujun  on 16/7/2018.
 */
public  class ActivityProxyInstrumentation extends Instrumentation {

    private static final String TAG = "ActivityProxyInstrumentation";

    // ActivityThread中原始的对象, 保存起来
    Instrumentation mBase;

    public ActivityProxyInstrumentation(Instrumentation base) {
        mBase = base;
    }

    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {

        // Hook之前, 可以输出你想要的!
        Log.d(TAG,"xxxx: 执行了startActivity, 参数如下: " + "who = [" + who + "], " +
                "contextThread = [" + contextThread + "], token = [" + token + "], " +
                "target = [" + target + "], intent = [" + intent +
                "], requestCode = [" + requestCode + "], options = [" + options + "]");

        // 开始调用原始的方法, 调不调用随你,但是不调用的话, 所有的startActivity都失效了.
        // 由于这个方法是隐藏的,因此需要使用反射调用;首先找到这个方法
        try {
            Method execStartActivity = Instrumentation.class.getDeclaredMethod(
                    "execStartActivity",
                    Context.class, IBinder.class, IBinder.class, Activity.class,
                    Intent.class, int.class, Bundle.class);
            execStartActivity.setAccessible(true);
            return (ActivityResult) execStartActivity.invoke(mBase, who,
                    contextThread, token, target, intent, requestCode, options);
        } catch (Exception e) {
            // rom修改了 需要手动适配
            throw new RuntimeException("do not support!!! pls adapt it");
        }
    }


}
