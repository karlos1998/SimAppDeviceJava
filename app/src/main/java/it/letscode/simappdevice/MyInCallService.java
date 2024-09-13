package it.letscode.simappdevice;

import android.telecom.Call;
import android.telecom.InCallService;
import android.util.Log;

import java.util.List;

public class MyInCallService extends InCallService {
    private static MyInCallService instance;

    private static final String TAG = "MyInCallService";

    public static MyInCallService getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    public void onCallAdded(Call call) {
        super.onCallAdded(call);
        Log.d(TAG, "onCallAdded");
    }

    @Override
    public void onCallRemoved(Call call) {
        super.onCallRemoved(call);
        Log.d(TAG, "onCallRemoved");
    }

    public void mergeCalls() {
        // Implementacja logiki do łączenia połączeń
        // Użyj odpowiednich metod TelecomManager, aby połączyć aktywne połączenia
    }
}

