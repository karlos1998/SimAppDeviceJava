package it.letscode.simappdevice;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class ApplicationContextProvider {
    private static Context applicationContext;
    private static PackageInfo packageInfo;

    public static void initialize(Context context) {
        applicationContext = context.getApplicationContext();

        /**
         * ! Important
         */
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            setPackageInfo(packageInfo);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Context getApplicationContext() {
        return applicationContext;
    }

    public static void setPackageInfo(PackageInfo packageInfo) {
        ApplicationContextProvider.packageInfo = packageInfo;
    }

    public static PackageInfo getPackageInfo() {
        return packageInfo;
    }
}
