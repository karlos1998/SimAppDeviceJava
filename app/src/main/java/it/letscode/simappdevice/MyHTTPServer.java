package it.letscode.simappdevice;

import static java.security.AccessController.getContext;

import static it.letscode.simappdevice.DatabaseManager.getAllHttpRequests;
import static it.letscode.simappdevice.VersionManager.getVersionJson;

import android.annotation.SuppressLint;
import android.content.Context;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.iki.elonen.NanoHTTPD;
import io.sentry.Sentry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
public class MyHTTPServer  extends NanoHTTPD {
    private final String adminPassword = "lci123password";

    private SmsSender smsSender;
    private SmsReader smsReader;

    private Permissions permissions;

    private MyPreferences myPreferences;

    ControllerHttpGateway controllerHttpGateway;

    SocketClient socketClient;

    private final SystemInfo systemInfo = new SystemInfo();
    public MyHTTPServer(int port) {
        super(port);

        this.smsSender = new SmsSender();
        this.smsReader = new SmsReader();
        this.permissions = new Permissions();

        this.myPreferences = new MyPreferences();
        this.controllerHttpGateway = new ControllerHttpGateway();

        this.socketClient = new SocketClient();

    }

    @Override
    public Response serve(IHTTPSession session) {
        // Zbierz dane wysłane przez klienta (przeglądarkę)
        Method method = session.getMethod();
        String uri = session.getUri();
        String cookieHeader = session.getHeaders().get("cookie");

        boolean isLogged = cookieHeader != null && cookieHeader.contains("lci-session=" + adminPassword);

        System.out.println("Cookie header: " + cookieHeader);

        if (uri.equals("/style.css")) {
            return serveStaticFile("style.css", "text/css");
        } else if (uri.equals("/favicon.ico")) {
            return serveStaticFile("favicon.ico", "image/x-icon");
        }


        Map<String, String> postData = new HashMap<>();

        if(method == Method.POST) {

            /**
             * Todo: kod sie strasznie wolno wykonuje
             */
            InputStream inputStream = session.getInputStream();
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            try {
                while ((length = inputStream.read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                    if(length < 1024) break;
                }
            } catch (IOException e) {
                Sentry.captureException(e);
                e.printStackTrace();
            }

            byte[] byteArray = result.toByteArray();
            String postBody = new String(byteArray, StandardCharsets.UTF_8);

            String[] pairs = postBody.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    String value = keyValue[1];
                    postData.put(key, value);
                }
            }
            System.out.println("POST body: " + postBody);
        }

//        Map<String, String> params = new HashMap<>();
//        try {
//            session.parseBody(params);
//            System.out.println("get query: " + session.getQueryParameterString());
//        } catch (IOException | ResponseException e) {
//            Sentry.captureException(e);
//            e.printStackTrace();
//        }
//
//        System.out.println("params");
//        System.out.println(params);


