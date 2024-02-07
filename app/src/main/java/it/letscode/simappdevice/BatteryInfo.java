package it.letscode.simappdevice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import io.sentry.Sentry;

public class BatteryInfo {

    static double lastKnownTemperature;
    public JSONObject getBatteryJsonData () {
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = ApplicationContextProvider.getApplicationContext().registerReceiver(null, iFilter);

        if(batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryPct = level / (float) scale;

            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

            JSONObject batteryInfo = new JSONObject();
            try {
                batteryInfo.put("percentage", batteryPct * 100);
                batteryInfo.put("isCharging", isCharging);
                batteryInfo.put("temperature", lastKnownTemperature);
            } catch (JSONException e) {
                Sentry.captureException(e);
            }

            return batteryInfo;
        }
        return null;
    }

    public void registerBatteryTemperatureReceiver() {
        BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
                lastKnownTemperature = temperature / 10.0;
                Log.d("Battery Temperature", "Temperatura baterii: " + lastKnownTemperature + "°C");
            }
        };

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        ApplicationContextProvider.getApplicationContext().registerReceiver(batteryReceiver, filter);
    }
}
