package it.letscode.simappdevice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Wifi {

    private String ssid;
    private int signalStrength;
    WifiManager wifiManager = (WifiManager) ApplicationContextProvider.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

    public void getCurrentNetwork() {
        WifiInfo info = wifiManager.getConnectionInfo();
        ssid = info.getSSID();
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        signalStrength = WifiManager.calculateSignalLevel(info.getRssi(), 100);
        Log.d("WiFiInfo", "SSID: " + ssid + ", Siła sygnału: " + signalStrength + "%");
    }

    public String getSsid() {
        return ssid;
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public JSONArray scanResults() {
//        if (ActivityCompat.checkSelfPermission(ApplicationContextProvider.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            @SuppressLint("MissingPermission") List<ScanResult> scanResults = wifiManager.getScanResults();
            JSONArray wifiArray = new JSONArray();

            for (ScanResult result : scanResults) {
                JSONObject wifiObject = new JSONObject();
                try {
                    wifiObject.put("SSID", result.SSID);
                    wifiObject.put("SignalStrength", result.level);
                    wifiObject.put("BSSID", result.BSSID);
                    wifiObject.put("Capabilities", result.capabilities);

                    wifiArray.put(wifiObject);
                } catch (JSONException ignored) {}
            }
            return wifiArray;
//        };
//        return null;
    }
}
