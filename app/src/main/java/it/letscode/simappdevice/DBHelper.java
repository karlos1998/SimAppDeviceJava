package it.letscode.simappdevice;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.sentry.Sentry;

public class DBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "HttpRequests.db";
    private static final String TABLE_NAME = "http_requests";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_URL = "url";
    private static final String COLUMN_REQUEST_DATA = "request_data";
    private static final String COLUMN_RESPONSE_DATA = "response_data";
    private static final String COLUMN_RESPONSE_CODE = "response_code";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_URL + " TEXT,"
                + COLUMN_REQUEST_DATA + " TEXT,"
                + COLUMN_RESPONSE_DATA + " TEXT,"
                + COLUMN_RESPONSE_CODE + " INTEGER" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addHttpRequest(String url, String requestData, String responseData, int responseCode) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_URL, url);
            values.put(COLUMN_REQUEST_DATA, requestData);
            values.put(COLUMN_RESPONSE_DATA, responseData);
            values.put(COLUMN_RESPONSE_CODE, responseCode);

            db.insert(TABLE_NAME, null, values);

            Log.d("DBHelper", "Insert to " + TABLE_NAME);

            String DELETE_EXCESS = "DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " NOT IN (SELECT " + COLUMN_ID + " FROM " + TABLE_NAME + " ORDER BY " + COLUMN_ID + " DESC LIMIT 10)";
            db.execSQL(DELETE_EXCESS);

        } catch (IllegalStateException e) {
            Log.e("DBHelper", "Wystąpił błąd IllegalStateException: " + e.getMessage());
            Sentry.captureException(e);

        } catch (Exception e) {
            Log.e("DBHelper", "Wystąpił inny błąd: " + e.getMessage());
            Sentry.captureException(e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    @SuppressLint("Range")
    public JSONArray getAllHttpRequests() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {
                COLUMN_ID,
                COLUMN_URL,
                COLUMN_REQUEST_DATA,
                COLUMN_RESPONSE_DATA,
                COLUMN_RESPONSE_CODE
        };
        String sortOrder = COLUMN_ID + " DESC"; // Sortuj od najnowszego do najstarszego

        Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, sortOrder);
        JSONArray jsonArray = new JSONArray();

        while (cursor.moveToNext()) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                obj.put("url", cursor.getString(cursor.getColumnIndex(COLUMN_URL)));
                obj.put("request_data", cursor.getString(cursor.getColumnIndex(COLUMN_REQUEST_DATA)));
                obj.put("response_data", cursor.getString(cursor.getColumnIndex(COLUMN_RESPONSE_DATA)));
                obj.put("response_code", cursor.getInt(cursor.getColumnIndex(COLUMN_RESPONSE_CODE)));
                jsonArray.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        db.close();
        return jsonArray;
    }

}
