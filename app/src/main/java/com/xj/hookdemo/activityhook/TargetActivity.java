package com.xj.hookdemo.activityhook;

import android.app.Activity;
import android.os.Bundle;

import com.xj.hookdemo.R;

public class TargetActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target);
    }
}
