package it.letscode.simappdevice;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.text.format.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class SmsReader {
    public List<SmsMessage> getLastMessages(int numberOfMessages, Context context) {
        List<SmsMessage> messages = new ArrayList<>();
        Uri uri = Telephony.Sms.CONTENT_URI;

        String[] projection = {Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.TYPE}; // Dodanie kolumny TYPE
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, Telephony.Sms.DEFAULT_SORT_ORDER + " LIMIT " + numberOfMessages);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String senderNumber = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS));
                String messageBody = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
                long dateInMillis = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE));
                int messageType = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.TYPE)); // Pobranie rodzaju wiadomości

                String formattedDate = DateUtils.formatDateTime(context, dateInMillis, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME); // Konwersja daty

//                String type = (messageType == Telephony.Sms.MESSAGE_TYPE_INBOX) ? "Incoming" : "Outgoing"; // Sprawdzenie rodzaju wiadomości
                Boolean isIncoming = messageType == Telephony.Sms.MESSAGE_TYPE_INBOX;

                SmsMessage sms = new SmsMessage(senderNumber, messageBody, formattedDate, isIncoming);
                messages.add(sms);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return messages;
    }
}
