package it.letscode.simappdevice;

import android.content.Context;
import android.content.pm.PackageInfo;

public class ApplicationContextProvider {
    private static Context applicationContext;
    private static PackageInfo packageInfo;

    public static void initialize(Context context) {
        applicationContext = context.getApplicationContext();
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
