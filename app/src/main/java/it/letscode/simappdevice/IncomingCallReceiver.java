package it.letscode.simappdevice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class IncomingCallReceiver extends BroadcastReceiver {
    private static final String TAG = "IncomingCallReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.PHONE_STATE")) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            if (state != null) {
                if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    Log.d(TAG, "Incoming call from: " + incomingNumber);

                    // Odbieranie połączenia
//                    try {
//                        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//                        Class<?> telephonyClass = Class.forName(telephonyManager.getClass().getName());
//                        java.lang.reflect.Method method = telephonyClass.getDeclaredMethod("getITelephony");
//                        method.setAccessible(true);
//                        Object telephonyInterface = method.invoke(telephonyManager);
//
//                        Class<?> telephonyInterfaceClass = Class.forName(telephonyInterface.getClass().getName());
//                        java.lang.reflect.Method answerMethod = telephonyInterfaceClass.getDeclaredMethod("answerRingingCall");
//                        answerMethod.invoke(telephonyInterface);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }

                } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    Log.d(TAG, "Call ended");
                }
            }
        }
    }
}
