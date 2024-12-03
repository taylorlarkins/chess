package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import websocket.NotificationHandler;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.Scanner;

import static ui.EscapeSequences.*;
import static websocket.messages.ServerMessage.ServerMessageType.LOAD_GAME;
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
            ;
            NotificationMessage notificationMessage = (NotificationMessage) notification;
            System.out.print("\n" + notificationMessage.getMessage());
        } else if (notification.getServerMessageType() == LOAD_GAME) {
            LoadGameMessage loadGameMessage = (LoadGameMessage) notification;
            System.out.println();
            ChessGame game = loadGameMessage.getChessGame();
            printGame(game.getBoard(), loadGameMessage.whitePerspective());
        }
    }

    private void printGame(ChessBoard board, boolean whitePerspective) {
        String perspective;
        String fileLabels;
        if (whitePerspective) {
            perspective = SET_TEXT_COLOR_WHITE;
            fileLabels = "    a  b  c  d  e  f  g  h    ";
        } else {
            perspective = SET_TEXT_COLOR_BLACK;
            fileLabels = "    h  g  f  e  d  c  b  a    ";
        }
        System.out.print(perspective + SET_TEXT_BOLD + SET_BG_COLOR_LIGHT_GREY);
        System.out.println(SET_BG_COLOR_LIGHT_GREY + fileLabels + RESET_BG_COLOR);
        printSquaresAndPieces(board, whitePerspective, perspective);
        System.out.print(SET_BG_COLOR_LIGHT_GREY + fileLabels + RESET_BG_COLOR);
        System.out.println(RESET_TEXT_COLOR + RESET_TEXT_BOLD_FAINT);
    }

    private void printSquaresAndPieces(ChessBoard board, boolean whitePerspective, String perspective) {
        String[] checkerColors = {SET_BG_COLOR_BLACK, SET_BG_COLOR_WHITE};
        if (whitePerspective) {
            for (int row = 8; row >= 1; row--) {
                System.out.print(SET_BG_COLOR_LIGHT_GREY + perspective);
                System.out.print(" " + row + " " + RESET_BG_COLOR);
                for (int col = 1; col <= 8; col++) {
                    System.out.print(checkerColors[(col + row) % 2]);
                    System.out.print(" " + getPiece(board, row, col) + " ");
                }
                System.out.println(SET_BG_COLOR_LIGHT_GREY + perspective + " " + row + " " + RESET_BG_COLOR);
            }
        } else {
            for (int row = 1; row <= 8; row++) {
                System.out.print(SET_BG_COLOR_LIGHT_GREY + perspective);
                System.out.print(" " + row + " " + RESET_BG_COLOR);
                for (int col = 8; col >= 1; col--) {
                    System.out.print(checkerColors[(col + row) % 2]);
                    System.out.print(" " + getPiece(board, row, col) + " ");
                }
                System.out.println(SET_BG_COLOR_LIGHT_GREY + perspective + " " + row + " " + RESET_BG_COLOR);
            }
        }
    }

    private String getPiece(ChessBoard board, int row, int col) {
        ChessPiece piece = board.getPiece(new ChessPosition(row, col));
        if (piece == null) {
            return " ";
        }
        String color;
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            color = SET_TEXT_COLOR_BLUE;
        } else {
            color = SET_TEXT_COLOR_RED;
        }
        return color + piece;
    }
}