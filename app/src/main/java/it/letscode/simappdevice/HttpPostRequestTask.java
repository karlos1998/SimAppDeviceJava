package it.letscode.simappdevice;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpPostRequestTask {

    private Timer timer;
    private final long INTERVAL = 10000; // 10 sekund

    public void startSending() {
        timer = new Timer();
        TimerTask sendPostTask = new TimerTask() {
            @Override
            public void run() {
                // Uruchomienie na innym wątku
                sendPostRequest();
            }
        };

        timer.scheduleAtFixedRate(sendPostTask, 0, INTERVAL);
    }

    private void sendPostRequest() {
        HttpURLConnection connection = null;

        System.out.println("Dupa - sendPostRequest");

        OkHttpClient client = new OkHttpClient();

        String json = "{\"name\": \"Upol\", \"job\": \"Developer\"}";
        RequestBody body = RequestBody.create(
                json, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url("https://webhook.site/ec512eb6-f92f-4116-a050-283f98bcb26c")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stopSending() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
