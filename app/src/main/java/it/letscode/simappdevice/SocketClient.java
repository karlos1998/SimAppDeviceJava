package it.letscode.simappdevice;

import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

import org.json.JSONException;
import org.json.JSONObject;

public class SocketClient {

    private Pusher pusher;
    private Channel channel;

    public void connectToPusher() {
        PusherOptions options = new PusherOptions();
        options.setCluster("YOUR_PUSHER_CLUSTER");
        options.setHost("192.168.98.113");
        options.setWsPort(6001);
        options.setUseTLS(false);

        pusher = new Pusher("app-key", options);

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

        channel = pusher.subscribe("channel-name");

//        channel.bind("App\\Events\\Test", (data) -> {
//            System.out.println("Received event with data: " + data);
//        });

        channel.bind("App\\Events\\Test", new SubscriptionEventListener() {
            @Override
            public void onEvent(PusherEvent event) {
                System.out.print("Pusher channel-name TestEvent Laravel: ");
                System.out.println(event.getData());
                try {
                    JSONObject obj = new JSONObject(event.getData());
                    System.out.println(obj.getString("text"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
