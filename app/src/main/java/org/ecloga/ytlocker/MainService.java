package org.ecloga.ytlocker;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class MainService extends Service {

    public static final int PASSWORD_RESET_DELAY_MILLIS = 1000;
    public static final String PINNED_MODE_WAITING = "Pinned mode waiting...";
    private WindowManager windowManager;
    private View fullLayout;
    private View rootLayout;
    private View passwordLayout;
    private Handler mHandler;
    private BroadcastReceiver onOffScreenReceiver;
    private ComponentName mReceiverComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("aaa", "MainService.onCreate");

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        fullLayout = layoutInflater.inflate(R.layout.password_layout, null);
        rootLayout = fullLayout.findViewById(R.id.rootLayout);
        passwordLayout = fullLayout.findViewById(R.id.blockLayout);
        mHandler = new Handler(Looper.getMainLooper());
        onOffScreenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateForgroundView(true);
            }
        };

        addForegroundView();
        registerOnOffScreenBroadcastReceiver();
        registerMediaButtonEventReceiver();

        if (isPinnedModeEnabled()) {
            updateForgroundView(true);
        } else {
            Toast.makeText(this, PINNED_MODE_WAITING, Toast.LENGTH_SHORT).show();
            showToastOrLockScreenTouchesAfterDelay();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("aaa", "MainService.onDestroy");
        unregisterMediaButtonEventReceiver();
        unregisterOnOffScreenBroadcastReceiver();
        removeForegroundView();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void registerMediaButtonEventReceiver() {
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mReceiverComponent = new ComponentName(this, DoorbellReceiver.class);
        assert mAudioManager != null;
        mAudioManager.registerMediaButtonEventReceiver(mReceiverComponent);
    }

    private void unregisterMediaButtonEventReceiver() {
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        assert mAudioManager != null;
        mAudioManager.unregisterMediaButtonEventReceiver(mReceiverComponent);
    }

    private void registerOnOffScreenBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(onOffScreenReceiver, intentFilter);
    }

    private void unregisterOnOffScreenBroadcastReceiver() {
        unregisterReceiver(onOffScreenReceiver);
    }

    private void addForegroundView() {
        setupPasswodTextView();

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 0;

        rootLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                Log.d("aaa", "Some action on the screen");
                return false;
            }
        });

        windowManager.addView(fullLayout, params);
    }

    private void removeForegroundView() {
        windowManager.removeView(fullLayout);
    }

    private void updateForgroundView(boolean needBlockTouches) {
        int visibility;
        int width;
        int height;
        int flags;
        if (needBlockTouches) {
            visibility = View.VISIBLE;
            width = WindowManager.LayoutParams.MATCH_PARENT;
            height = WindowManager.LayoutParams.MATCH_PARENT;
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;

//            mHandler.postDelayed(() -> updateForgroundView(false), 15000);
        } else {
            visibility = View.INVISIBLE;
            width = WindowManager.LayoutParams.WRAP_CONTENT;
            height = WindowManager.LayoutParams.WRAP_CONTENT;
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        }

        passwordLayout.setVisibility(visibility);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                width,
                height,
                WindowManager.LayoutParams.TYPE_TOAST,
                flags,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 0;

        windowManager.updateViewLayout(fullLayout, params);
    }

    private void setupPasswodTextView() {
        TextView passwordTextView = passwordLayout.findViewById(R.id.textView);
        passwordTextView.addTextChangedListener(new TextWatcher() {
            Handler handler = new Handler(Looper.getMainLooper());

            @Override
            public void afterTextChanged(Editable s) {
                handler.removeCallbacksAndMessages(null);
                if (isPasswordCorrect(s.toString())) {
                    passwordTextView.setText("");
                    updateForgroundView(false);
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
        ;
    }

    private boolean isPinnedModeEnabled() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        assert activityManager != null;
        return activityManager.isInLockTaskMode();
    }

    private void showToastOrLockScreenTouchesAfterDelay() {
        mHandler.postDelayed(() -> {
                    if (!isPinnedModeEnabled()) {
                        mHandler.post(() -> Toast.makeText(this, PINNED_MODE_WAITING, Toast.LENGTH_SHORT).show());
                        showToastOrLockScreenTouchesAfterDelay();
                    } else {
                        // let user some time to tap OK on the standard hint about pinned mode..
                        mHandler.postDelayed(() -> updateForgroundView(true), 3000);
                    }
                }
                , 5000);
    }

    private void changePasswordWhenButtonClicked(int buttonId, TextView password, String nextPasswordChar) {
        passwordLayout.findViewById(buttonId).setOnClickListener(view -> password.setText(getString(R.string.passwordValue, password.getText(), nextPasswordChar)));
    }

    private boolean isPasswordCorrect(String password) {
        return "123".equals(password);
    }
}
