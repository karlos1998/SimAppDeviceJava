package it.letscode.simappdevice;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;

import java.util.ArrayList;


public class SmsSender {

    private Context context;

    public SmsSender(Context context) {
        this.context = context;
    }

    public SmsSender() {

    }

    public void sendSms(String phoneNumber, String message) {
        int randomId = (int)(Math.random() * 50 + 1);
        sendSms(phoneNumber, message, -randomId);
    }
    public void sendSms(String phoneNumber, String message, int messageId) {

        try {
            SmsManager smsManager = SmsManager.getDefault();

            ArrayList<String> parts = smsManager.divideMessage(message);

            if(context != null) {
                Intent sentIntent = new Intent("SMS_SENT");
                sentIntent.putExtra("phoneNumber", phoneNumber);
                sentIntent.putExtra("messageId", messageId);

                PendingIntent sentPendingIntent = PendingIntent.getBroadcast(context, messageId, sentIntent, PendingIntent.FLAG_IMMUTABLE);


                ArrayList<PendingIntent> sentPendingIntents = new ArrayList<>();
                sentPendingIntents.add(sentPendingIntent);
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, sentPendingIntents, null);
            } else {
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
            }

        } catch (Exception e) {
            System.out.println("Wysylanie wiadomosci nie powiodlo sie:");
            e.printStackTrace();
        }
    }
}
