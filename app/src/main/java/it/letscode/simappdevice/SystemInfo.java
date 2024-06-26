package it.letscode.simappdevice;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.CellIdentityGsm;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.sentry.Sentry;

public class SystemInfo {


    public JSONArray getBtsJsonData() {

        JSONArray data = new JSONArray();

        if (ActivityCompat.checkSelfPermission(ApplicationContextProvider.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return data;
        }

        TelephonyManager telephonyManager = (TelephonyManager) ApplicationContextProvider.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();

        if(cellInfoList == null) return null;

        for (CellInfo cellInfo : cellInfoList) {
            if (cellInfo instanceof CellInfoGsm) {
                CellIdentityGsm cellIdentity = ((CellInfoGsm) cellInfo).getCellIdentity();
                int cid = cellIdentity.getCid(); // ID stacji bazowej
                int lac = cellIdentity.getLac(); // Lokalny kod obszaru
                Log.d("NetworkInfo", "BTS ID: " + cid + ", LAC: " + lac);
                data.put(new JSONObject(){{
                    try {
                        put("lac", lac);
                        put("cid", cid);
                    } catch (JSONException e) {
                        Sentry.captureException(e);
                    }
                }});
            }
        }

        return data;
    }
    public JSONObject getJsonDetails() {

        JSONObject deviceInfo = new JSONObject();
        try {
            deviceInfo.put("isRooted", RootChecker.isDeviceRooted());
            deviceInfo.put("isSystemApp", SystemAppChecker.isSystemApp());
            deviceInfo.put("manufacturer", getManufacturer());
            deviceInfo.put("model", getModel());
            deviceInfo.put("device", getDevice());
            deviceInfo.put("product", getProduct());
            deviceInfo.put("brand", getBrand());
            deviceInfo.put("androidVersion", getAndroidVersion());
            deviceInfo.put("buildId", getBuildId());
            deviceInfo.put("sdkVersion", getSdkVersion());
            deviceInfo.put("operatorName", getOperatorName());
            deviceInfo.put("phoneNumber", getPhoneNumber());
            deviceInfo.put("bts", getBtsJsonData());

//            Log.d("DeviceInfo", deviceInfo.toString(4));
        } catch (JSONException|NullPointerException e) {
            Log.d("DeviceInfo", "Json Error: " + e.getMessage());
            Sentry.captureException(e);
        }

        return deviceInfo;
    }

    public String getManufacturer() {
        return Build.MANUFACTURER;
    }

    public String getModel() {
        return Build.MODEL;
    }

    private String getDevice() {
        return Build.DEVICE;
    }

    private String getProduct() {
        return Build.PRODUCT;
    }

    private String getBrand() {
        return Build.BRAND;
    }

    private String getAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    private String getBuildId() {
        return Build.ID;
    }

    public int getSdkVersion() {
        return Build.VERSION.SDK_INT;
    }

    private String getOperatorName() {
        TelephonyManager telephonyManager = (TelephonyManager) ApplicationContextProvider.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getNetworkOperatorName();
    }

    @SuppressLint("HardwareIds")
    private String getPhoneNumber() {
        List<String> numbers = new ArrayList<>();

        SubscriptionManager subscriptionManager = (SubscriptionManager) ApplicationContextProvider.getApplicationContext().getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        if (ActivityCompat.checkSelfPermission(ApplicationContextProvider.getApplicationContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            List<SubscriptionInfo> subscriptionInfos = subscriptionManager.getActiveSubscriptionInfoList();
            for (SubscriptionInfo subscriptionInfo : subscriptionInfos) {
//                numbers.add(subscriptionInfo.getNumber());
                int subscriptionId = subscriptionInfo.getSubscriptionId();
                TelephonyManager specificTelephonyManager = (TelephonyManager) ApplicationContextProvider.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
                numbers.add(specificTelephonyManager.getLine1Number());

            }
        }
        return String.join(", ", numbers);
    }

}
