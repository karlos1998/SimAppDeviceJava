package it.letscode.simappdevice;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.PowerManager;

public class WifiWakeKeeper {

    private WifiManager.WifiLock mWifiLock;
    private PowerManager.WakeLock mWakeLock;

    public WifiWakeKeeper(Context ctx, String Name) {
        PowerManager powerManager = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
        WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);

        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Name + "-cpu");
        mWifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, Name + "-wifi");
    }

    public boolean isLocking() {
        return mWakeLock.isHeld() && mWifiLock.isHeld();
    }

    public boolean lock() {
        if (!mWakeLock.isHeld()) mWakeLock.acquire();
        if (!mWifiLock.isHeld()) mWifiLock.acquire();

        return isLocking();
    }

    public boolean release() {
        if (mWifiLock.isHeld()) mWifiLock.release();
        if (mWakeLock.isHeld()) mWakeLock.release();

        return !isLocking();
    }
}
