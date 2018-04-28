package org.ecloga.ytlocker;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class MainService extends Service {

    public static final int PASSWORD_RESET_DELAY_MILLIS = 1000;
    private WindowManager windowManager;
    private View passwordLayout;

    DevicePolicyManager mDpm;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();




//        new Thread(() -> {
//            Thread.sleep(1000);
//            startLockTask();
//            Thread.sleep(1000);
//            Thread.sleep(1000);
//        }).start();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        passwordLayout = layoutInflater.inflate(R.layout.password_layout, null);

        TextView passwordTextView = passwordLayout.findViewById(R.id.textView);
        passwordTextView.addTextChangedListener(new TextWatcher() {
            Handler handler = new Handler(Looper.getMainLooper());

            @Override
            public void afterTextChanged(Editable s) {
                handler.removeCallbacksAndMessages(null);
                if (isPasswordCorrect(s.toString())) {
                    stopSelf();
                } else {
                    if (!passwordTextView.getText().toString().isEmpty()) {
                        handler.postDelayed(() -> passwordTextView.setText(""), PASSWORD_RESET_DELAY_MILLIS);
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
        });

        changePasswordWhenButtonClicked(R.id.button1, passwordTextView, "1");
        changePasswordWhenButtonClicked(R.id.button2, passwordTextView, "2");
        changePasswordWhenButtonClicked(R.id.button3, passwordTextView, "3");
        changePasswordWhenButtonClicked(R.id.button4, passwordTextView, "4");
        changePasswordWhenButtonClicked(R.id.button5, passwordTextView, "5");
        changePasswordWhenButtonClicked(R.id.button6, passwordTextView, "6");
        changePasswordWhenButtonClicked(R.id.button7, passwordTextView, "7");
        changePasswordWhenButtonClicked(R.id.button8, passwordTextView, "8");
        changePasswordWhenButtonClicked(R.id.button9, passwordTextView, "9");

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 0;

        windowManager.addView(passwordLayout, params);
    }

    private void changePasswordWhenButtonClicked(int buttonId, TextView password, String nextPasswordChar) {
        passwordLayout.findViewById(buttonId).setOnClickListener(view -> password.setText(getString(R.string.passwordValue, password.getText(), nextPasswordChar)));
    }

    private boolean isPasswordCorrect(String password) {
        return "123".equals(password);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        windowManager.removeView(passwordLayout);
    }
}
