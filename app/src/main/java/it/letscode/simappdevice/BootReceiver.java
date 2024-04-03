package it.letscode.simappdevice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import it.letscode.simappdevice.WifiKeeperService;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {


        /**
         * Don't hate me!
         * ! Important
         */
        ApplicationContextProvider.initialize(context.getApplicationContext());




        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, WifiKeeperService.class);
            // Dla Android Oreo (API 26) i nowszych, użyj startForegroundService dla usług działających w tle
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                // Dla starszych wersji Androida, użyj startService
                context.startService(serviceIntent);
            }
        }
    }
}
