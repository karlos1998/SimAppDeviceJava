package it.letscode.simappdevice;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;


public class SmsSender {

    private final ControllerHttpGateway controllerHttpGateway = new ControllerHttpGateway();
    public SmsSender() {

    }

    public void sendSms(String phoneNumber, String message) {
        int randomId = (int)(Math.random() * 50 + 1);
        sendSms(phoneNumber, message, -randomId);
    }
    public void sendSms(String phoneNumber, String message, int messageId) {

        controllerHttpGateway.markMessageAsOrderReceived(messageId, true, null);

        try {
            SmsManager smsManager = SmsManager.getDefault();

            ArrayList<String> parts = smsManager.divideMessage(message);

            Intent sentIntent = new Intent("SMS_SENT");
            sentIntent.putExtra("phoneNumber", phoneNumber);
            sentIntent.putExtra("messageId", messageId);

            PendingIntent sentPendingIntent = PendingIntent.getBroadcast(ApplicationContextProvider.getApplicationContext(), messageId, sentIntent, PendingIntent.FLAG_IMMUTABLE);


            ArrayList<PendingIntent> sentPendingIntents = new ArrayList<>();
            sentPendingIntents.add(sentPendingIntent);
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, sentPendingIntents, null);

        } catch (Exception e) {
            controllerHttpGateway.markMessageAsOrderReceived(messageId, false, e.toString());
            System.out.println("Wysylanie wiadomosci nie powiodlo sie:");
            e.printStackTrace();
        }
    }
}
