package it.letscode.simappdevice;

import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.app.Service;

public class MyBackgroundService extends Service {

    private static final long DELAY_TIME = 30000;

    private final MyPreferences myPreferences = new MyPreferences();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendSMS();
                startMainActivity();
            }
        }, DELAY_TIME);

        return START_STICKY;
    }

    private void startMainActivity() {
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(mainActivityIntent);
    }

    private void sendSMS() {
        if(myPreferences.trustedNumberExist()) {
            SmsSender smsSender = new SmsSender();
            smsSender.sendSms(myPreferences.getTrustedNumber(), "MyBackgroundService.sendSMS");
        };
    }
}
