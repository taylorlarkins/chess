package ui;

public class ClientException extends Exception {
    private final int statusCode;

    public ClientException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
