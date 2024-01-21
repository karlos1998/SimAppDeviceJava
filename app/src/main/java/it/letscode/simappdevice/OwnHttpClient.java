package it.letscode.simappdevice;


import android.os.Handler;
import android.os.Looper;
import android.telecom.Call;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.security.auth.callback.Callback;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OwnHttpClient {

    private final OkHttpClient client = new OkHttpClient();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static final String TAG = "Own Http Client";

    public interface HttpResponseCallback {
        void onResponse(String responseBody, int responseCode);
        void onFailure(Throwable throwable);
    }


    private void makeRequest(String method, String url, String json, HttpResponseCallback callback) {
        Log.d(TAG, "Request URK: " + url);
        Log.d(TAG, "Request JSON: " + json);
        Log.d(TAG, "Request Bearer: " + Device.getAuthToken());
        Runnable task = () -> {
            RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
            Request.Builder builder = new Request.Builder()
                    .url(url)
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "Bearer " + Device.getAuthToken());

            switch (method) {
                case "POST":
                    builder.post(body);
                    break;
                case "PUT":
                    builder.put(body);
                    break;
                case "PATCH":
                    builder.patch(body);
                    break;
            }

            Request request = builder.build();
            int attempts = 0;
            while (attempts < 3) {
                try (Response response = client.newCall(request).execute()) {
                    final String responseBody = response.body().string();
                    final int responseCode = response.code();
                    Log.d(TAG, "Response: (" + url + ") [" + responseCode + "]: " + responseBody);
                    mainHandler.post(() -> callback.onResponse(responseBody, responseCode));
                    return;
                } catch (IOException e) {
                    attempts++;
                    if (attempts >= 3) {
                        mainHandler.post(() -> callback.onFailure(e));
                    } else {
                        try {
                            Thread.sleep(10 * 1000);
                            System.out.println("HTTP Request try again: " + url);
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }
        };

        executor.execute(task);
    }

    public void post(String url, String json, HttpResponseCallback callback) {
        makeRequest("POST", url, json, callback);
    }

    public void put(String url, String json, HttpResponseCallback callback) {
        makeRequest("PUT", url, json, callback);
    }

    public void patch(String url, String json, HttpResponseCallback callback) {
        makeRequest("PATCH", url, json, callback);
    }
}