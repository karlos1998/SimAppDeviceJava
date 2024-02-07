package it.letscode.simappdevice;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;

import io.sentry.Sentry;

public class Device {
    private static String deviceId;

    private static String deviceName;

    private static String loginToken;

    private static String authToken;

    private static final MyPreferences myPreferences = new MyPreferences();
    private static final SocketClient socketClient = new SocketClient();

    private static final ControllerHttpGateway controllerHttpGateway = new ControllerHttpGateway();

    public static void setFromLoginResponse(JSONObject obj) {
        try {
            deviceId = obj.getString("deviceId");
            deviceName = obj.getString("deviceName");
            loginToken = obj.getString("loginToken");
            authToken = obj.getString("authToken");

            Log.d("Device", "setAuthToken: " + authToken);

        } catch (JSONException e) {
            Sentry.captureException(e);
        }
    }

    public static void clear () {

        myPreferences.forgetLoginToken();
        socketClient.previousStop();

        deviceId = null;
        deviceName = null;
        loginToken = null;
        authToken = null;

        Log.d("Device", "Logout - device data cleared.");
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

    public static void login() {
        controllerHttpGateway.login(myPreferences.getLoginToken());
    }
}
