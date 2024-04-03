package it.letscode.simappdevice;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import java.util.Random;

public class WifeeperHelper {

    public static Notification createNotification(Context context, Intent intent) {

// Stworzenie PendingIntent, które otwiera aplikację (lub inną aktywność) po kliknięciu na powiadomienie
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Budowanie powiadomienia
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "LCI_APP1")
                .setSmallIcon(context.getApplicationInfo().icon) // Upewnij się, że masz tę ikonę w zasobach
                .setContentTitle("Simply Connect App") // Tytuł powiadomienia
                .setContentText("The service runs in the background.") // Treść powiadomienia
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Opcjonalnie ustaw priorytet
                .setOngoing(true) // Sprawia, że powiadomienie jest trwałe
                .setContentIntent(pendingIntent); // Ustawienie intentu, który ma być wykonany przy kliknięciu

        return builder.build();

    }
}

