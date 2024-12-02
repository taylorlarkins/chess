package ui;

import websocket.NotificationHandler;
import websocket.messages.ServerMessage;

import java.util.Scanner;

import static ui.EscapeSequences.RESET_TEXT_COLOR;
import static ui.EscapeSequences.SET_TEXT_COLOR_RED;
import static websocket.messages.ServerMessage.ServerMessageType.NOTIFICATION;

public class Repl implements NotificationHandler {
    private final ChessClient client;

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl, this);
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
                var msg = e.getMessage();
                System.out.print(SET_TEXT_COLOR_RED + msg + RESET_TEXT_COLOR);
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

    @Override
    public void notify(ServerMessage notification) {
        if (notification.getServerMessageType() == NOTIFICATION) {
            System.out.println(notification.getMessage());
        }
    }
}
