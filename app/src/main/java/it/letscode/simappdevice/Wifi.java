package it.letscode.simappdevice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.sentry.Sentry;

public class Wifi {

    private String ssid;
    private int signalPercentage;
    private int signalStrength;

    private int ipAddress;
    WifiManager wifiManager = (WifiManager) ApplicationContextProvider.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

    public void getCurrentNetwork() {
        WifiInfo info = wifiManager.getConnectionInfo();
        ssid = info.getSSID();
        signalStrength = info.getRssi();
        ipAddress = info.getIpAddress();
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        signalPercentage = WifiManager.calculateSignalLevel(info.getRssi(), 100);
        Log.d("WiFiInfo", "SSID: " + ssid + ", Siła sygnału: " + signalPercentage + "%");
    }

    public String getSsid() {
        return ssid;
    }

    public int getSignalPercentage() {
        return signalPercentage;
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public String formatIpAddress(int ipAddress) {
        return (ipAddress & 0xFF) + "." +
                ((ipAddress >> 8) & 0xFF) + "." +
                ((ipAddress >> 16) & 0xFF) + "." +
                (ipAddress >> 24 & 0xFF);
    }

    public String getFormattedIpAddress() {
        return formatIpAddress(ipAddress);
    }

    public JSONObject getCurrentNetworkData() {
        return new JSONObject() {{
            try {
                put("name", getSsid());
                put("signalPercentage", getSignalPercentage());
                put("signalStrength", getSignalStrength());
                put("ipAddress", getFormattedIpAddress());
            } catch (JSONException e) {
                Sentry.captureException(e);
            }
        }};
    }

    public List<ScanResult> scanResultsFromManager() {
        @SuppressLint("MissingPermission") List<ScanResult> scanResults = wifiManager.getScanResults();
        return scanResults;
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
                    wifiObject.put("ssid", result.SSID);
                    wifiObject.put("signalStrength", result.level);
                    wifiObject.put("bssid", result.BSSID);
                    wifiObject.put("capabilities", result.capabilities);
                    wifiObject.put("signalPercentage", WifiManager.calculateSignalLevel(result.level, 100));

                    wifiArray.put(wifiObject);
                } catch (JSONException e) {
                    Sentry.captureException(e);
                }
            }
            return wifiArray;
//        };
//        return null;
    }

    public void changeNetwork(String networkSSID, String networkPass) {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", networkSSID);
        wifiConfig.preSharedKey = String.format("\"%s\"", networkPass);

        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
    }
}
