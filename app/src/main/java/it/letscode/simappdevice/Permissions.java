package it.letscode.simappdevice;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;

import io.sentry.Sentry;
import android.Manifest;
public class Permissions {
    public Map<String, Boolean> getAllPermissions() {
        Map<String, Boolean> permissionsMap = new HashMap<>();
        try {
            PackageInfo packageInfo = ApplicationContextProvider.getApplicationContext().getPackageManager().getPackageInfo("it.letscode.simappdevice", PackageManager.GET_PERMISSIONS);
            if (packageInfo != null && packageInfo.requestedPermissions != null) {
                for (String permission : packageInfo.requestedPermissions) {
                    int permissionCheck = ApplicationContextProvider.getApplicationContext().getPackageManager().checkPermission(permission, "it.letscode.simappdevice");
                    permissionsMap.put(permission, permissionCheck == PackageManager.PERMISSION_GRANTED);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Sentry.captureException(e);
            e.printStackTrace();
        }
        return permissionsMap;
    }


    public void requestPermissions() {

    }


    public boolean hasAllPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(ApplicationContextProvider.getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    private String[] getRequiredPermissions() {
        try {
            String[] permissions = ApplicationContextProvider.getApplicationContext().getPackageManager()
                    .getPackageInfo(ApplicationContextProvider.getApplicationContext().getPackageName(), PackageManager.GET_PERMISSIONS)
                    .requestedPermissions;
            if (permissions != null) {
                return permissions;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

}
