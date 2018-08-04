package com.xj.hookdemo.activityhook;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.xj.hookdemo.R;

public class TargetAppCompatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target_app_compat);
    }
}
