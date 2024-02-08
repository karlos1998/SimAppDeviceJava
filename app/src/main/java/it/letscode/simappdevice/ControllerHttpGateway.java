package it.letscode.simappdevice;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import io.sentry.Sentry;


public class ControllerHttpGateway {
//    final String hostUrl = "https://panel-dev.simply-connect.ovh";

    private static final String TAG = "HTTP Controller Gateway";
    private final SocketClient socketClient = new SocketClient();
    OwnHttpClient httpClient;

    private final SystemInfo systemInfo = new SystemInfo();

    private final BatteryInfo batteryInfo = new BatteryInfo();


    MyPreferences myPreferences;
    public ControllerHttpGateway() {
        httpClient = new OwnHttpClient();
        this.myPreferences = new MyPreferences();
    }

    public interface ResponseCallback {
        void onResponse(JSONObject data, int responseCode);
        void onFailure(Throwable throwable);
    }

    public void login(String token) {

        PingServer.resetNotLoggedCount();

        socketClient.previousStop();

        JSONObject json = new JSONObject();
        try {
            json.put("token", token);
            json.put("systemInfo", systemInfo.getJsonDetails());
        } catch (JSONException e) {
            Sentry.captureException(e);
            return;
        }

        httpClient.post(myPreferences.getHostUrl() + "/device-api/login", json.toString(), new OwnHttpClient.HttpResponseCallback() {
            @Override
            public void onResponse(JSONObject data, int responseCode) {
                Log.d(TAG, "Udało się zalogowac");

                try {

                    Device.setFromLoginResponse(data);

                    socketClient.setConfig(data.getJSONObject("socketConfig"));
                    socketClient.connectToPusher();
                } catch (JSONException e) {
                    Log.d(TAG, "Nie udało się zalogowac (1)");
                    Sentry.captureException(e);
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.d(TAG, "Nie udało się zalogowac - HTTP FAIL");
                System.out.println(throwable.getMessage());
            }

            @Override
            public void onError(JSONObject data, int responseCode) {
                Device.clear();
            }
        });
    }


    public void pair(String token) {

        PingServer.resetNotLoggedCount();

        socketClient.previousStop();

        JSONObject json = new JSONObject();
        try {
            json.put("token", token);
            json.put("systemInfo", systemInfo.getJsonDetails());
        } catch (JSONException e) {
            Sentry.captureException(e);
        }
        httpClient.post(myPreferences.getHostUrl() + "/device-api/pair", json.toString(), new OwnHttpClient.HttpResponseCallback() {
            @Override
            public void onResponse(JSONObject data, int responseCode) {
                Log.d(TAG, "Udało się sparować urządzenie");

                try {
                    Device.setFromLoginResponse(data);

                    myPreferences.setLoginToken(data.getString("loginToken"));

                    socketClient.setConfig(data.getJSONObject("socketConfig"));
                    socketClient.connectToPusher();
                } catch (JSONException e) {
                    Log.d(TAG, "Nie udało się sparować urządzenia (1)");
                    Sentry.captureException(e);
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.d(TAG, "Nie udało się zalogowac - HTTP FAIL");
                System.out.println(throwable.getMessage());
            }

            @Override
            public void onError(JSONObject data, int responseCode) {

            }
        });
    }


//    public void sendSignalStrength(int signalStrength) {
//
//        JSONObject json = new JSONObject();
//        try {
//            json.put("signalStrength", signalStrength);
//        } catch (JSONException ignored) {
//
//        }
//        httpClient.put(myPreferences.getHostUrl() + "/device-api/signal-strength", json.toString(), new OwnHttpClient.HttpResponseCallback() {
//            @Override
//            public void onResponse(String responseBody, int responseCode) {
//
//            }
//
//            @Override
//            public void onFailure(Throwable throwable) {
//                Log.d(TAG, "Signal strength send error: ");
//                System.out.println(throwable.getMessage());
//            }
//        });
//    }

    public void ping() {

        Wifi wifi = new Wifi();
        wifi.getCurrentNetwork();

        JSONObject json = new JSONObject();
        try {
            json.put("ping", true);
            json.put("loginToken", myPreferences.getLoginToken());
            json.put("socketConnected", socketClient.isConnected());

            json.put("currentNetwork", wifi.getCurrentNetworkData());
            json.put("nearbyNetworks", wifi.scanResults());

            json.put("batteryInfo", batteryInfo.getBatteryJsonData());

            json.put("deviceUuid", myPreferences.getDeviceUuid());

            json.put("gsmSignalStrength", NetworkSignalStrengthChecker.getSignalStrength());

            json.put("isScreenOn", DeviceScreenStatus.isScreenOn());
        } catch (JSONException e) {
            Sentry.captureException(e);
        }

        httpClient.post(myPreferences.getHostUrl() + "/device-api/ping", json.toString(), new OwnHttpClient.HttpResponseCallback() {
            @Override
            public void onResponse(JSONObject data, int responseCode) {

                try {
                    PingServer.deviceId = data.getString("deviceId");
                    PingServer.receiveLoginStatus(data.getBoolean("isLoggedIn"));
                } catch (JSONException e) {
                    Log.d(TAG, "Nie udalo sie odczytac json z pingu");
                    Sentry.captureException(e);
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.d(TAG, "Ping send error: ");
                System.out.println(throwable.getMessage());
            }

            @Override
            public void onError(JSONObject data, int responseCode) {

            }
        });
    }

    public void sendMessageCallback(int messageId, String responseCode) {

        JSONObject json = new JSONObject();
        try {
            json.put("responseCode", responseCode);
        } catch (JSONException e) {
            Sentry.captureException(e);
        }
        httpClient.patch(myPreferences.getHostUrl() + "/device-api/messages/" + messageId + "/update-response-code", json.toString(), new OwnHttpClient.HttpResponseCallback() {
            @Override
            public void onResponse(JSONObject data, int responseCode) {
                
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.d(TAG, "Send Message Callback send error: ");
                System.out.println(throwable.getMessage());
            }

            @Override
            public void onError(JSONObject data, int responseCode) {

            }
        });
    }

    public void saveReceivedMessage(String phoneNumber, String text, long timestamp, ResponseCallback responseCallback) {

        JSONObject json = new JSONObject();
        try {
            json.put("phoneNumber", phoneNumber);
            json.put("text", text);
            json.put("timestamp", timestamp);
        } catch (JSONException e) {
            Sentry.captureException(e);
        }
        httpClient.post(myPreferences.getHostUrl() + "/device-api/messages", json.toString(), new OwnHttpClient.HttpResponseCallback() {
            @Override
            public void onResponse(JSONObject data, int responseCode) {
                responseCallback.onResponse(data, responseCode);
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.d(TAG, "Save Received Message Callback send error: ");
                System.out.println(throwable.getMessage());
                responseCallback.onFailure(throwable);
            }

            @Override
            public void onError(JSONObject data, int responseCode) {

            }
        });
    }

    public void saveReceivedMessage(String phoneNumber, String text, long timestamp) {
        saveReceivedMessage(phoneNumber, text, timestamp, new ResponseCallback() {
            @Override
            public void onResponse(JSONObject data, int responseCode) {}
            @Override
            public void onFailure(Throwable throwable) {}
        });
    }

    public void markMessageAsOrderReceived(Integer messageId, boolean isAccepted, String errorMessage) {

        if(messageId < 0) return;

        JSONObject json = new JSONObject();
        try {
            json.put("isAccepted", isAccepted);
            json.put("errorMessage", errorMessage);
        } catch (JSONException e) {
            Sentry.captureException(e);
        }
        httpClient.patch(myPreferences.getHostUrl() + "/device-api/messages/" + messageId + "/order-received", json.toString(), new OwnHttpClient.HttpResponseCallback() {
            @Override
            public void onResponse(JSONObject data, int responseCode) {
                
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.d(TAG, "Save Received Message Callback send error: ");
                System.out.println(throwable.getMessage());
            }

            @Override
            public void onError(JSONObject data, int responseCode) {

            }
        });
    }

    public void sendAttachmentToMessage(String messageId, String messageAttachmentUuid, String contentType, String value, Boolean lastPart, ResponseCallback responseCallback) {

        JSONObject json = new JSONObject();
        try {
            json.put("content_type", contentType);
            json.put("value", value);
            json.put("uuid", messageAttachmentUuid);
            json.put("ready", lastPart);
        } catch (JSONException e) {
            Sentry.captureException(e);
        }
        httpClient.put(myPreferences.getHostUrl() + "/device-api/messages/" + messageId + "/attachments", json.toString(), new OwnHttpClient.HttpResponseCallback() {
            @Override
            public void onResponse(JSONObject data, int responseCode) {

                responseCallback.onResponse(data, responseCode);
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.d(TAG, "Save Received Message Callback send error: ");
                System.out.println(throwable.getMessage());
            }

            @Override
            public void onError(JSONObject data, int responseCode) {

            }
        });
    }
}
