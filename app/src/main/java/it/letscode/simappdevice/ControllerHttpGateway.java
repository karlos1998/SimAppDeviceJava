package it.letscode.simappdevice;

import static it.letscode.simappdevice.MemoryInfoHelper.getMemoryInfoAsJson;
import static it.letscode.simappdevice.MessagesQueue.checkMessagesQueueCrontabStart;
import static it.letscode.simappdevice.VersionManager.getVersionJson;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.function.Function;

import io.sentry.Sentry;


public class ControllerHttpGateway {
//    final String hostUrl = "https://panel-dev.simply-connect.ovh";

    private static final String TAG = "HTTP Controller Gateway";
    private final SocketClient socketClient = new SocketClient();
    OwnHttpClient httpClient;

    private final SystemInfo systemInfo = new SystemInfo();

    private final BatteryInfo batteryInfo = new BatteryInfo();

    private long waitingToPingResponseToTime = 0;

    private final Permissions permissions = new Permissions();

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
            json.put("version", getVersionJson());
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

                    PingServer.isLoggedIn = true;
                    ViewManager.changeHttpConnectionStatus(true);

                    MessagesQueue.check();

                    checkMessagesQueueCrontabStart();
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
            json.put("version", getVersionJson());
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

                    PingServer.isLoggedIn = true;
                    ViewManager.changeHttpConnectionStatus(true);

                    //tutaj bez MessagesQueue.check() bo przeciez po sparowaniu urzadzenia nie beda czekac na nie sms do wyslania

                    checkMessagesQueueCrontabStart();
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

        if(System.currentTimeMillis() < waitingToPingResponseToTime) {
            Log.d("Ping", "Nie wyslano pingu - inny jest juz w kolejce");
            return;
        }

        waitingToPingResponseToTime = System.currentTimeMillis() + 60000;

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

            json.put("permissions", new JSONObject(permissions.getAllPermissions()));

            if(LocationManager.getCurrentLocation() != null) {
                json.put("location", LocationManager.getCurrentLocation().toJSON());
            }

            Log.d("Memory", getMemoryInfoAsJson().toString());

            json.put("memory", getMemoryInfoAsJson());

        } catch (JSONException e) {
            Sentry.captureException(e);
        }

        httpClient.post(myPreferences.getHostUrl() + "/device-api/ping", json.toString(), new OwnHttpClient.HttpResponseCallback() {
            @Override
            public void onResponse(JSONObject data, int responseCode) {
                waitingToPingResponseToTime = 0;
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
                waitingToPingResponseToTime = 0;
                Log.d(TAG, "Ping send error: ");
                System.out.println(throwable.getMessage());
            }

            @Override
            public void onError(JSONObject data, int responseCode) {
                waitingToPingResponseToTime = 0;
            }
        });
    }

    public void sendMessageCallback(int messageId, String responseCode, boolean sentSuccess) {

        JSONObject json = new JSONObject();
        try {
            json.put("responseCode", responseCode);
            json.put("sentSuccess", sentSuccess);
        } catch (JSONException e) {
            Sentry.captureException(e);
        }
        httpClient.patch(myPreferences.getHostUrl() + "/device-api/single-messages/" + messageId + "/update-response-code", json.toString(), new OwnHttpClient.HttpResponseCallback() {
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

    /**
     * Android >= 10 problem ;x
     */
    public void markMessageAsUnconfirmed(int messageId) {

        JSONObject json = new JSONObject();
        httpClient.patch(myPreferences.getHostUrl() + "/device-api/single-messages/" + messageId + "/mark-as-unconfirmed", json.toString(), new OwnHttpClient.HttpResponseCallback() {
            @Override
            public void onResponse(JSONObject data, int responseCode) {

            }

            @Override
            public void onFailure(Throwable throwable) {

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
        httpClient.patch(myPreferences.getHostUrl() + "/device-api/single-messages/" + messageId + "/order-received", json.toString(), new OwnHttpClient.HttpResponseCallback() {
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

    public void getSingleMessages() {

        httpClient.get(myPreferences.getHostUrl() + "/device-api/single-messages", new OwnHttpClient.HttpResponseCallback() {
            @Override
            public void onResponse(JSONObject data, int responseCode) {
                Log.d(TAG, "Pobrano liste wiadmoosci oczekujacych na nadanie");
                try {
                    MessagesQueue.addMessagesToQueue(data.getJSONArray("data"), data.getJSONObject("meta"));
                } catch (JSONException e) {
                    Log.d(TAG, "Nie udało się pobrac wiadomosci oczekujacych na nadanie - exception");
                    System.out.println(e);
                    Sentry.captureException(e);
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.d(TAG, "Nie udało się pobrac wiadomosci oczekujacych na nadanie");
                System.out.println(throwable.getMessage());
            }

            @Override
            public void onError(JSONObject data, int responseCode) {

            }
        });
    }

    public void sendIncomingCall(String phoneNumber, String status) {
        this.sendIncomingCall(phoneNumber, status, new ControllerHttpGateway.ResponseCallback() {
            @Override
            public void onResponse(JSONObject data, int responseCode) {}
            @Override
            public void onFailure(Throwable throwable) {}
        });
    }

    public void sendIncomingCall(String phoneNumber, String status, ResponseCallback responseCallback) {

        JSONObject json = new JSONObject();
        try {
            json.put("phone_number", phoneNumber);
            json.put("status", status);
        } catch (JSONException e) {
            Sentry.captureException(e);
        }
        httpClient.put(myPreferences.getHostUrl() + "/device-api/incoming-calls", json.toString(), new OwnHttpClient.HttpResponseCallback() {
            @Override
            public void onResponse(JSONObject data, int responseCode) {
                responseCallback.onResponse(data, responseCode);
            }

            @Override
            public void onFailure(Throwable throwable) {

            }

            @Override
            public void onError(JSONObject data, int responseCode) {

            }
        });
    }
}
