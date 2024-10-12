package it.letscode.simappdevice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SmsDeliveryReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsDeliveryReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (getResultCode()) {
            case android.app.Activity.RESULT_OK:
                Log.d(TAG, "SMS delivered successfully");
                //..todo
                break;
            case android.app.Activity.RESULT_CANCELED:
                Log.d(TAG, "SMS delivery failed");
                //.. todo
                break;
            default:
                Log.d(TAG, "Unknown result code: " + getResultCode());
                break;
        }
    }
}