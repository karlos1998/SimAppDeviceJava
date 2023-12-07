package it.letscode.simappdevice;

import static java.security.AccessController.getContext;

import android.content.Context;

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.io.InputStream;


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
            return serveStaticFile("login.html", "text/css");
        }

    }

    private Response serveStaticFile(String filename, String mime) {
        try {
            InputStream fileStream = context.getAssets().open(filename); // Pobierz plik z zasobów aplikacji
            return newChunkedResponse(Response.Status.OK, mime, fileStream);
        } catch (IOException e) {
            e.printStackTrace();
            return newFixedLengthResponse("Błąd wczytywania pliku: " + e.getMessage());
        }
    }

}
