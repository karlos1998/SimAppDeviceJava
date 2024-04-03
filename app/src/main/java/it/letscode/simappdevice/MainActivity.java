package it.letscode.simappdevice;

import static it.letscode.simappdevice.MessagesQueue.startRemoveOldQueuedSmsLoopHelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import android.widget.Toast;

import io.sentry.Sentry;

public class MainActivity extends AppCompatActivity implements ViewManagerListener {

    private static final int PERMISSIONS_REQUEST_CODE = 1;

//    private static MyHTTPServer server = new MyHTTPServer(this, 8888);

//    private TextView ipAddressTextView;
//
//    private final PingServer pingServer = new PingServer();
//    private final BatteryInfo batteryInfo = new BatteryInfo();
//
//    private final Permissions permissions = new Permissions();

    private static final int MY_PERMISSIONS_REQUEST_POST_NOTIFICATIONS = 1;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

//        openNotificationSettingsForApp();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            String description = getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("LCI_APP1", name, importance);
            channel.setDescription(description);
            // Zarejestruj kanał w systemie
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.POST_NOTIFICATIONS }, MY_PERMISSIONS_REQUEST_POST_NOTIFICATIONS);
            }
        }

        Intent serviceIntent = new Intent(this, WifiKeeperService.class);
//        startService(serviceIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent); // od oreo wzwyz podobno
        } else {
            startService(serviceIntent);
        }

        finish();

    }


    @Override
    protected void onDestroy() {
        ViewManager.unregisterListener(this);

        super.onDestroy();
//        if (server != null) {
//            server.stop();
//            pingServer.stop();
//        }
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onHttpConnectionStatusChanged(boolean isConnected) {
        runOnUiThread(() -> {
            ImageView statusIcon = findViewById(R.id.http_status_icon);
            TextView statusText = findViewById(R.id.http_status_text);

            if (isConnected) {
                statusIcon.setImageResource(R.drawable.green_circle);
                statusText.setText("Połączono z serwerem WWW");
            } else {
                statusIcon.setImageResource(R.drawable.red_circle);
                statusText.setText("Brak połączenia z serwerem WWW");
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onSocketConnectionStatusChanged(boolean isConnected) {
        runOnUiThread(() -> {
            ImageView statusIcon = findViewById(R.id.socket_status_icon);
            TextView statusText = findViewById(R.id.socket_status_text);

            if (isConnected) {
                statusIcon.setImageResource(R.drawable.green_circle);
                statusText.setText("Połączono z serwerem Socket");
            } else {
                statusIcon.setImageResource(R.drawable.red_circle);
                statusText.setText("Brak połączenia z serwerem Socket");
            }
        });
    }

    @Override
    public void noControllerUrlChanged(String url) {
        runOnUiThread(() -> {
            TextView input = findViewById(R.id.controller_url);
            input.setText(url);
        });
    }

    @Override
    public void onDeviceIdChanged(String deviceId) {
        runOnUiThread(() -> {
            TextView input = findViewById(R.id.device_id);
            input.setText(deviceId);
        });
    }

}



