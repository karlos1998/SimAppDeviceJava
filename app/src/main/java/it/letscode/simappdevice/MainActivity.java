package it.letscode.simappdevice;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private MyHTTPServer server;
    private TextView ipAddressTextView;
    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        }

        SmsSender smsSender = new SmsSender();
        smsSender.sendSms("+48884167733", "Start Sim App Device");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (server != null) {
            server.stop();
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