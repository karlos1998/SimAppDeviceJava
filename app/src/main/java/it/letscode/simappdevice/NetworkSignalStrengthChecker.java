package it.letscode.simappdevice;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import java.util.List;

public class NetworkSignalStrengthChecker {
    private Context context;
    private long lastLoggedTime = 0;
    private static final long LOG_INTERVAL = 10000; // 10 sekund w milisekundach

    private static int lastKnownSignalStrength;


    public NetworkSignalStrengthChecker(Context context) {
        this.context = context;
    }

    public void startSignalStrengthCheck() {
        final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

//        ControllerHttpGateway controllerHttpGateway = new ControllerHttpGateway();

        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);

                long currentTime = System.currentTimeMillis();
                if (currentTime - lastLoggedTime >= LOG_INTERVAL) {
                    lastKnownSignalStrength = convertSignal(signalStrength);

                    lastLoggedTime = currentTime;

//                    controllerHttpGateway.sendSignalStrength(lastKnownSignalStrength);

                }
            }
        };

        // Rejestracja PhoneStateListener do odczytu zmian w sygnale
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    private int convertSignal(SignalStrength signalStrength) {
        if (signalStrength == null) {
            return 0;
        } else {
            if (signalStrength.isGsm()) {
                int signalDbm = signalStrength.getGsmSignalStrength();
                return (2 * signalDbm) - 113; // Konwersja sygnału GSM na dBm
            } else {
                return signalStrength.getCdmaDbm();
            }
        }
    }

    public static int getSignalStrength() {
        return lastKnownSignalStrength;
    }
}