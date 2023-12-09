package it.letscode.simappdevice;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.core.content.ContextCompat;

public class CallPhone {

    /**
     * No usage! TODO :)
     */

    public Context context;

    public CallPhone(Context context) {
        this.context = context;
    }
    public Boolean makeCall(String phoneNumber) {

        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + Uri.encode(phoneNumber)));

        int permissionCheck = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            context.startActivity(intent);
            return true;
        } else {
            System.out.println("Brak uprawnien do ussd code");
            return false;
        }
    }
}
