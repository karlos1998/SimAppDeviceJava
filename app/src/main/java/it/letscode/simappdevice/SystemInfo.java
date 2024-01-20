package it.letscode.simappdevice;

import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class SystemInfo {

    public JSONObject getJsonDetails() {
        JSONObject deviceInfo = new JSONObject();
        try {
            deviceInfo.put("manufacturer", getManufacturer());
            deviceInfo.put("model", getModel());
            deviceInfo.put("device", getDevice());
            deviceInfo.put("product", getProduct());
            deviceInfo.put("brand", getBrand());
            deviceInfo.put("androidVersion", getAndroidVersion());
            deviceInfo.put("buildId", getBuildId());
            deviceInfo.put("sdkVersion", getSdkVersion());
            
            Log.d("DeviceInfo", deviceInfo.toString(4));
        } catch (JSONException ignore) {
            Log.d("DeviceInfo", "Json Error!");
        }

        return deviceInfo;
    }

    private String getManufacturer() {
        return Build.MANUFACTURER;
    }

    private String getModel() {
        return Build.MODEL;
    }

    private String getDevice() {
        return Build.DEVICE;
    }

    private String getProduct() {
        return Build.PRODUCT;
    }

    private String getBrand() {
        return Build.BRAND;
    }

    private String getAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    private String getBuildId() {
        return Build.ID;
    }

    private int getSdkVersion() {
        return Build.VERSION.SDK_INT;
    }

}
