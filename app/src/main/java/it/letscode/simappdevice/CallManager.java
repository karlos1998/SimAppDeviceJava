package it.letscode.simappdevice;

import android.content.Context;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

public class CallManager {
    private static final String TAG = "CallManager";

    private static final ControllerHttpGateway controllerHttpGateway = new ControllerHttpGateway();

    private static void answerCall(Context context, String incomingNumber) {
        try {
            TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            Log.d(TAG, "Answering call from: " + incomingNumber);

            Thread.sleep(200);

            telecomManager.acceptRingingCall();
            Runtime.getRuntime().exec("su -c input keyevent " + KeyEvent.KEYCODE_HEADSETHOOK);

        } catch (Exception e) {
            Log.e(TAG, "Error answering call: " + e.getMessage());
        }
    }

    public static void callEnded(String incomingNumber) {
        Log.d(TAG, "Call ended for: " + incomingNumber);

        controllerHttpGateway.sendIncomingCall(incomingNumber, TelephonyManager.EXTRA_STATE_IDLE);
    }

    public static void ringingCall(Context context, String incomingNumber) {

        controllerHttpGateway.sendIncomingCall(incomingNumber, TelephonyManager.EXTRA_STATE_RINGING);

        if(incomingNumber.contains("884167733")) {
            answerCall(context, incomingNumber);
        }
    }

    public static void callAnswered(String incomingNumber) {
        Log.d(TAG, "Call answered: " + incomingNumber);
        controllerHttpGateway.sendIncomingCall(incomingNumber, TelephonyManager.EXTRA_STATE_OFFHOOK);
    }
}
