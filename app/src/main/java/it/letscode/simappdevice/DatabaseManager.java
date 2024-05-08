package it.letscode.simappdevice;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;

import io.sentry.Sentry;

public class DatabaseManager {
    private static DBHelper dbHelper = null;

    public static void initializeInstance(Context context) {
        if (dbHelper == null) {
            dbHelper = new DBHelper(context);
        }
    }

    public static void addHttpRequest(String url, String requestData, String responseData, int responseCode) {
        if (dbHelper != null) {
            try {
                dbHelper.addHttpRequest(url, requestData, responseData, responseCode);
            } catch (Exception e) {
                Sentry.captureException(e);
            }
        } else {
            Log.d("DatabaseManager", "DBHelper is not initialized.");
        }
    }

    public static JSONArray getAllHttpRequests()
    {
        if (dbHelper != null) {
            return dbHelper.getAllHttpRequests();
        } else {
            Log.d("DatabaseManager", "DBHelper is not initialized.");
        }
        return new JSONArray();
    }
}
