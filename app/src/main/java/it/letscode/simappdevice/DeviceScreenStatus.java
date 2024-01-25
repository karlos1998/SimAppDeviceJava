package it.letscode.simappdevice;

import android.content.Context;
import android.os.PowerManager;

public class DeviceScreenStatus {
    public static boolean isScreenOn() {
        PowerManager powerManager = (PowerManager) ApplicationContextProvider.getApplicationContext().getSystemService(Context.POWER_SERVICE);
        if (powerManager == null) {
            return false;
        }

        return powerManager.isInteractive();
    }
}
