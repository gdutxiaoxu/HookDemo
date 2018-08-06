package com.xj.hookdemo.activityhook;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.xj.hookdemo.App;
import com.xj.hookdemo.R;
import com.xj.hookdemo.hook.activity.AMSHookUtil;

public class TestStartActivityNoRegister extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_start_no_register);
    }

    public void onButtonClick(View v) {
        switch (v.getId()) {
            case R.id.btn_1:
                App.reset();
                try {
                    AMSHookUtil.hookActivity(this, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                startActivity(new Intent(this, TargetAppCompatActivity.class));
                break;
            case R.id.btn_2:
                App.reset();
                try {
                    //                    HookHelper.hookActivityThreadHandler();
                    //                    HookHelper.hookActivityManagerNative();
                    AMSHookUtil.hookActivity(this, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                startActivity(new Intent(this, TargetActivity.class));
                break;
            case R.id.btn_3:
                App.reset();
                startActivity(new Intent(this, TargetAppCompatActivity.class));
                break;

            default:
                break;
        }
    }
}
