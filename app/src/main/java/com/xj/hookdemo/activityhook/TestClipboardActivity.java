package com.xj.hookdemo.activityhook;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.xj.hookdemo.R;
import com.xj.hookdemo.hook.clipboard.ClipboardHelper;

public class TestClipboardActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "TestClipboardActivity";

    private ClipboardManager mClipboardManager;
    /**
     * 英俊潇洒
     */
    private EditText mEt;
    /**
     * copy
     */
    private Button mBtn1;
    /**
     * show
     */
    private Button mBtn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_clipboard);
        try {
            ClipboardHelper.hookClipboardService(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        initView();
        mClipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    }

    public void onButtonClick(View view) {
        ClipData clip = null;
        switch (view.getId()) {
            case R.id.btn_1:
                String text = mEt.getText().toString().trim();
                clip = ClipData.newPlainText("simple text", text);
                mClipboardManager.setPrimaryClip(clip);
                break;
            case R.id.btn_2:

                clip = mClipboardManager.getPrimaryClip();
                Toast.makeText(TestClipboardActivity.this, clip.getItemAt(0).getText(), Toast
                        .LENGTH_SHORT).show();
                break;
        }
    }

    private void initView() {
        mEt = (EditText) findViewById(R.id.et);
        mBtn1 = (Button) findViewById(R.id.btn_1);
        mBtn2 = (Button) findViewById(R.id.btn_2);
        mEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                boolean isOpen=imm.isActive();
                if(!isOpen){
                    imm.showSoftInput(mEt,0);
                }
                Log.i(TAG, "beforeTextChanged: isOpen=" +isOpen);
            }
        });
        mEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.btn_1:
                break;
            case R.id.btn_2:
                break;
        }
    }
}
