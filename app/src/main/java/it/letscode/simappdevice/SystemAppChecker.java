package it.letscode.simappdevice;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class SystemAppChecker {
    public static boolean isSystemApp() {
        try {
            ApplicationInfo appInfo = ApplicationContextProvider.getApplicationContext().getPackageManager().getApplicationInfo(ApplicationContextProvider.getApplicationContext().getPackageName(), 0);
            return (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
}
