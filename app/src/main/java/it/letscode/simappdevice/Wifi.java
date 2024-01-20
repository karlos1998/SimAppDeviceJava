package it.letscode.simappdevice;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class Wifi {

    private String ssid;
    private int signalStrength;
    WifiManager wifiManager = (WifiManager) ApplicationContextProvider.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    public void getCurrentNetwork () {
        WifiInfo info = wifiManager.getConnectionInfo();
        ssid = info.getSSID();
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        signalStrength = WifiManager.calculateSignalLevel(info.getRssi(), 100);
        Log.d("WiFiInfo", "SSID: " + ssid + ", Siła sygnału: " + signalStrength + "%");
    }

    public String getSsid()
    {
        return ssid;
    }

    public int getSignalStrength()
    {
        return signalStrength;
    }
}
