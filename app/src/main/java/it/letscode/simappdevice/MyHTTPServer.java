package it.letscode.simappdevice;

import static java.security.AccessController.getContext;

import android.annotation.SuppressLint;
import android.content.Context;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import org.json.JSONException;
import org.json.JSONObject;

import fi.iki.elonen.NanoHTTPD;

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
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
public class MyHTTPServer  extends NanoHTTPD {
    private Context context;
    private final String adminPassword = "lci123password";

    private SmsSender smsSender;
    private SmsReader smsReader;

    private Permissions permissions;

    private MyPreferences myPreferences;

    ControllerHttpGateway controllerHttpGateway;

    SocketClient socketClient;

    public MyHTTPServer(Context context, int port) {
        super(port);
        this.context = context;

        this.smsSender = new SmsSender(context);
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
                }
            } catch (IOException e) {
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

        Map<String, String> params = new HashMap<>();
        try {
            session.parseBody(params);
            System.out.println("get query: " + session.getQueryParameterString());
        } catch (IOException | ResponseException e) {
            e.printStackTrace();
        }



        if(isLogged) {
            if(uri.equals("/")) {
                return loadPage("index.html");
            } else if(uri.equals("/send_sms")) {
                if(method == Method.POST) {
                    String number = postData.get("number");
                    String text = postData.get("text");
                    try {
                        text = URLDecoder.decode(text, StandardCharsets.UTF_8.name());
                    } catch (UnsupportedEncodingException e) {
                        System.out.print("Nie udało się dekodować tekstu z formularza wysylania wiadommosci na Utf8 - wyslam tekst przed dekodowaniem.");
                    }
                    smsSender.sendSms(number, text);
                    return redirect("/");
                } else {
                    return loadPage("send_sms.html");
                }
            } else if(uri.equals("/sms_list")) {
                Map<String, Object> data = new HashMap<>();
                data.put("messages", smsReader.getLastMessages(10, context));
                return loadPage("sms_list.html", data);
            } else if(uri.equals("/debug")) {

                // Pobranie danych o uprawnieniach
                Map<String, Boolean> permissions = this.permissions.getAllPermissions(context.getPackageManager());

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
            } else if(uri.equals("/controller_configuration")) {
                if(method == Method.POST) {
                    String url = postData.get("url");
                    try {
                        url = URLDecoder.decode(url, StandardCharsets.UTF_8.name());
                    } catch (UnsupportedEncodingException ignored) {

                    }
                    assert url != null;
                    myPreferences.setHostUrl(url.startsWith("http") ? url : ("http://" + url));

                    String token = postData.get("token");
                    myPreferences.setLoginToken(token);
                    controllerHttpGateway.login(token); //53614ad765993b47eec5cdee5239f8a4aa4c2e55

                    return redirect("/");
                } else {
                    Map<String, Object> data = new HashMap<>();
                    data.put("url", myPreferences.getHostUrl());
                    data.put("token", myPreferences.getLoginToken());
                    return loadPage("controller_configuration.html", data);
                }
            } else if(uri.equals("/data.json")) {
                JSONObject json = new JSONObject() {{
                    try {
                        put("socketIsConnected", socketClient.isConnected());
                    } catch (JSONException ignore) {}
                }};
                return newFixedLengthResponse(Response.Status.OK, "application/json", json.toString());
            } else {
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

    private Response loadPage(String filename) {
        Map<String, Object> data = new HashMap<>();
        return loadPage(filename, data);
    }


    private Response loadPage(String filename, Map<String, Object> data) {
        try {
            InputStream fileStream = context.getAssets().open(filename);
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
            e.printStackTrace();
            return newFixedLengthResponse("Błąd wczytywania pliku: " + e.getMessage());
        }
    }

    /**
     * Load head, footer, etc.
     */
    private String getTemplate(String content) throws IOException {
        InputStream fileStream = context.getAssets().open("template.html");
        InputStreamReader reader = new InputStreamReader(fileStream, StandardCharsets.UTF_8);

        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(reader, "template");

        Map<String, String> context = new HashMap<>();
        context.put("content", content);

        StringWriter writer = new StringWriter();
        mustache.execute(writer, context).flush();

        return writer.toString();
    }

    private Response serveStaticFile(String filename, String mime) {
        try {
            InputStream fileStream = context.getAssets().open(filename);
            return newChunkedResponse(Response.Status.OK, mime, fileStream);
        } catch (IOException e) {
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
