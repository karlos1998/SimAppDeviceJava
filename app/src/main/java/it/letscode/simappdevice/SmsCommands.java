package it.letscode.simappdevice;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.List;

public class SmsCommands {

    private final SmsSender smsSender = new SmsSender();

    private final MyPreferences myPreferences = new MyPreferences();
    private final Wifi wifi = new Wifi();
    private String phoneNumber;
    public void checkSender(String phoneNumber, String text) {
        if(myPreferences.trustedNumberExist() && phoneNumber.equals(myPreferences.getTrustedNumber()) && text.startsWith("#")) {
            this.phoneNumber = phoneNumber;

            String command = text.substring(1);

            if(command.startsWith("wifi set")) {
                changeWifi(command);
            } else if(command.startsWith("wifi scan")) {
                wifiScan();
            } else if(command.startsWith("wifi check")) {
                wifiCheck();
            } else {
                sendResponse("Nieznana komenda: " + command);
            }
        }
    }

    private void sendResponse(String text) {
        smsSender.sendSms(phoneNumber, text);
    }

    private void changeWifi(String command) {
        String[] commandLines = command.split("\n");
        String networkName = commandLines[1];
        String networkPassword = commandLines[2];

        wifi.changeNetwork(networkName, networkPassword);

        sendResponse(String.format("Zmieniam dane sieci wifi na: \nNazwa: '%s'\nHasło: '%s'", networkName, networkPassword));
    }

    private void wifiScan() {
        StringBuilder data = new StringBuilder();
        List<ScanResult> scanResults =  wifi.scanResultsFromManager();
        for (ScanResult result : scanResults) {
            if(result.SSID.isEmpty()) continue;
            data.append(String.format("%s (%s%%) \n", result.SSID, WifiManager.calculateSignalLevel(result.level, 100)));
        }
        sendResponse(String.format("Sieci w pobliżu: %s", data));
    }

    private void wifiCheck() {
        wifi.getCurrentNetwork();

        String currentNetworkData = "Brak połączenia lub brak danych";

        if(!wifi.getSsid().isEmpty()) {
            currentNetworkData = String.format("%s (%s%%)", wifi.getSsid(), wifi.getSignalPercentage());
        }

        sendResponse(String.format("Aktualna sieć:\n%s", currentNetworkData));
    }
}
