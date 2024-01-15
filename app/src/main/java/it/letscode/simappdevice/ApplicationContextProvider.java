package it.letscode.simappdevice;

import android.content.Context;

public class ApplicationContextProvider {
    private static Context applicationContext;

    public static void initialize(Context context) {
        applicationContext = context.getApplicationContext();
    }

    public static Context getApplicationContext() {
        return applicationContext;
    }
}
