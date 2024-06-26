package it.letscode.simappdevice;

import java.util.ArrayList;
import java.util.List;

public class ViewManager {
    private static final List<ViewManagerListener> listeners = new ArrayList<>();

    public static void clearListeners() {
        listeners.clear();
    }

    public static void registerListener(ViewManagerListener listener) {
        listeners.add(listener);
    }

    public static void unregisterListener(ViewManagerListener listener) {
        listeners.remove(listener);
    }

    public static void changeHttpConnectionStatus(boolean isLoggedIn) {
        for (ViewManagerListener listener : listeners) {
            listener.onHttpConnectionStatusChanged(isLoggedIn);
        }
    }

    public static void changeSocketConnectionStatus(boolean isConnected) {
        for (ViewManagerListener listener : listeners) {
            listener.onSocketConnectionStatusChanged(isConnected);
        }
    }

    public static void changeControllerUrl(String url) {
        for (ViewManagerListener listener : listeners) {
            listener.noControllerUrlChanged(url);
        }
    }

    public static void changeDeviceId(String deviceId) {
        for (ViewManagerListener listener : listeners) {
            listener.onDeviceIdChanged(deviceId);
        }
    }
}
