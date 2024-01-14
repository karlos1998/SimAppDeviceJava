package it.letscode.simappdevice;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class ControllerHttpGateway {
//    final String hostUrl = "https://panel-dev.simply-connect.ovh";

    private static final String TAG = "HTTP Controller Gateway";
    private final SocketClient socketClient = new SocketClient();
    OwnHttpClient httpClient;

    MyPreferences myPreferences;
    public ControllerHttpGateway() {
        httpClient = new OwnHttpClient();
        this.myPreferences = new MyPreferences();
    }

    public void login(String token) {


        socketClient.previousStop();

        JSONObject json = new JSONObject();
        try {
            json.put("token", token);
        } catch (JSONException ignored) {

        }
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

        SocketClient socketClient = new SocketClient();

        socketClient.previousStop();

        JSONObject json = new JSONObject();
        try {
            json.put("token", token);
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

        JSONObject json = new JSONObject();
        try {
            json.put("ping", true);
            json.put("loginToken", myPreferences.getLoginToken());
            json.put("socketConnected", socketClient.isConnected());
        } catch (JSONException ignored) {

        }
        httpClient.post(myPreferences.getHostUrl() + "/device-api/ping", json.toString(), new OwnHttpClient.HttpResponseCallback() {
            @Override
            public void onResponse(String responseBody, int responseCode) {
                System.out.println(responseBody);

                try {
                    JSONObject obj = new JSONObject(responseBody);

                    PingServer.deviceId = obj.getString("deviceId");
                    PingServer.isLoggedIn = obj.getBoolean("isLoggedIn");

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

}
