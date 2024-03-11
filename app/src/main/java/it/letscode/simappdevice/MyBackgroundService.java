package it.letscode.simappdevice;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.json.JSONObject;

public class MyBackgroundService extends Service {

    private final Handler handler = new Handler();
    private static final String CHANNEL_ID = "ForegroundServiceChannel";

    private final MyPreferences myPreferences = new MyPreferences();
    private static final OwnHttpClient httpClient = new OwnHttpClient();

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Aplikacja działa w tle")
                .setContentText("Dotknij, aby otworzyć.")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        // Uruchomienie zadania wysyłającego POST co 10 sekund
        handler.postDelayed(postRunnable, 0); // Start without delay

        return START_NOT_STICKY;
    }

    private final Runnable postRunnable = new Runnable() {
        @Override
        public void run() {
            sendPostRequest();
            handler.postDelayed(this, 10000); // Schedule again
        }
    };

    private void sendPostRequest() {
        httpClient.get("https://webhook.site/03520281-10ee-4187-9f4a-f408d51b4d44", new OwnHttpClient.HttpResponseCallback() {
            @Override
            public void onResponse(JSONObject data, int responseCode) {
            }

            @Override
            public void onFailure(Throwable throwable) {
            }

            @Override
            public void onError(JSONObject data, int responseCode) {

            }
        });
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(postRunnable);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Foreground Service";
            String description = "Channel for Foreground Service";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
