package com.xj.hookdemo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.xj.hookdemo.activityhook.TestClipboardActivity;
import com.xj.hookdemo.activityhook.TestHookStartActivity;
import com.xj.hookdemo.hook.notification.NotificationHookHelper;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onButtonClick(View view) {
        switch (view.getId()) {
            case R.id.btn_1:
                jump(this, TestOnClickActivity.class);
                break;
            case R.id.btn_2:
                jump(this, TestHookStartActivity.class);

                break;
            case R.id.btn_3:
                try {
                    NotificationHookHelper.hookNotificationManager(this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                testNotification();
                break;
            case R.id.btn_4:
                jump(this, TestClipboardActivity.class);
                break;

        }


    }

    private void testNotification() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable
                .ic_launcher_background);
        Intent intent = new Intent(MainActivity.this, TestNotificationActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent,
                FLAG_UPDATE_CURRENT);
        NotificationHelper.notification(MainActivity.this, bitmap, R.mipmap.ic_launcher, "title",
                "content", "subText", 1, pendingIntent);

    }

    public static <T extends Activity> void jump(Context context, Class<T> clz) {
        Intent intent = new Intent(context, clz);
        if (false == (context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);

    }
}
