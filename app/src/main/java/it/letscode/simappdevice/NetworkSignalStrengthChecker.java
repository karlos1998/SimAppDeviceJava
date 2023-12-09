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

    public NetworkSignalStrengthChecker(Context context) {
        this.context = context;
    }

    public void startSignalStrengthCheck() {
        final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);

                long currentTime = System.currentTimeMillis();
                if (currentTime - lastLoggedTime >= LOG_INTERVAL) {
                    int signalStrengthValue = getSignalStrength(signalStrength);

                    // Logowanie zasięgu w konsoli
                    System.out.println("Signal Strength: " + signalStrengthValue + " dBm");

                    lastLoggedTime = currentTime;
                }
            }
        };

        // Rejestracja PhoneStateListener do odczytu zmian w sygnale
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    private int getSignalStrength(SignalStrength signalStrength) {
        if (signalStrength == null) {
            return 0;
        } else {
            if (signalStrength.isGsm()) {
                int signalDbm = signalStrength.getGsmSignalStrength();
                return (2 * signalDbm) - 113; // Konwersja sygnału GSM na dBm
            } else {
                int signalDbm = signalStrength.getCdmaDbm();
                return signalDbm;
            }
        }
    }
}