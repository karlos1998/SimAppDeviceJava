package it.letscode.simappdevice;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PingServer {

    ControllerHttpGateway controllerHttpGateway = new ControllerHttpGateway();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final MyPreferences myPreferences = new MyPreferences();
    private static final SocketClient socketClient = new SocketClient();
    static public boolean isLoggedIn;
    static public String deviceId;

    private static int notLoggedCount = 0;
    private static int socketNotLoggedCount = 0;

    public static void resetNotLoggedCount() {
        notLoggedCount = 0;
        socketNotLoggedCount = 0;
    }
    public static void receiveLoginStatus(boolean isLoggedIn) {
        PingServer.isLoggedIn = isLoggedIn;
        if(myPreferences.isLoginTokenExist()) {
            if(!isLoggedIn) {
                if(++notLoggedCount > 8) {
                    resetNotLoggedCount();
                    Device.login();
                }
            } else if(!socketClient.isConnected()) {
                if(++socketNotLoggedCount > 8) {
                    resetNotLoggedCount();
                    Device.login();
                }
            }
        }
    }

    public void start() {
        final Runnable task = () -> controllerHttpGateway.ping();

        // Uruchomienie zadania co 10 sekund
        scheduler.scheduleAtFixedRate(task, 0, 10, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdown();
    }
}
