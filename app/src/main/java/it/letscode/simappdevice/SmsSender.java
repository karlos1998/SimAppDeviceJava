package it.letscode.simappdevice;

import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;


public class SmsSender {

    public void sendSms(String phoneNumber, String message) {

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        } catch (Exception e) {
            System.out.println("Wysylanie wiadomosci nie powiodlo sie:");
            e.printStackTrace();
        }
    }
}
