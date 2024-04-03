package it.letscode.simappdevice;

import static android.content.Context.POWER_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;
import static com.google.common.reflect.Reflection.getPackageName;
import static it.letscode.simappdevice.ApplicationContextProvider.getApplicationContext;
import static it.letscode.simappdevice.MessagesQueue.startRemoveOldQueuedSmsLoopHelper;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.IOException;

import io.sentry.Sentry;

public class Bootloader {

    private static final PingServer pingServer = new PingServer();
    private static final BatteryInfo batteryInfo = new BatteryInfo();

    public static void run() {

        int port = 8888;
        try {

            /**
             * Run webserwer
             */
            MyHTTPServer server = new MyHTTPServer(port);
            server.start();
            System.out.println("Serwer działa na porcie: " + port);




            MyPreferences myPreferences = new MyPreferences();

            myPreferences.setContext(getApplicationContext());

            myPreferences.generateDeviceUuidIfNotExist();

            String prefsContent = myPreferences.getAllPreferences();
            Log.d("SharedPreferences", "Zawartość: " + prefsContent);



            NetworkSignalStrengthChecker networkSignalStrengthChecker = new NetworkSignalStrengthChecker(ApplicationContextProvider.getApplicationContext());
            networkSignalStrengthChecker.startSignalStrengthCheck();

            Device.login();

            pingServer.start();

            startRemoveOldQueuedSmsLoopHelper();

            if(myPreferences.trustedNumberExist()) {
                SmsSender smsSender = new SmsSender();
                smsSender.sendSms(myPreferences.getTrustedNumber(), "Start Sim App Device");
            }

            batteryInfo.registerBatteryTemperatureReceiver();

            LocationManager locationManager = new LocationManager(ApplicationContextProvider.getApplicationContext());
//            locationManager.setListener(new LocationManager.LocationUpdateListener() {
//                @Override
//                public void onLocationUpdated(LocationManager.LocationData location) {
//                    // Reaguj na aktualizacje lokalizacji, np. aktualizuj UI
//                    System.out.println("Nowa lokalizacja: Latitude: " + location.latitude + ", Longitude: " + location.longitude);
//                }
//            });
            locationManager.startLocationUpdates();

        } catch (IOException e) {
            System.err.println("Błąd uruchamiania serwera: " + e.getMessage());
            Sentry.captureException(e);
        }
    }

}
