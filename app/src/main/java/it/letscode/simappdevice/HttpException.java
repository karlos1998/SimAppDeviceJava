package it.letscode.simappdevice;

import java.io.IOException;

public class HttpException extends IOException {
    private final int statusCode;
    private final String responseBody;

    public HttpException(int statusCode, String responseBody) {
        super("HTTP Error: " + statusCode);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
