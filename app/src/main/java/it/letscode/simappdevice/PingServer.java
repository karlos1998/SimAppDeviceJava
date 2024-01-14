package it.letscode.simappdevice;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PingServer {

    ControllerHttpGateway controllerHttpGateway = new ControllerHttpGateway();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    static public boolean isLoggedIn;
    static public String deviceId;
    public void start() {
        final Runnable task = () -> controllerHttpGateway.ping();

        // Uruchomienie zadania co 10 sekund
        scheduler.scheduleAtFixedRate(task, 0, 10, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdown();
    }
}
