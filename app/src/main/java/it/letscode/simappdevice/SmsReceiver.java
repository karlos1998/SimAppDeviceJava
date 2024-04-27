package it.letscode.simappdevice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";

    private final ControllerHttpGateway controllerHttpGateway = new ControllerHttpGateway();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            StringBuilder body = new StringBuilder();
            String senderNumber = messages[0].getOriginatingAddress();
            long timestamp = messages[0].getTimestampMillis();

            for (SmsMessage message : messages) {
                body.append(message.getMessageBody());
            }

            String messageBody = body.toString();

            Log.d(TAG, "New SMS received:");
            Log.d(TAG, "Sender: " + senderNumber);
            Log.d(TAG, "Message: " + messageBody);
            Log.d(TAG, "Timestamp: " + timestamp);

            SmsCommands smsCommands = new SmsCommands();
            smsCommands.checkSender(senderNumber, messageBody);

            controllerHttpGateway.saveReceivedMessage(senderNumber, messageBody, timestamp / 1000);
        }
    }

}
