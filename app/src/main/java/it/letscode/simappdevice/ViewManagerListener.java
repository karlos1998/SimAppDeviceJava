package it.letscode.simappdevice;

public interface ViewManagerListener {
    void onHttpConnectionStatusChanged(boolean isConnected);

    void onSocketConnectionStatusChanged(boolean isConnected);
}
