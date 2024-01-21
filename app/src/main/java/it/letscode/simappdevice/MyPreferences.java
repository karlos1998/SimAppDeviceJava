package it.letscode.simappdevice;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;
import java.util.UUID;

public class MyPreferences {
    private static final String PREFS_NAME = "MyPrefsFile";


    private static final String HOST_URL_KEY = "hostUrl";
    private static final String DEFAULT_HOST_URL = "http://192.168.98.113";


    private static final String TOKEN_KEY = "token";
    private static final String DEFAULT_TOKEN = "";


    private static SharedPreferences prefs;

    public MyPreferences() {}

    public void setContext(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Ustaw host aplikacji backendowej (laravel) w konfiguracji przez web panel
     * @param url String
     */
    public void setHostUrl(String url) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(HOST_URL_KEY, url);
        editor.apply();
    }

    /**
     * Pobierz url hosta - adres do backendu (laravel)
     * @return String
     */
    public String getHostUrl() {
        return prefs.getString(HOST_URL_KEY, DEFAULT_HOST_URL);
    }

    /**
     * Testowa funkcja pokazujaca zawartosc ZAPISANEJ konfiguracji
     * @return String
     */
    public String getAllPreferences() {
        Map<String, ?> allEntries = prefs.getAll();
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            builder.append(entry.getKey()).append(": ").append(entry.getValue().toString()).append("\n");
        }
        return builder.toString();
    }


    public String getLoginToken() {
        return prefs.getString(TOKEN_KEY, DEFAULT_TOKEN);
    }
    public boolean isLoginTokenExist() {
        return this.getLoginToken() != null && !this.getLoginToken().isEmpty();
    }

    public void setLoginToken(String token) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(TOKEN_KEY, token);
        editor.apply();
    }
    public void forgetLoginToken () {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(TOKEN_KEY, "");
        editor.apply();
    }


    //////
    public void setLastMmsAttachment(String data) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("LastMmsAttachment", data);
        editor.apply();
    }
    public String getLastMmsAttachment() {
        return prefs.getString("LastMmsAttachment", "");
    }
    ////////


    public String getDeviceUuid() {
        return prefs.getString("DeviceUniqueUuid", null);
    }
    public void generateDeviceUuidIfNotExist() {
        if(getDeviceUuid() == null || getDeviceUuid().isEmpty()) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("DeviceUniqueUuid", UUID.randomUUID().toString());
            editor.apply();
        }
    }


    ///

    public void setTrustedNumber(String data) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("TrustedNumber", data);
        editor.apply();
    }
    public String getTrustedNumber() {
        return prefs.getString("TrustedNumber", "");
    }
    public Boolean trustedNumberExist() {
        return getTrustedNumber() != null && !getTrustedNumber().isEmpty() && getTrustedNumber().length() > 5;
    }
}