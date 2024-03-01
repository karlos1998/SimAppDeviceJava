package it.letscode.simappdevice;

import android.util.Log;

import com.pusher.client.AuthorizationFailureException;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.PresenceChannelEventListener;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.channel.User;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Set;

import io.sentry.Sentry;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SocketClient {

    private static Pusher pusher;

    private MyPreferences myPreferences;

    private static String appKey;
    private static String host;
    private static int wsPort;
    private static int wssPort;
    private static String scheme;
    private static String cluster;
    private static Boolean useTLS;


    public SocketClient() {
        this.myPreferences = new MyPreferences();
    }

    private final SystemInfo systemInfo = new SystemInfo();

    public void setConfig(JSONObject socketConfig) {
        Log.d("Socket Client", socketConfig.toString());
        try {
            appKey = socketConfig.getString("appKey");
            host = socketConfig.getString("host");
            wsPort = socketConfig.getInt("wsPort");
            wssPort = socketConfig.getInt("wssPort");
            scheme = socketConfig.getString("scheme");
            cluster = socketConfig.getString("cluster");
            useTLS = socketConfig.getBoolean("useTLS");
        } catch (JSONException e) {
            Sentry.captureException(e);
        }
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

    public boolean privateChannelIsSubscribed() {
        return isConnected() && pusher.getPrivateChannel("private-device." + Device.getDeviceId()).isSubscribed();
    }

    public void connectToPusher() {

        PusherOptions options = new PusherOptions();

        options.setCluster(cluster);
        options.setHost(host);
        options.setWsPort(wsPort);
        options.setWssPort(wssPort);

        if(systemInfo.getSdkVersion() <= 23) { //coś nie chce działać socket po HTTP, ale tylko android 6
            useTLS = false;
        }
        options.setUseTLS(useTLS);

        options.setActivityTimeout(10000);
        options.setPongTimeout(30000);

        options.setChannelAuthorizer((channelName, socketId) -> {
            Log.d("Pusher", "W tym moemncie authorize token byl wymagany dla kanalu: " + channelName);
            JSONObject json = new JSONObject() {{
                try {
                    put("socket_id", socketId);
                    put("channel_name", channelName);
                } catch (JSONException e) {
                    Sentry.captureException(e);
                }
            }};

            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(myPreferences.getHostUrl() + "/device-api/broadcasting/auth")
                    .post(body)
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "Bearer " + Device.getAuthToken())
                    .build();

            OkHttpClient client = new OkHttpClient();

            try (Response response = client.newCall(request).execute()) {

                if(response.code() != 200) {
                    Log.d("Pusher", "Nie udalo sie pobrac klucza logowania do prywatnego kanalu: " + channelName + ". Status code: " + response.code());
//                    Log.d("Pusher", response.body().string());
                    throw new AuthorizationFailureException();
                }
                final String responseBody = response.body().string();
                Log.d("Pusher", "Otrzymany token logowania do kanalu '" + channelName + "': " + responseBody);
                return responseBody;
            } catch (IOException e) {
                Sentry.captureException(e);
                throw new RuntimeException(e);
            }
        });

        pusher = new Pusher(appKey, options);

        Log.d("Pusher", "After create pusher instance...");
        pusher.getConnection().bind(ConnectionState.ALL, new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(ConnectionStateChange change) {
                Log.d("Pusher", "State changed to " + change.getCurrentState() +
                        " from " + change.getPreviousState());
            }

            @Override
            public void onError(String message, String code, Exception e) {
                Log.d("Pusher", "There was a problem connecting!");
            }
        });

        pusher.connect();

        /**
         * Prywatny kanal nasluchiwania dla danego device
         */
        Channel privateChannel = pusher.subscribePrivate("private-device." + Device.getDeviceId(), new PrivateChannelEventListener() {
            @Override
            public void onEvent(PusherEvent event) {
                System.out.print("Pusher private-device.* onEvent: ");
                Log.d("Pusher", event.getData());
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

        /**
         * Testowy listener prywatnego kanalu
         */
        privateChannel.bind("App\\Events\\SendMessage", new PrivateChannelEventListener() {
            @Override
            public void onEvent(PusherEvent event) {
                System.out.print("New Event from laravel: SendMessage ");
                Log.d("Pusher", event.getData());
                try {
                    JSONObject obj = new JSONObject(event.getData());

                    SmsSender smsSender = new SmsSender();
                    smsSender.sendSms(obj.getString("phoneNumber"), obj.getString("text"), obj.getInt("messageId"));

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onSubscriptionSucceeded(String channelName) {
                // Obsługa sukcesu subskrypcji
                Log.d("Pusher", "Subskrypcja kanału " + channelName + " zakończona sukcesem");
            }

            @Override
            public void onAuthenticationFailure(String message, Exception e) {
                // Obsługa błędu autoryzacji
                System.err.println("Błąd autoryzacji: " + message);
            }
        });
        /* ************************************************* */


        /* *
         * Online device status for user
         */
        pusher.subscribePresence("presence-device-status." + Device.getDeviceId(), new PresenceChannelEventListener() {
            @Override
            public void onUsersInformationReceived(String channelName, Set<User> users) {

            }

            @Override
            public void userSubscribed(String channelName, User user) {

            }

            @Override
            public void userUnsubscribed(String channelName, User user) {

            }

            @Override
            public void onAuthenticationFailure(String message, Exception e) {

            }

            @Override
            public void onSubscriptionSucceeded(String channelName) {

            }

            @Override
            public void onEvent(PusherEvent event) {

            }
        });


        /* *
         * Łączenie się do kanlu uzytkownika i jego devices
         * Chodzi o przekazywanie informacji realtime o statusie dzialania device realtime
         * -
         * UWAGA. nazwa kanlu jest testowa.
         */
        pusher.subscribePresence("presence-online-users", new PresenceChannelEventListener() {
            @Override
            public void onUsersInformationReceived(String channelName, Set<User> users) {

            }

            @Override
            public void userSubscribed(String channelName, User user) {

            }

            @Override
            public void userUnsubscribed(String channelName, User user) {

            }

            @Override
            public void onAuthenticationFailure(String message, Exception e) {

            }

            @Override
            public void onSubscriptionSucceeded(String channelName) {

            }

            @Override
            public void onEvent(PusherEvent event) {

            }
        });


    }
}
