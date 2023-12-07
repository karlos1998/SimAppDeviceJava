package it.letscode.simappdevice;

import android.content.Intent;
import android.os.IBinder;
import android.app.Service;

public class MyBackgroundService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Tutaj umieść kod, który ma być wykonywany w tle
        // Możesz umieścić kod do wysyłania wiadomości SMS
        sendSMS();
        return START_STICKY; // Ta flaga sprawi, że usługa będzie ponownie uruchamiana w razie jej zatrzymania
    }

    private void sendSMS() {
        SmsSender smsSender = new SmsSender();
        smsSender.sendSms("+48884167733", "MyBackgroundService.sendSMS");
    }
}
