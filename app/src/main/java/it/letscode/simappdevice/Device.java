package it.letscode.simappdevice;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Device {
    private static String deviceId;

    private static String deviceName;

    private static String loginToken;

    private static String authToken;

    public static void setFromLoginResponse(JSONObject obj) {
        try {
            deviceId = obj.getString("deviceId");
            deviceName = obj.getString("deviceName");
            loginToken = obj.getString("loginToken");
            authToken = obj.getString("authToken");

            Log.d("Device", "setAuthToken: " + authToken);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getDeviceId() {
        return deviceId;
    }

    public static String getDeviceName() {
        return deviceName;
    }

    public static String getAuthToken () {
        return authToken;
    }
}
