package org.ecloga.ytlocker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private DevicePolicyManager mDpm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ComponentName deviceAdmin = new ComponentName(this, AdminReceiver.class);
        mDpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (!mDpm.isAdminActive(deviceAdmin)) {
            Log.e("Kiosk Mode Error", "not_device_admin");
        }

        boolean isAdminActive = mDpm.isAdminActive(deviceAdmin);
        boolean isDeviceOwnerApp = mDpm.isDeviceOwnerApp(getPackageName());

        if (isDeviceOwnerApp) {
            mDpm.setLockTaskPackages(deviceAdmin, new String[]{getPackageName()});
        } else {
            Log.e("Kiosk Mode Error", "not_device_owner");
        }

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/timeburnernormal.ttf");

        final Button btnToggle = (Button) findViewById(R.id.btnToggle);
        btnToggle.setTypeface(font);
        btnToggle.setTextColor(Color.parseColor("#ecf0f1"));

        DisplayMetrics metrics;
        metrics = getApplicationContext().getResources().getDisplayMetrics();
        float Textsize = btnToggle.getTextSize() / metrics.density;
        btnToggle.setTextSize(Textsize + 8);

        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        TextView tvAuthor = (TextView) findViewById(R.id.tvAuthor);

        tvTitle.setTypeface(font);
        tvAuthor.setTypeface(font);

        tvTitle.setTextSize(Textsize + 20);
        tvAuthor.setTextSize(Textsize + 2);

        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        btnToggle.setHeight(Math.min(height, width) / 3);
        btnToggle.setWidth(Math.min(height, width) / 3);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String started = prefs.getString("started", "");

        if(started.equals("true")) {
            btnToggle.setText("Stop");
            btnToggle.setBackground(getResources().getDrawable(R.drawable.stop));
        }else {
            btnToggle.setText("Start");
            btnToggle.setBackground(getResources().getDrawable(R.drawable.start));
        }

        PendingIntent pi = PendingIntent.getService(MainActivity.this,
                0, new Intent(MainActivity.this, MainService.class), 0);

        final Notification notification = new NotificationCompat.Builder(MainActivity.this)
                .setTicker("YTLocker")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("YTLocker")
                .setContentText("Click this to block touches")
                .setContentIntent(pi)
                .setOngoing(true)
                .build();

        final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        btnToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                Animation zoom = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom);

                if(btnToggle.getText() == "Start") {
                    btnToggle.setText("Stop");
                    btnToggle.setBackground(getResources().getDrawable(R.drawable.stop));

                    notificationManager.notify(21098, notification);

                    editor.putString("started", "true");
                    startLockTask();
                }else {
                    btnToggle.setText("Start");
                    btnToggle.setBackground(getResources().getDrawable(R.drawable.start));

                    notificationManager.cancel(21098);

                    editor.putString("started", "false");
                    stopLockTask();
                }

                btnToggle.startAnimation(zoom);
                editor.apply();
            }
        });
    }
}
