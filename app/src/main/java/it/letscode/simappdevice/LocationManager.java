package it.letscode.simappdevice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.provider.Telephony;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import io.sentry.Sentry;

public class LocationManager {

    public interface LocationUpdateListener {
        void onLocationUpdated(LocationData location);
    }

    public static class LocationData {
        public double latitude;
        public double longitude;

        public LocationData(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public JSONObject toJSON() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("latitude", latitude);
                jsonObject.put("longitude", longitude);
            } catch (JSONException e) {
                System.out.println("Read location error:" + e.toString());
                Sentry.captureException(e);
            }
            return jsonObject;
        }
    }

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private static LocationData currentLocation;
    private LocationUpdateListener listener;

    public LocationManager(Context context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        createLocationRequest();
        createLocationCallback();
    }

    public void setListener(LocationUpdateListener listener) {
        this.listener = listener;
    }

    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    currentLocation = new LocationData(location.getLatitude(), location.getLongitude());
                    if (listener != null) {
                        listener.onLocationUpdated(currentLocation);
                    }
                }
            }
        };
    }

    @SuppressLint("MissingPermission") // Upewnij się, że masz odpowiednie uprawnienia w miejscu wywołania
    public void startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    public void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    public static LocationData getCurrentLocation() {
        return currentLocation;
    }
}
