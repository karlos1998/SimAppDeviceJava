package it.letscode.simappdevice;

import static java.security.AccessController.getContext;

import android.annotation.SuppressLint;
import android.content.Context;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import fi.iki.elonen.NanoHTTPD;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class MyHTTPServer  extends NanoHTTPD {
    private Context context;
    private final String adminPassword = "lci123password";

    private SmsSender smsSender;

    public MyHTTPServer(Context context, int port) {
        super(port);
        this.context = context;

        this.smsSender = new SmsSender();
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
                    smsSender.sendSms(number, text);
                    return redirect("/");
                } else {
                    return loadPage("send_sms.html");
                }
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
                return loadPage("login.html");
            }
        }

    }

    private Response loadPage(String filename) {
        try {
            InputStream fileStream = context.getAssets().open(filename);
            InputStreamReader reader = new InputStreamReader(fileStream, StandardCharsets.UTF_8);

            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache mustache = mf.compile(reader, "mytemplate");

            Map<String, String> context = new HashMap<>();
            context.put("error", "Błąd logowania");

            StringWriter writer = new StringWriter();
            mustache.execute(writer, context).flush();

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
