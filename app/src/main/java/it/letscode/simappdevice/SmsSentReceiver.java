package it.letscode.simappdevice;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

public class SmsSentReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsSentReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("SMS_SENT")) {
            int resultCode = getResultCode();

            String phoneNumber = intent.getStringExtra("phoneNumber");
            int messageId = intent.getIntExtra("messageId", -1);


            String message;
            switch (resultCode) {
                case Activity.RESULT_OK:
                    message = "Wiadomość wysłana pomyślnie";
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    message = "Błąd wysyłania - ogólny błąd";
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    message = "Błąd wysyłania - brak zasięgu";
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    message = "Błąd wysyłania - pusty PDU";
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    message = "Błąd wysyłania - radio wyłączone";
                    break;
                default:
                    message = "Nieznany błąd wysyłania";
                    break;
            }

            Log.d(TAG, message);

            Log.d(TAG, "Message ID: " + messageId);
            Log.d(TAG, "Recipient: " + phoneNumber);

            // Tutaj możesz podejmować dalsze działania związane z obsługą błędów wysyłania
        }
    }
}
