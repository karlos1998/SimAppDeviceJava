package it.letscode.simappdevice;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;


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
        } catch (JSONException ignored) {}

        httpClient.post(myPreferences.getHostUrl() + "/device-api/login", json.toString(), new OwnHttpClient.HttpResponseCallback() {
            @Override
            public void onResponse(String responseBody, int responseCode) {
                if(responseCode == 200) {
                    Log.d(TAG, "Udało się zalogowac");
                    System.out.println(responseBody);


                    try {
                        JSONObject obj = new JSONObject(responseBody);

                        Device.setFromLoginResponse(obj);

                        socketClient.connectToPusher();
                    } catch (JSONException ignored) {
                        Log.d(TAG, "Nie udało się zalogowac (1)");
                    }

                } else {
                    Log.d(TAG, "Nie udało się zalogowac");
                    Device.clear();
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.d(TAG, "Nie udało się zalogowac - HTTP FAIL");
                System.out.println(throwable.getMessage());
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
        } catch (JSONException ignored) {

        }
        httpClient.post(myPreferences.getHostUrl() + "/device-api/pair", json.toString(), new OwnHttpClient.HttpResponseCallback() {
            @Override
            public void onResponse(String responseBody, int responseCode) {
                if(responseCode == 200) {
                    Log.d(TAG, "Udało się sparować urządzenie");
                    System.out.println(responseBody);

                    try {
                        JSONObject obj = new JSONObject(responseBody);
                        Device.setFromLoginResponse(obj);

                        myPreferences.setLoginToken(obj.getString("loginToken"));

                        socketClient.connectToPusher();
                    } catch (JSONException ignored) {
                        Log.d(TAG, "Nie udało się sparować urządzenia (1)");
                    }

                } else {
                    Log.d(TAG, "Nie udało się sparować urządzenia");
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.d(TAG, "Nie udało się zalogowac - HTTP FAIL");
                System.out.println(throwable.getMessage());
            }
        });
    }


    public void sendSignalStrength(int signalStrength) {

        JSONObject json = new JSONObject();
        try {
            json.put("signalStrength", signalStrength);
        } catch (JSONException ignored) {

        }
        httpClient.put(myPreferences.getHostUrl() + "/device-api/signal-strength", json.toString(), new OwnHttpClient.HttpResponseCallback() {
            @Override
            public void onResponse(String responseBody, int responseCode) {
                System.out.println(responseBody);
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.d(TAG, "Signal strength send error: ");
                System.out.println(throwable.getMessage());
            }
        });
    }

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
        } catch (JSONException ignored) {

        }
        httpClient.post(myPreferences.getHostUrl() + "/device-api/ping", json.toString(), new OwnHttpClient.HttpResponseCallback() {
            @Override
            public void onResponse(String responseBody, int responseCode) {
                System.out.println(responseBody);

                try {
                    JSONObject obj = new JSONObject(responseBody);

                    PingServer.deviceId = obj.getString("deviceId");
                    PingServer.receiveLoginStatus(obj.getBoolean("isLoggedIn"));

                } catch (JSONException ignored) {
                    Log.d(TAG, "Nie udalo sie odczytac json z pingu");
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.d(TAG, "Ping send error: ");
                System.out.println(throwable.getMessage());
            }
        });
    }

    public void sendMessageCallback(int messageId, String responseCode) {

        JSONObject json = new JSONObject();
        try {
            json.put("responseCode", responseCode);
        } catch (JSONException ignored) {

        }
        httpClient.patch(myPreferences.getHostUrl() + "/device-api/messages/" + messageId + "/update-response-code", json.toString(), new OwnHttpClient.HttpResponseCallback() {
            @Override
            public void onResponse(String responseBody, int responseCode) {
                System.out.println(responseBody);
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.d(TAG, "Send Message Callback send error: ");
                System.out.println(throwable.getMessage());
            }
        });
    }

    public void saveReceivedMessage(String phoneNumber, String text, long timestamp, ResponseCallback responseCallback) {

        JSONObject json = new JSONObject();
        try {
            json.put("phoneNumber", phoneNumber);
            json.put("text", text);
            json.put("timestamp", timestamp);
        } catch (JSONException ignored) {

        }
        httpClient.post(myPreferences.getHostUrl() + "/device-api/messages", json.toString(), new OwnHttpClient.HttpResponseCallback() {
            @Override
            public void onResponse(String responseBody, int responseCode) {
                System.out.println(responseBody);
                try {
                    JSONObject obj = new JSONObject(responseBody);
                    responseCallback.onResponse(obj, responseCode);
                } catch (JSONException ignored) {
                    Log.d(TAG, "Nie udalo sie odczytac json z pingu");
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.d(TAG, "Save Received Message Callback send error: ");
                System.out.println(throwable.getMessage());
                responseCallback.onFailure(throwable);
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
        } catch (JSONException ignored) {

        }
        httpClient.patch(myPreferences.getHostUrl() + "/device-api/messages/" + messageId + "/order-received", json.toString(), new OwnHttpClient.HttpResponseCallback() {
            @Override
            public void onResponse(String responseBody, int responseCode) {
                System.out.println(responseBody);
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.d(TAG, "Save Received Message Callback send error: ");
                System.out.println(throwable.getMessage());
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
        } catch (JSONException ignored) {

        }
        httpClient.put(myPreferences.getHostUrl() + "/device-api/messages/" + messageId + "/attachments", json.toString(), new OwnHttpClient.HttpResponseCallback() {
            @Override
            public void onResponse(String responseBody, int responseCode) {
                System.out.println(responseBody);
                try {
                    responseCallback.onResponse(new JSONObject(responseBody), responseCode);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.d(TAG, "Save Received Message Callback send error: ");
                System.out.println(throwable.getMessage());
            }
        });
    }
}
