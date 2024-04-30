package it.letscode.simappdevice;

import static it.letscode.simappdevice.ApplicationContextProvider.getApplicationContext;
import static it.letscode.simappdevice.MessagesQueue.startRemoveOldQueuedSmsLoopHelper;

import androidx.annotation.RequiresApi;
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
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import android.widget.Toast;

import io.sentry.Sentry;

//import org.apache.sshd.server.SshServer;
//import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
//import org.apache.sshd.server.shell.ProcessShellFactory;

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

    private static final Permissions permissions = new Permissions();

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);



//        SshServer sshd = SshServer.setUpDefaultServer();
//        sshd.setPort(2222); // Zmieniono port na 2222, aby uniknąć konfliktów z domyślnym portem SSH (22), który może wymagać uprawnień roota
//        sshd.setShellFactory(new ProcessShellFactory());
//// Dodaj tutaj konfigurację uwierzytelniania
//        try {
//            sshd.start();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }


        /**
         * Full screen
         */
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);


        /**
         * Permissions request
         */

        if (!permissions.hasAllPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.RECEIVE_WAP_PUSH,
                    Manifest.permission.RECEIVE_MMS,
                    Manifest.permission.RECEIVE_BOOT_COMPLETED,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.CHANGE_NETWORK_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WAKE_LOCK,
                    Manifest.permission.READ_PHONE_NUMBERS,
                    Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.READ_CALL_LOG,
                    // ... dodaj inne uprawnienia z listy
            }, PERMISSIONS_REQUEST_CODE);
        }


        /**
         * Don't hate me!
         * ! Important
         */
        ApplicationContextProvider.initialize(getApplicationContext());


        /**
         * Register app layout listeners
         */
        ViewManager.clearListeners();
        ViewManager.registerListener(this);


        /**
         * Register notification channel
         */
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


        /**
         * Android >= 13 must accept notifications
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.POST_NOTIFICATIONS }, MY_PERMISSIONS_REQUEST_POST_NOTIFICATIONS);
            }
        }

        Intent serviceIntent = new Intent(this, WifiKeeperService.class);
//        startService(serviceIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            SmsSentReceiver smsSentReceiver = new SmsSentReceiver();
            IntentFilter intentFilter = new IntentFilter("SMS_SENT");
            registerReceiver(smsSentReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);

//            IncomingCallReceiver incomingCallReceiver = new IncomingCallReceiver();
//            IntentFilter filter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
//            registerReceiver(incomingCallReceiver, filter);
        }


        /**
         * Start background service
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent); // od oreo wzwyz podobno
        } else {
            startService(serviceIntent);
        }

        TextView ipAddressTextView = findViewById(R.id.ip_address_text_view);

        Wifi wifi = new Wifi();
        String ipAddress = wifi.getOnlyIpString();
        ipAddressTextView.setText("URI: http://" + ipAddress + ":" + 8888);

//        finish();

    }


    @Override
    protected void onDestroy() {
        ViewManager.unregisterListener(this);

        //todo ? ??
//        unregisterReceiver(smsSentReceiver);

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



