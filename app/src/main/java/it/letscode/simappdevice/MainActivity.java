package it.letscode.simappdevice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.content.pm.PackageManager;
import android.widget.Toast;

import org.json.JSONException;
import io.sentry.Sentry;

public class MainActivity extends AppCompatActivity {

    private MyHTTPServer server;
    private TextView ipAddressTextView;

    private PingServer pingServer = new PingServer();

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ApplicationContextProvider.initialize(getApplicationContext());

        super.onCreate(savedInstanceState);
//    // waiting for view to draw to better represent a captured error with a screenshot
//    findViewById(android.R.id.content).getViewTreeObserver().addOnGlobalLayoutListener(() -> {
//      try {
//        throw new Exception("This app uses Sentry! :)");
//      } catch (Exception e) {
//        Sentry.captureException(e);
//      }
//    });

        setContentView(R.layout.activity_main);

        ipAddressTextView = findViewById(R.id.ip_address_text_view);

        String ipAddress = getDeviceIpAddress(this);
        ipAddressTextView.setText("Adres IP: " + ipAddress);

        int port = 8888;
        try {
            server = new MyHTTPServer(this, port);
            server.start();
            System.out.println("Serwer działa na porcie: " + port);
        } catch (IOException e) {
            System.err.println("Błąd uruchamiania serwera: " + e.getMessage());
            Toast.makeText(this, "Wystąpił błąd", Toast.LENGTH_SHORT).show();
            finish();
        }

        NetworkSignalStrengthChecker networkSignalStrengthChecker = new NetworkSignalStrengthChecker(this);
        networkSignalStrengthChecker.startSignalStrengthCheck();


        MyPreferences myPreferences = new MyPreferences();

        myPreferences.setContext(getApplicationContext());

        myPreferences.generateDeviceUuidIfNotExist();

        String prefsContent = myPreferences.getAllPreferences();
        Log.d("SharedPreferences", "Zawartość: " + prefsContent);

        Device.login();

        pingServer.start();

        if(myPreferences.trustedNumberExist()) {
            SmsSender smsSender = new SmsSender();
            smsSender.sendSms(myPreferences.getTrustedNumber(), "Start Sim App Device");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (server != null) {
            server.stop();
            pingServer.stop();
        }
    }


    private String getDeviceIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ip = wifiInfo.getIpAddress();
            return formatIpAddress(ip);
        }
        return null;
    }

    private String formatIpAddress(int ipAddress) {
        return (ipAddress & 0xFF) + "." +
                ((ipAddress >> 8) & 0xFF) + "." +
                ((ipAddress >> 16) & 0xFF) + "." +
                (ipAddress >> 24 & 0xFF);
    }
}