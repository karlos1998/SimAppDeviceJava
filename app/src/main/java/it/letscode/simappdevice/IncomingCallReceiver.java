package it.letscode.simappdevice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

import java.io.IOException;

public class IncomingCallReceiver extends BroadcastReceiver {
    private static final String TAG = "IncomingCallReceiver";
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.PHONE_STATE")) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            if (state != null) {
                String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    Log.d(TAG, "Incoming call from: " + incomingNumber);
                    answerCall();
                    playAudioForCaller(context, "https://download.samplelib.com/mp3/sample-9s.mp3");
                } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    Log.d(TAG, "Call ended: " + incomingNumber);
                    stopAudio();
                }
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

    private void playAudioForCaller(Context context, String audioUrl) {
        try {
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                audioManager.setMode(AudioManager.MODE_IN_CALL);
                audioManager.setSpeakerphoneOn(false); // Wyłączenie głośnika, aby dźwięk był przesyłany do rozmówcy
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL); // Ustawienie strumienia na VOICE_CALL
            mediaPlayer.setDataSource(context, Uri.parse(audioUrl));
            mediaPlayer.prepare();
            mediaPlayer.setLooping(true); // Opcjonalnie: powtarzanie dźwięku
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopAudio() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
