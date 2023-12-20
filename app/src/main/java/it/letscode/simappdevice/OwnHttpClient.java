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

    private static String authToken = "";
    private static final String TAG = "Own Http Client";

    public interface HttpResponseCallback {
        void onResponse(String responseBody, int responseCode) throws JSONException;
        void onFailure(Throwable throwable);
    }

    public void setAuthToken(String token) {
        Log.d(TAG, "setAuthToken: " + token);
        authToken = token;
    }

    public void post(String url, String json, HttpResponseCallback callback) {

        Log.d(TAG, "Request JSON: " + json);
        Log.d(TAG, "Request Bearer: " + authToken);
        Runnable task = () -> {
            RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "Bearer " + authToken)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                final String responseBody = response.body().string();
                final int responseCode = response.code();
                mainHandler.post(() -> {
                    try {
                        callback.onResponse(responseBody, responseCode);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (IOException e) {
                mainHandler.post(() -> {
                    callback.onFailure(e);
                });
            }
        };

        executor.execute(task);
    }
}