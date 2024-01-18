package it.letscode.simappdevice;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsMessage;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class MmsReceiver extends BroadcastReceiver {

    private static final String TAG = "MMSReceiver";

    private static final MyPreferences myPreferences = new MyPreferences();

    private static final ControllerHttpGateway controllerHttpGateway = new ControllerHttpGateway();
    private static Integer skipMms = 0;
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("Przyjęto mms do odczytania");
        skipMms++;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            readMMSAttachments(context);
        }, 10000); // Opóźnienie 1 sekundy
    }

    public static void readMMSAttachments(Context context) {
        Uri mmsUri = Uri.parse("content://mms");
        String[] projection = new String[] { "_id" };
        String sortOrder = "date DESC LIMIT " + (--skipMms) + ", 1";

        try (Cursor cursor = context.getContentResolver().query(mmsUri, projection, null, null, sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                int mmsId = cursor.getInt(cursor.getColumnIndex("_id"));
                Log.d(TAG, "MMS ID: " + mmsId);

                // Pobierz numer nadawcy
                String senderNumber = getSenderNumber(context, mmsId);
                Log.d(TAG, "Sender Number: " + senderNumber);

                // Przetwarzanie załączników
                processAttachments(context, mmsId);
            }
        }
    }

    private static String getSenderNumber(Context context, int mmsId) {
        Uri uri = Uri.parse("content://mms/" + mmsId + "/addr");
        String selection = "type=137"; // Type 137 dla nadawcy MMS
        String senderNumber = "";
        try (Cursor addrCursor = context.getContentResolver().query(uri, null, selection, null, null)) {
            if (addrCursor != null && addrCursor.moveToFirst()) {
                senderNumber = addrCursor.getString(addrCursor.getColumnIndex("address"));
            }
        }
        return senderNumber;
    }

    private static void processAttachments(Context context, int mmsId) {

        String randomUuid = UUID.randomUUID().toString();

        String selectionPart = "mid=" + mmsId;
        Uri uri = Uri.parse("content://mms/part");
        try (Cursor cPart = context.getContentResolver().query(uri, null, selectionPart, null, null)) {
            if (cPart != null && cPart.moveToFirst()) {
                do {
                    String partId = cPart.getString(cPart.getColumnIndex("_id"));
                    String type = cPart.getString(cPart.getColumnIndex("ct"));

                    String text = null;

                    if ("text/plain".equals(type)) {
                        String data = cPart.getString(cPart.getColumnIndex("_data"));
                        if (data != null) {
                            // Odczytaj tekst z pliku
                            text = getTextFromPartFile(context, data);
                        } else {
                            // Odczytaj tekst bezpośrednio
                            text = cPart.getString(cPart.getColumnIndex("text"));
                        }
                        System.out.println("Załącznik tekstowy: " + text);

                    } else if ("image/jpeg".equals(type) || "image/bmp".equals(type) ||
                            "image/gif".equals(type) || "image/jpg".equals(type) ||
                            "image/png".equals(type)) {
                        Bitmap bitmap = getMmsImageBitmap(partId);
                        text = convertBitmapToBase64(bitmap);
                        System.out.println("BASE64: " + text.length());

                        myPreferences.setLastMmsAttachment(text);
                    }

                    controllerHttpGateway.sendAttachmentToMessage(randomUuid, type, text);

                } while (cPart.moveToNext());
            }
        }
    }

    private static String getTextFromPartFile(Context context, String dataPath) {
        Uri partURI = Uri.parse("content://mms/part/" + dataPath);
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream is = context.getContentResolver().openInputStream(partURI)) {
            try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                try (BufferedReader reader = new BufferedReader(isr)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Błąd odczytu tekstu załącznika MMS", e);
        }
        return stringBuilder.toString();
    }

    private static Bitmap getMmsImageBitmap(String _id) {
        Uri partURI = Uri.parse("content://mms/part/" + _id);
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            is = ApplicationContextProvider.getApplicationContext().getContentResolver().openInputStream(partURI);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {}
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {}
            }
        }
        return bitmap;
    }

    public static String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

}