package it.letscode.simappdevice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class IncomingCallReceiver extends BroadcastReceiver {
    private static final String TAG = "IncomingCallReceiver";
    private static final long TIME_LIMIT = 2000; // Czas w milisekundach

    private static long lastCallTime = 0;
    private static String lastIncomingNumber = "";

    private static long lastCallEndedTime = 0;

    private static long lastAnsweredTime = 0;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.PHONE_STATE")) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            final String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

            if(incomingNumber == null) return;

            if (state == null) return;

            long currentTime = System.currentTimeMillis();

            Log.d(TAG, "State: " + state);

            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                if (!incomingNumber.equals(lastIncomingNumber) || (currentTime - lastCallTime > TIME_LIMIT)) {
                    Log.d(TAG, "Incoming call from: " + incomingNumber);

                    lastIncomingNumber = incomingNumber;
                    lastCallTime = currentTime;

                    CallManager.ringingCall(context, incomingNumber);
                }
            } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                if(currentTime - lastCallEndedTime > TIME_LIMIT) {
                    Log.d(TAG, "Call ended for number: " + incomingNumber);

                    lastCallEndedTime = currentTime;

                    CallManager.callEnded(incomingNumber);
                }
            } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                if(currentTime - lastAnsweredTime > TIME_LIMIT) {
                    CallManager.callAnswered(incomingNumber);

                    lastAnsweredTime = currentTime;
                }
            }
        }
    }
}
