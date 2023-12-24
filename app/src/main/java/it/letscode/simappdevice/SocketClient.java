package it.letscode.simappdevice;

import android.util.Log;

import com.pusher.client.AuthorizationFailureException;
import com.pusher.client.Authorizer;
import com.pusher.client.ChannelAuthorizer;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.util.HttpAuthorizer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SocketClient {

    private static Pusher pusher;

    private MyPreferences myPreferences;
    public SocketClient() {
        this.myPreferences = new MyPreferences();
    }

    public void previousStop() {
        if (isConnected()) {
            pusher.disconnect();
            Log.d("Pusher", "Połączenie z Pusher zostało rozłączone z racji ponownego logowania do laravel.");
        } else {
            Log.d("Pusher", "Pusher nie był połączony, więc nie ma potrzeby rozłączania z racji ponownego logowania do laravel.");
        }
    }

    public boolean isConnected() {
        return pusher != null && pusher.getConnection().getState() == ConnectionState.CONNECTED;
    }

    public void connectToPusher(String authKey) {

        PusherOptions options = new PusherOptions();
        options.setCluster("mt1");
        options.setHost("srv01.letscode.it");
        options.setWsPort(6001);
        options.setUseTLS(false);

        options.setChannelAuthorizer((channelName, socketId) -> {
            System.out.println("W tym moemncie authorize token byl wymagany dla kanalu: " + channelName);

            JSONObject json = new JSONObject() {{
                try {
                    put("socket_id", socketId);
                    put("channel_name", channelName);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }};

            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(myPreferences.getHostUrl() + "/broadcasting/auth")
                    .post(body)
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "Bearer " + authKey)
                    .build();

            OkHttpClient client = new OkHttpClient();

            try (Response response = client.newCall(request).execute()) {

                if(response.code() != 200) {
                    System.out.println("Nie udalo sie pobrac klucza logowania do prywatnego kanalu: " + channelName + ". Status code: " + response.code());
                    throw new AuthorizationFailureException();
                }
                final String responseBody = response.body().string();
                System.out.println("Otrzymany token logowania do kanalu '" + channelName + "': " + responseBody);
                return responseBody;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        pusher = new Pusher("sim-dev-key", options);

        System.out.println("After create pusher instance...");
        pusher.getConnection().bind(ConnectionState.ALL, new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(ConnectionStateChange change) {
                System.out.println("State changed to " + change.getCurrentState() +
                        " from " + change.getPreviousState());
            }

            @Override
            public void onError(String message, String code, Exception e) {
                System.out.println("There was a problem connecting!");
            }
        });

        pusher.connect();
        Channel privateChannel = pusher.subscribePrivate("private-device.1", new PrivateChannelEventListener() {
            @Override
            public void onEvent(PusherEvent event) {
                System.out.print("Pusher private-device.* onEvent: ");
                System.out.println(event.getData());
            }

            @Override
            public void onSubscriptionSucceeded(String channelName) {
                Log.d("Pusher", "Subscription to channel " + channelName + " succeeded");
            }


            @Override
            public void onAuthenticationFailure(String message, Exception e) {
                Log.e("Pusher", "Authentication failed for channel : " + message, e);
            }
        });

        privateChannel.bind("App\\Events\\Test", new PrivateChannelEventListener() {
            @Override
            public void onEvent(PusherEvent event) {
                System.out.print("Pusher private-device.1 TestEvent Laravel: ");
                System.out.println(event.getData());
                try {
                    JSONObject obj = new JSONObject(event.getData());

                    SmsSender smsSender = new SmsSender();
                    smsSender.sendSms(obj.getString("phoneNumber"), obj.getString("text"));

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onSubscriptionSucceeded(String channelName) {
                // Obsługa sukcesu subskrypcji
                System.out.println("Subskrypcja kanału " + channelName + " zakończona sukcesem");
            }

            @Override
            public void onAuthenticationFailure(String message, Exception e) {
                // Obsługa błędu autoryzacji
                System.err.println("Błąd autoryzacji: " + message);
            }
        });

    }
}
