package it.letscode.simappdevice;

import static java.security.AccessController.getContext;

import android.content.Context;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class MyHTTPServer  extends NanoHTTPD {
    private Context context;

    public MyHTTPServer(Context context, int port) {
        super(port);
        this.context = context;
    }

    @Override
    public Response serve(IHTTPSession session) {
        // Zbierz dane wysłane przez klienta (przeglądarkę)
        Method method = session.getMethod();
        String uri = session.getUri();
        String msg = "<html><body><h1>Wymagane hasło</h1>";

        // Sprawdź czy żądanie zawiera parametr 'password'
        String password = session.getParms().get("password");

        if (password != null && password.equals("TwojeHaslo")) {
            // Jeśli hasło jest poprawne, udziel dostępu
            return newFixedLengthResponse(msg);
            // Tutaj możesz zwrócić żądane zasoby dla użytkownika
        } else {
            // Jeśli hasło nie jest poprawne, wyświetl formularz do wprowadzenia hasła
            return serveStaticFile("login.html", "text/html");
        }

    }

    private Response serveStaticFile(String filename, String mime) {
        try {
            InputStream fileStream = context.getAssets().open(filename);
            InputStreamReader reader = new InputStreamReader(fileStream, StandardCharsets.UTF_8);

            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache mustache = mf.compile(reader, "mytemplate");

            Map<String, String> context = new HashMap<>();
            context.put("error", "Błąd logowania");

            StringWriter writer = new StringWriter();
            mustache.execute(writer, context).flush();

            System.out.println("Wczytywanie template: " + writer.toString());

            return newFixedLengthResponse(Response.Status.OK, mime, writer.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return newFixedLengthResponse("Błąd wczytywania pliku: " + e.getMessage());
        }
    }

}
