package it.letscode.simappdevice;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PingServer {

    ControllerHttpGateway controllerHttpGateway = new ControllerHttpGateway();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final MyPreferences myPreferences = new MyPreferences();
    static public boolean isLoggedIn;
    static public String deviceId;

    private static int notLoggedCount = 0;

    public static void resetNotLoggedCount() {
        notLoggedCount = 0;
    }
    public static void receiveLoginStatus(boolean isLoggedIn) {
        PingServer.isLoggedIn = isLoggedIn;
        if(!isLoggedIn && myPreferences.isLoginTokenExist()) {
            if(++notLoggedCount > 8) { // 4 per minute == 2 minutes
                resetNotLoggedCount();
                Device.login();
            }
        }
    }

    public void start() {
        final Runnable task = () -> controllerHttpGateway.ping();

        // Uruchomienie zadania co 15 sekund
        scheduler.scheduleAtFixedRate(task, 0, 15, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdown();
    }
}
