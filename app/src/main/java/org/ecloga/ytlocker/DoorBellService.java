package org.ecloga.ytlocker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.widget.Toast;

import java.io.IOException;

public class DoorBellService extends Service {
    public static final long WAKELOCK_TIMEOUT = 20 * 1000L;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ringTheBell();
        turnOnDeviceScreen();
        Handler handler = new Handler(Looper.getMainLooper());
        if(Utils.requestRootPrivileges()) {
            handler.postDelayed(
                    this::stopSelf,
                    WAKELOCK_TIMEOUT
            );
        } else {
            handler.post(() -> Toast.makeText(this, "Please turn off screen manually on device without root!", Toast.LENGTH_LONG).show());
        }
    }

    @Override
    public void onDestroy() {
        turnOffScreen();
        super.onDestroy();
    }

    private void ringTheBell() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(this, notification);
        r.play();
    }

    private void turnOnDeviceScreen() {
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        assert pm != null;
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
        wl.acquire(WAKELOCK_TIMEOUT);
    }

    private void turnOffScreen() {
        try {
            Runtime.getRuntime().exec(new String[] { "su", "-c", "input keyevent 26"});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
