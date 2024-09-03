package it.letscode.simappdevice;

import android.telecom.Call;
import android.telecom.InCallService;
import android.util.Log;

public class MyInCallService extends InCallService {

    private static final String TAG = "MyInCallService";

    @Override
    public void onCallAdded(Call call) {
        super.onCallAdded(call);
        Log.d(TAG, "Nowe połączenie zostało dodane: " + call.toString());
        Log.d(TAG, "Stan połączenia: " + call.getState());

        // Dodanie nasłuchiwania zmian stanu połączenia
        call.registerCallback(new Call.Callback() {
            @Override
            public void onStateChanged(Call call, int state) {
                super.onStateChanged(call, state);
                Log.d(TAG, "Stan połączenia zmieniony: " + state);
                if (state == Call.STATE_ACTIVE) {
                    Log.d(TAG, "Połączenie zostało odebrane: " + call.toString());
                    onCallAnswered(call);
                }
            }
        });
    }

    @Override
    public void onCallRemoved(Call call) {
        super.onCallRemoved(call);
        Log.d(TAG, "Połączenie zostało usunięte: " + call.toString());
    }

    public void mergeCalls(Call activeCall, Call heldCall) {
        if (activeCall != null && heldCall != null) {
            Log.d(TAG, "Scalanie połączeń: Aktywne -> " + activeCall.toString() + " , Zawieszone -> " + heldCall.toString());
            activeCall.conference(heldCall);
            Log.d(TAG, "Połączenia zostały scalone.");
        } else {
            Log.w(TAG, "Nie udało się scalić połączeń: jedno z połączeń jest null.");
        }
    }

    // Metoda wywoływana, gdy połączenie zostanie odebrane
    public void onCallAnswered(Call call) {
        Log.d(TAG, "Wykonywanie akcji po odebraniu połączenia: " + call.toString());
        // Dodaj tutaj kod, który ma się wykonać, gdy połączenie zostanie odebrane
    }
}