        if(isLogged) {
            switch (uri) {
                case "/":
                    return loadPage("index.html");
                case "/send_sms":
                    if (method == Method.POST) {
                        String number = postData.get("number");
                        String text = postData.get("text");
                        try {
                            text = URLDecoder.decode(text, StandardCharsets.UTF_8.name());
                        } catch (UnsupportedEncodingException e) {
                            Sentry.captureException(e);
                            System.out.print("Nie udało się dekodować tekstu z formularza wysylania wiadommosci na Utf8 - wyslam tekst przed dekodowaniem.");
                        }
                        smsSender.sendSms(number, text);
                        return redirect("/");
                    } else {
                        return loadPage("send_sms.html");
                    }
                case "/sms_list": {
                    Map<String, Object> data = new HashMap<>();
                    data.put("messages", smsReader.getLastMessages(10, ApplicationContextProvider.getApplicationContext()));
                    return loadPage("sms_list.html", data);
                }
                case "/debug": {

                    // Pobranie danych o uprawnieniach
                    Map<String, Boolean> permissions = this.permissions.getAllPermissions();

                    // Przygotowanie danych do wstrzyknięcia w szablon
                    List<Map<String, Object>> permissionList = new ArrayList<>();
                    for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
                        Map<String, Object> permissionData = new HashMap<>();
                        permissionData.put("name", entry.getKey());
                        permissionData.put("granted", entry.getValue());
                        permissionList.add(permissionData);
                    }

                    // Przygotowanie danych do wstrzyknięcia w szablon Mustache
                    Map<String, Object> data = new HashMap<>();
                    data.put("permissions", permissionList);

                    return loadPage("debug.html", data);
                }
                case "/controller_configuration":
                    if (method == Method.POST) {
                        String url = postData.get("url");
                        try {
                            url = URLDecoder.decode(url, StandardCharsets.UTF_8.name());
                        } catch (UnsupportedEncodingException e) {
                            Sentry.captureException(e);
                        }
                        assert url != null;
                        myPreferences.setHostUrl(url.startsWith("http") ? url : ("http://" + url));

                        String token = postData.get("token");

                        try {
                            token = URLDecoder.decode(token, StandardCharsets.UTF_8.name());
                        } catch (UnsupportedEncodingException e) {
                            Sentry.captureException(e);
                        }

                        controllerHttpGateway.pair(token);

                        return redirect("/");
                    } else if(method == Method.DELETE) {
                        Device.clear();

                        return newFixedLengthResponse(Response.Status.NO_CONTENT, null, null);
                    } else {
                        Map<String, Object> data = new HashMap<>();
                        data.put("url", myPreferences.getHostUrl());
//                        data.put("token", myPreferences.getLoginToken());

                        JSONObject json = new JSONObject() {{
                            try {
                                put("loginTokenExist", myPreferences.isLoginTokenExist());
                            } catch (JSONException e) {
                                Sentry.captureException(e);
                            }
                        }};
                        data.put("data", json.toString());

                        return loadPage("controller_configuration.html", data);
                    }
                case "/data.json":

                    return newFixedLengthResponse(Response.Status.OK, "application/json", this.jsonData(4));

                case "/last_mms": {
                    Map<String, Object> data = new HashMap<>();
                    data.put("lastMmsData", myPreferences.getLastMmsAttachment());
                    return loadPage("last_mms.html", data);
                }

                case "/config": {
                    if (method == Method.POST) {
                        try {
                            myPreferences.setTrustedNumber(URLDecoder.decode(postData.get("trustedNumber"), StandardCharsets.UTF_8.name()));
                        } catch (UnsupportedEncodingException e) {
                            Sentry.captureException(e);
                        }
                        return redirect("/");
                    } else {
                        Map<String, Object> data = new HashMap<>();
                        data.put("trustedNumber", myPreferences.getTrustedNumber());
                        return loadPage("config.html", data);
                    }
                }

                case "/http_logs.json": {
                    try {
                        return newFixedLengthResponse(Response.Status.OK, "application/json", getAllHttpRequests().toString(4));
                    } catch (JSONException e) {
                        System.out.println("http logs - get error");
                        return newFixedLengthResponse(Response.Status.OK, "application/json", new JSONArray().toString());
                    }
                }

                default:
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html", "Page not found");
            }
        } else {
            String password = postData.get("password");
            if (password != null && password.equals(adminPassword)) {
                // Tworzymy wartość ciasteczka
                String cookieValue = adminPassword; //todo - no tu raczej sesja powinna byc czy cos

                String setCookieValue = String.format("lci-session=%s; max-age=%s", cookieValue, "2592000");

                Response response = redirect("/");
                response.addHeader("Set-cookie", setCookieValue);
                return response;
            } else {
                // Jeśli hasło nie jest poprawne, wyświetl formularz do wprowadzenia hasła
                Map<String, Object> data = new HashMap<>();
                if(method == Method.POST) {
                    data.put("error", "Wrong password");
                }
                return loadPage("login.html", data);
            }
        }

    }

    private String jsonData() {
        return jsonData(0);
    }
    private String jsonData(int indentSpaces) {

        JSONObject jsonObject = new JSONObject() {{
            try {
                put("socketIsConnected", socketClient.isConnected());
                put("socketPrivateChannelIsSubscribed", socketClient.privateChannelIsSubscribed());
                put("deviceName", Device.getDeviceName());
                put("deviceId", Device.getDeviceId());
                put("loginTokenExist", myPreferences.isLoginTokenExist());
                put("signalStrength", NetworkSignalStrengthChecker.getSignalStrength());

                put("version", getVersionJson());

                put("controllerUrl", myPreferences.getHostUrl());

                put("systemInfo", systemInfo.getJsonDetails());

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                put("currentDate", sdf.format(calendar.getTime()) );

                put("ping", new JSONObject(){{
                    put("deviceId", PingServer.deviceId);
                    put("isLoggedIn", PingServer.isLoggedIn);
                }});

                put("deviceUuid", myPreferences.getDeviceUuid() );

                put("generatedDeviceName", String.format("%s %s", systemInfo.getManufacturer(), systemInfo.getModel()));
            } catch (JSONException e) {
                Sentry.captureException(e);
            }
        }};

        if(indentSpaces > 0) {
            try {
                return jsonObject.toString(indentSpaces);
            } catch (JSONException ignored) {
            }
        }

        return jsonObject.toString();
    }

    private Response loadPage(String filename) {
        Map<String, Object> data = new HashMap<>();
        return loadPage(filename, data);
    }


    private Response loadPage(String filename, Map<String, Object> data) {
        try {
            InputStream fileStream = ApplicationContextProvider.getApplicationContext().getAssets().open(filename);
            InputStreamReader reader = new InputStreamReader(fileStream, StandardCharsets.UTF_8);

            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache mustache = mf.compile(reader, "mytemplate");

//            data.put("error", "Błąd logowania");

            StringWriter writer = new StringWriter();
            mustache.execute(writer, data).flush();

//            System.out.println("Wczytywanie template: " + writer.toString());

            Response response = newFixedLengthResponse(Response.Status.OK, "text/html", getTemplate(writer.toString()));
            response.addHeader("Author", "Let's Code It - www.letscode.it");
            return response;
        } catch (IOException e) {
            Sentry.captureException(e);
            e.printStackTrace();
            return newFixedLengthResponse("Błąd wczytywania pliku: " + e.getMessage());
        }
    }

    /**
     * Load head, footer, etc.
     */
    private String getTemplate(String content) throws IOException {
        InputStream fileStream = ApplicationContextProvider.getApplicationContext().getAssets().open("template.html");
        InputStreamReader reader = new InputStreamReader(fileStream, StandardCharsets.UTF_8);

        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(reader, "template");

        Map<String, String> scope = new HashMap<>();
        scope.put("content", content);
        scope.put("app_data", this.jsonData());

        StringWriter writer = new StringWriter();
        mustache.execute(writer, scope).flush();

        return writer.toString();
    }

    private Response serveStaticFile(String filename, String mime) {
        try {
            InputStream fileStream = ApplicationContextProvider.getApplicationContext().getAssets().open(filename);
            return newChunkedResponse(Response.Status.OK, mime, fileStream);
        } catch (IOException e) {
            Sentry.captureException(e);
            e.printStackTrace();
            return newFixedLengthResponse("Error during load static file: " + e.getMessage());
        }
    }

    private Response redirect(String url) {
        Response response = newFixedLengthResponse(Response.Status.REDIRECT, "text/plain", "");
        response.addHeader("Location", url);
        return response;
    }

}
