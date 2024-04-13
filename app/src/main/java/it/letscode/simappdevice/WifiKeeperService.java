package it.letscode.simappdevice;

import static it.letscode.simappdevice.WifeeperHelper.createNotification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

public class WifiKeeperService extends Service {

    private WifiWakeKeeper mKeeper;

    @Override
    public void onCreate() {
        super.onCreate();

        ApplicationContextProvider.initialize(this);

        mKeeper = new WifiWakeKeeper(this, "Wifeeper");

        Bootloader.run();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String command = intent != null ? intent.getStringExtra("command") : null;
        if (command == null) {
            if (!mKeeper.isLocking()) {
                if (mKeeper.lock()) {

                    ///
                    Intent stopIntent = new Intent(this, this.getClass());
                    stopIntent.putExtra("command", "stop");
                    System.out.println("Create notification ...");
                    startForeground(1, createNotification(this, stopIntent));
                }
            }
            return Service.START_STICKY;
        } else if ("stop".equals(command)) {
            Log.d("WifiKeeperService", "Stopping myself");
            stopForeground(true);
            stopSelf();
            return Service.START_NOT_STICKY;
        } else {
            return Service.START_STICKY;
        }
    }

}
