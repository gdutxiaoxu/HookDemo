package com.xj.hookdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.xj.hookdemo.hook.HookHelper;

public class TestOnClickActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "TestOnClickActivity";
    private Button mBtn1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_on_click);
        initView();
    }


    private void initView() {
        mBtn1 = (Button) findViewById(R.id.btn_1);
        mBtn1.setOnClickListener(this);
        try {
            HookHelper.hookOnClickListener(mBtn1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.btn_1:
                Log.i(TAG, "onClick: btn_1");
                break;
        }
    }
}
