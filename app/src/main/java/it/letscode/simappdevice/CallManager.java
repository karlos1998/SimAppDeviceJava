package it.letscode.simappdevice;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

import androidx.core.app.ActivityCompat;

import org.json.JSONException;
import org.json.JSONObject;

public class CallManager {
    private static final String TAG = "CallManager";

    private static final ControllerHttpGateway controllerHttpGateway = new ControllerHttpGateway();

    private static void answerCall(Context context) {
        try {
            TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);

            Thread.sleep(200);

            if(RootChecker.isDeviceRooted()) {
                Runtime.getRuntime().exec("su -c input keyevent " + KeyEvent.KEYCODE_HEADSETHOOK);
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
                telecomManager.acceptRingingCall();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error answering call: " + e.getMessage());
        }
    }

    public static void callEnded(String incomingNumber) {
        Log.d(TAG, "Call ended for: " + incomingNumber);

        controllerHttpGateway.sendIncomingCall(incomingNumber, TelephonyManager.EXTRA_STATE_IDLE);
    }

    public static void ringingCall(Context context, String incomingNumber) {

        controllerHttpGateway.sendIncomingCall(incomingNumber, TelephonyManager.EXTRA_STATE_RINGING, new ControllerHttpGateway.ResponseCallback() {
            @Override
            public void onResponse(JSONObject data, int responseCode) {
                try {
                    String action = data.getString("action");

                    if(action.equals("answer")) {
                        Log.d(TAG, "Answering call from: " + incomingNumber);
                        answerCall(context);
                    } else if(action.equals("reject")) {
                        Log.d(TAG, "Rejecting call from: " + incomingNumber);
                        rejectCall(context);
                    }
                } catch (JSONException ignore) {

                }
            }
            @Override
            public void onFailure(Throwable throwable) {}
        });

    }

    public static void rejectCall(Context context)
    {
        try {
            TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);


            if(RootChecker.isDeviceRooted()) {
                Runtime.getRuntime().exec("su -c input keyevent " + KeyEvent.KEYCODE_ENDCALL);
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
                telecomManager.endCall();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error rejecting call: " + e.getMessage());
        }
    }

    public static void callAnswered(String incomingNumber) {
        Log.d(TAG, "Call answered: " + incomingNumber);
        controllerHttpGateway.sendIncomingCall(incomingNumber, TelephonyManager.EXTRA_STATE_OFFHOOK);
    }
}
