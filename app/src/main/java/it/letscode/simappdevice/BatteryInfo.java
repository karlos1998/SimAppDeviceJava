package it.letscode.simappdevice;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import org.json.JSONException;
import org.json.JSONObject;

public class BatteryInfo {
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
            } catch (JSONException ignored) {}

            return batteryInfo;
        }
        return null;
    }
}