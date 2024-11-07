package ui;

import java.util.Scanner;

import static ui.EscapeSequences.SET_TEXT_COLOR_RED;

public class Repl {
    private final ChessClient client;

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl);
    }

    public void run() {
        System.out.println("\uD83C\uDFF0 Welcome to 240 Chess! \uD83D\uDC0E");
        System.out.print(client.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("Goodbye!")) {
            printPrompt();
            String line = scanner.nextLine();
            try {
                result = client.eval(line);
                System.out.print(result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    private void printPrompt() {
        String state;
        if (client.getState() == State.LOGGEDOUT) {
            state = "LOGGED OUT";
        } else {
            state = "LOGGED IN";
        }
        System.out.printf("\n[%s]" + " >>> ", state);
    }
}
