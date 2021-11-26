package com.xj.hookdemo.thread

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.xj.hookdemo.R

class TestThreadHookActivity : AppCompatActivity() {

    private val TAG = "TestThreadHookActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_thread_hook)
        ThreadHookUtils.hook()
        findViewById<View>(R.id.btn_start_new_thread).setOnClickListener {
            Log.i(TAG, "onCreate: ")
        }
    }


}