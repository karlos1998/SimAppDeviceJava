package it.letscode.simappdevice;

import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;

import java.util.ArrayList;


public class SmsSender {

    public void sendSms(String phoneNumber, String message) {

        try {
            SmsManager smsManager = SmsManager.getDefault();

            ArrayList<String> parts = smsManager.divideMessage(message);

            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
        } catch (Exception e) {
            System.out.println("Wysylanie wiadomosci nie powiodlo sie:");
            e.printStackTrace();
        }
    }
}
