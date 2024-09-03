package it.letscode.simappdevice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioTrack;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class IncomingCallReceiver extends BroadcastReceiver {
    private static final String TAG = "IncomingCallReceiver";
    private static final String SECONDARY_NUMBER = "+48606816819";  // Numer do zadzwonienia po odebraniu
    private static final int DELAY_MILLIS = 10000;  // Opóźnienie przed dodaniem kolejnej osoby (10 sekund)
    private AudioTrack audioTrack;
    private boolean isPlaying = false;

    ControllerHttpGateway controllerHttpGateway = new ControllerHttpGateway();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.PHONE_STATE")) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            if (state != null) {
                String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    Log.d(TAG, "Incoming call from: " + incomingNumber);
//                    answerCall();
//                    scheduleSecondaryCall();
                } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    Log.d(TAG, "Call ended: " + incomingNumber);
                }
                controllerHttpGateway.sendIncomingCall(incomingNumber, state);
            }
        }
    }

    private void answerCall() {
        try {
            Log.d(TAG, "Answering call...");
            Runtime.getRuntime().exec("su -c input keyevent " + KeyEvent.KEYCODE_HEADSETHOOK);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void scheduleSecondaryCall() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "Calling secondary number: " + SECONDARY_NUMBER);
                    Runtime.getRuntime().exec("su -c am start -a android.intent.action.CALL -d tel:" + SECONDARY_NUMBER);
                    mergeCalls();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, DELAY_MILLIS);
    }

    private void mergeCalls() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "Merge calls");
                    Runtime.getRuntime().exec("su -c input keyevent " + KeyEvent.KEYCODE_HEADSETHOOK);
                    mergeCalls();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, DELAY_MILLIS * 3);
    }
}
