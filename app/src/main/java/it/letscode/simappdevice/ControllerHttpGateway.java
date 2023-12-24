package it.letscode.simappdevice;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class ControllerHttpGateway {
//    final String hostUrl = "https://panel-dev.simply-connect.ovh";

    private static final String TAG = "HTTP Controller Gateway";

    OwnHttpClient httpClient;

    MyPreferences myPreferences;
    public ControllerHttpGateway() {
        httpClient = new OwnHttpClient();
        this.myPreferences = new MyPreferences();
    }

    public void login(String token) {

        SocketClient socketClient = new SocketClient();

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
                        httpClient.setAuthToken( obj.getString("authToken") );
                        socketClient.connectToPusher( obj.getString("authToken") );
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
}
