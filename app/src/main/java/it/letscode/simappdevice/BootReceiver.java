package it.letscode.simappdevice;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, MyBackgroundService.class);
            context.startService(serviceIntent);
        }
    }
}
