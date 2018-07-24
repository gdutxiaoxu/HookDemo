package com.example.administrator.hookdemo.activityhook;

import android.app.Activity;
import android.os.Bundle;

import com.example.administrator.hookdemo.R;

public class TargetActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target);
    }
}
