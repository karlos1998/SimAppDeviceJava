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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MmsReceiver extends BroadcastReceiver {

    private static final String TAG = "MMSReceiver";

    private static final MyPreferences myPreferences = new MyPreferences();

    private static final ControllerHttpGateway controllerHttpGateway = new ControllerHttpGateway();
    private static Integer skipMms = 0;

    public static class AttachmentDetails {
        private final String type;
        private final String text;

        public AttachmentDetails(String type, String text) {
            this.type = type;
            this.text = text;
        }

        public String getType() {
            return type;
        }

        public String getText() {
            return text;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("Przyjęto mms do odczytania");
        skipMms++;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            readMMSAttachments(context);
        }, 10000);
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
                List<AttachmentDetails> attachmentDetailsList = processAttachments(context, mmsId);

                //Pobranie daty wiadomosci
                long timestamp = getMmsTimestamp(context, mmsId);

                createMessageAndSendAttachments(senderNumber, timestamp, attachmentDetailsList);
            }
        }
    }

    private static void createMessageAndSendAttachments(String senderNumber, long timestamp, List<AttachmentDetails> attachmentDetailsList) {
        controllerHttpGateway.saveReceivedMessage(senderNumber, null, timestamp, new ControllerHttpGateway.ResponseCallback() {
            @Override
            public void onResponse(JSONObject data, int responseCode) {
                if(responseCode != 200)
                {
                    System.out.println("Bład zapisywania MMS: " + responseCode);
                    return;
                }
                try {
                    String messageId = data.getString("id");

                   MmsReceiver mmsReceiver = new MmsReceiver();
                   mmsReceiver.sendAttachments(messageId, attachmentDetailsList);
                } catch (JSONException ignored) {

                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                System.out.println("Nie udało sie zapisac MMS w controllerze.");
            }
        });
    }


    /////////////////////////////////////////////////////////

    public void sendAttachments(String messageId, List<AttachmentDetails> attachmentDetailsList) {
        for (AttachmentDetails attachmentDetails : attachmentDetailsList) {
            List<String> parts = splitIntoParts(attachmentDetails.getText(), 102400); // Dzielenie na części o rozmiarze 0.1 MB
            String messageAttachmentUuid = UUID.randomUUID().toString();
            sendPart(messageId, messageAttachmentUuid, attachmentDetails, parts, 0);
        }
    }

    // Metoda do wysyłania części załącznika
    private void sendPart(String messageId, String messageAttachmentUuid, AttachmentDetails attachmentDetails, List<String> parts, int index) {
        if (index >= parts.size()) {
            return; // Wszystkie części zostały wysłane
        }

        String part = parts.get(index);
        controllerHttpGateway.sendAttachmentToMessage(messageId, messageAttachmentUuid, attachmentDetails.getType(), part, ((index + 1) >= parts.size()), new ControllerHttpGateway.ResponseCallback() {
            @Override
            public void onResponse(JSONObject data, int responseCode) {
                // Wysyłanie kolejnej części
                sendPart(messageId, messageAttachmentUuid, attachmentDetails, parts, index + 1);
            }

            @Override
            public void onFailure(Throwable throwable) {
                // Obsługa błędu...
            }
        });
    }

    // Metoda do dzielenia tekstu na części
    private List<String> splitIntoParts(String text, int partSize) {
        List<String> parts = new ArrayList<>();
        int length = text.length();
        for (int i = 0; i < length; i += partSize) {
            parts.add(text.substring(i, Math.min(length, i + partSize)));
        }
        return parts;
    }

    /////////////////////////////////////////////////////////

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

    private static long getMmsTimestamp(Context context, int mmsId) {
        Uri uri = Uri.parse("content://mms/" + mmsId);
        String[] projection = new String[]{"date"};
        long timestamp = 0;

        try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                timestamp = cursor.getLong(cursor.getColumnIndex("date"));
            }
        }

        return timestamp;
    }

    private static List<AttachmentDetails> processAttachments(Context context, int mmsId) {

        List<AttachmentDetails> attachmentDetailsList = new ArrayList<AttachmentDetails>();

        String selectionPart = "mid=" + mmsId;
        Uri uri = Uri.parse("content://mms/part");
        try (Cursor cPart = context.getContentResolver().query(uri, null, selectionPart, null, null)) {
            if (cPart != null && cPart.moveToFirst()) {
                do {
                    String partId = cPart.getString(cPart.getColumnIndex("_id"));
                    String type = cPart.getString(cPart.getColumnIndex("ct"));

                    String text = "";

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

                    attachmentDetailsList.add(new AttachmentDetails(type, text));
//                    controllerHttpGateway.sendAttachmentToMessage(randomUuid, type, text);

                } while (cPart.moveToNext());
            }
        }

        return attachmentDetailsList;
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