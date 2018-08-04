package com.xj.hookdemo.activityhook;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.xj.hookdemo.App;
import com.xj.hookdemo.R;
import com.xj.hookdemo.hook.HookHelper;

public class TestHookStartActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test_hook_start);
    }

    public void onButtonClick(View view){
        switch (view.getId()){
            case R.id.btn_1:
                App.reset();
                try {
                    HookHelper.replaceInstrumentation(this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                startActivity(new Intent(this,TestActivityStart.class));
                break;
            case R.id.btn_2:
                App.reset();
                try {
                    HookHelper.attachContext();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(this, TestActivityStart.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
                break;
            case R.id.btn_3:
                App.reset();
                try {
                    HookHelper.hookAMS();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                startActivity(new Intent(this,TestActivityStart.class));
                break;

            case R.id.btn_4:
                App.reset();
                startActivity(new Intent(this,TestStartActivityNoRegister.class));
                break;


            case R.id.btn_resetHook:
                App.reset();
                break;

        }
    }
}
