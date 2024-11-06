package ui;

public class Repl {
    private final ChessClient client;

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl);
    }

    public void run() {
        System.out.println("\uD83C\uDFF0 Welcome to 240 Chess! \uD83D\uDC0E");
    }

}
