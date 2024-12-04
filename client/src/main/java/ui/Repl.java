package ui;

import chess.ChessGame;
import websocket.NotificationHandler;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.Scanner;

import static ui.EscapeSequences.*;
import static websocket.messages.ServerMessage.ServerMessageType.LOAD_GAME;
import static websocket.messages.ServerMessage.ServerMessageType.NOTIFICATION;

public class Repl implements NotificationHandler {
    private final ChessClient client;
    private final GamePrinter gamePrinter;

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl, this);
        gamePrinter = new GamePrinter();
    }

    public void run() {
        System.out.println("\uD83C\uDFF0 Welcome to 240 Chess! \uD83D\uDC0E");
        System.out.print(client.help());

        Scanner scanner = new Scanner(System.in);
        String result = "";
        printPrompt();
        while (!"Goodbye!".equals(result)) {
            String line = scanner.nextLine();
            try {
                result = client.eval(line);
                if (result != null) {
                    System.out.print(result);
                    printPrompt();
                }
            } catch (Throwable e) {
                var msg = e.getMessage();
                System.out.print(SET_TEXT_COLOR_RED + msg + RESET_TEXT_COLOR);
                printPrompt();
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
            NotificationMessage notificationMessage = (NotificationMessage) notification;
            System.out.print("\n" + notificationMessage.getMessage());
            printPrompt();
        } else if (notification.getServerMessageType() == LOAD_GAME) {
            LoadGameMessage loadGameMessage = (LoadGameMessage) notification;
            System.out.println();
            ChessGame game = loadGameMessage.getChessGame();
            client.setLastLoadedGame(game);
            gamePrinter.printGame(game, client.getRole(), null);
            printPrompt();
        } else {
            ErrorMessage errorMessage = (ErrorMessage) notification;
            System.out.print(SET_TEXT_COLOR_RED + errorMessage.getErrorMessage() + RESET_TEXT_COLOR);
            printPrompt();
        }
    }
}