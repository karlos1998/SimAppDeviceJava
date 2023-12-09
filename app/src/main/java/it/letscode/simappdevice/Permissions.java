package it.letscode.simappdevice;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.HashMap;
import java.util.Map;

public class Permissions {
    public Map<String, Boolean> getAllPermissions(PackageManager packageManager) {
        Map<String, Boolean> permissionsMap = new HashMap<>();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo("it.letscode.simappdevice", PackageManager.GET_PERMISSIONS);
            if (packageInfo != null && packageInfo.requestedPermissions != null) {
                for (String permission : packageInfo.requestedPermissions) {
                    int permissionCheck = packageManager.checkPermission(permission, "it.letscode.simappdevice");
                    permissionsMap.put(permission, permissionCheck == PackageManager.PERMISSION_GRANTED);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return permissionsMap;
    }
}
