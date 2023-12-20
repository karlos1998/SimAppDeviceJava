package it.letscode.simappdevice;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class ControllerHttpGateway {
//    final String hostUrl = "https://panel-dev.simply-connect.ovh";
    final String hostUrl = "http://192.168.98.113";

    private static final String TAG = "HTTP Controller Gateway";

    OwnHttpClient httpClient;
    public ControllerHttpGateway() {
        httpClient = new OwnHttpClient();
    }

    public void login(String token) throws JSONException {

        SocketClient socketClient = new SocketClient();

        socketClient.previousStop();

        JSONObject json = new JSONObject();
        json.put("token", token);
        httpClient.post(hostUrl + "/device-api/login", json.toString(), new OwnHttpClient.HttpResponseCallback() {
            @Override
            public void onResponse(String responseBody, int responseCode) throws JSONException {
                if(responseCode == 200) {
                    Log.d(TAG, "Udało się zalogowac");
                    System.out.println(responseBody);

                    JSONObject obj = new JSONObject(responseBody);

                    httpClient.setAuthToken( obj.getString("authToken") );

                    socketClient.connectToPusher( obj.getString("authToken") );

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
