package it.letscode.simappdevice;

import static it.letscode.simappdevice.MessagesQueue.startRemoveOldQueuedSmsLoopHelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PowerManager;
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
    private MyHTTPServer server;
    private TextView ipAddressTextView;

    private final PingServer pingServer = new PingServer();
    private final BatteryInfo batteryInfo = new BatteryInfo();

    private final Permissions permissions = new Permissions();

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        PackageManager packageManager = getPackageManager();


//    // waiting for view to draw to better represent a captured error with a screenshot
//    findViewById(android.R.id.content).getViewTreeObserver().addOnGlobalLayoutListener(() -> {
//      try {
//        throw new Exception("This app uses Sentry! :)");
//      } catch (Exception e) {
//        Sentry.captureException(e);
//      }
//    });

        int port = 8888;
        try {

            ViewManager.registerListener(this);

            server = new MyHTTPServer(this, port);
            server.start();
            System.out.println("Serwer działa na porcie: " + port);

            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "MyApp::MyWakeLockTag");

            // Aby zapobiec usypianiu
            wakeLock.acquire();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            ApplicationContextProvider.initialize(getApplicationContext());

            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);

            ApplicationContextProvider.setPackageInfo(packageInfo);

            super.onCreate(savedInstanceState);

            setContentView(R.layout.activity_main);

            // full screeen
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

            MyPreferences myPreferences = new MyPreferences();

            myPreferences.setContext(getApplicationContext());

            myPreferences.generateDeviceUuidIfNotExist();

            String prefsContent = myPreferences.getAllPreferences();
            Log.d("SharedPreferences", "Zawartość: " + prefsContent);



            ipAddressTextView = findViewById(R.id.ip_address_text_view);

            Wifi wifi = new Wifi();
            String ipAddress = wifi.getOnlyIpString();
            ipAddressTextView.setText("URI: http://" + ipAddress + ":" + port);




            if (!permissions.hasAllPermissionsGranted()) {
                ActivityCompat.requestPermissions(this, new String[]{
                        android.Manifest.permission.SEND_SMS,
                        android.Manifest.permission.RECEIVE_SMS,
                        android.Manifest.permission.READ_SMS,
                        android.Manifest.permission.CALL_PHONE,
                        android.Manifest.permission.RECEIVE_WAP_PUSH,
                        android.Manifest.permission.RECEIVE_MMS,
                        android.Manifest.permission.RECEIVE_BOOT_COMPLETED,
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.CHANGE_WIFI_STATE,
                        android.Manifest.permission.INTERNET,
                        android.Manifest.permission.ACCESS_NETWORK_STATE,
                        android.Manifest.permission.CHANGE_NETWORK_STATE,
                        android.Manifest.permission.ACCESS_WIFI_STATE,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.WAKE_LOCK,
                        Manifest.permission.READ_PHONE_NUMBERS,
                        // ... dodaj inne uprawnienia z listy
                }, PERMISSIONS_REQUEST_CODE);
            }


            NetworkSignalStrengthChecker networkSignalStrengthChecker = new NetworkSignalStrengthChecker(this);
            networkSignalStrengthChecker.startSignalStrengthCheck();

            Device.login();

            pingServer.start();

            startRemoveOldQueuedSmsLoopHelper();

            if(myPreferences.trustedNumberExist()) {
                SmsSender smsSender = new SmsSender();
                smsSender.sendSms(myPreferences.getTrustedNumber(), "Start Sim App Device");
            }

            batteryInfo.registerBatteryTemperatureReceiver();

        } catch (IOException | PackageManager.NameNotFoundException e) {
            System.err.println("Błąd uruchamiania serwera: " + e.getMessage());
            Toast.makeText(this, "Wystąpił błąd", Toast.LENGTH_SHORT).show();
            Sentry.captureException(e);
            finish();
        }


    }

    @Override
    protected void onDestroy() {
        ViewManager.unregisterListener(this);

        super.onDestroy();
        if (server != null) {
            server.stop();
            pingServer.stop();
        }
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