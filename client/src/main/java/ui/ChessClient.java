package ui;

import chess.ChessBoard;
import chess.ChessPiece;
import chess.ChessPosition;
import model.AuthData;
import model.GameData;
import model.UserData;
import request.CreateGameRequest;
import request.JoinGameRequest;
import request.LoginRequest;

import java.util.Arrays;
import java.util.HashMap;

import static ui.EscapeSequences.*;

public class ChessClient {
    private AuthData user = null;
    private final ServerFacade server;
    private State state = State.LOGGEDOUT;
    private final HashMap<Integer, Integer> gameMap;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        gameMap = new HashMap<>();
    }

    public State getState() {
        return state;
    }

    public String eval(String input) throws ClientException {
        String[] tokens = input.split(" ");
        String cmd = (tokens.length > 0) ? tokens[0].toLowerCase() : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch (cmd) {
            case "quit" -> "Goodbye!";
            case "register" -> register(params);
            case "login" -> login(params);
            case "create" -> create(params);
            case "list" -> list();
            case "join" -> join(params);
            case "observe" -> observe(params);
            case "logout" -> logout();
            default -> help();
        };
    }

    public String register(String... params) throws ClientException {
        if (params.length == 3) {
            server.register(new UserData(params[0], params[1], params[2]));
            user = server.login(new LoginRequest(params[0], params[1]));
            state = State.LOGGEDIN;
            return String.format("%s has been logged in!", user.username());
        }
        throw new ClientException(400, "Expected: <username> <password> <email>");
    }

    public String login(String... params) throws ClientException {
        if (params.length == 2) {
            user = server.login(new LoginRequest(params[0], params[1]));
            state = State.LOGGEDIN;
            return String.format("%s has been logged in!", user.username());
        }
        throw new ClientException(400, "Expected: <username> <password>");
    }

    public String create(String... params) throws ClientException {
        assertLoggedIn();
        if (params.length == 1) {
            server.createGame(new CreateGameRequest(params[0]), user.authToken());
            return String.format("A game titled \"%s\" has been created.", params[0]);
        }
        throw new ClientException(400, "Expected: <game name>");
    }

    public String list(String... params) throws ClientException {
        assertLoggedIn();
        GameData[] games = server.listGames(user.authToken());
        updateGameMap();
        if (games.length == 0) {
            return "No existing games!";
        }
        StringBuilder result = new StringBuilder();
        int i = 1;
        for (GameData game : games) {
            String whiteUsername = game.whiteUsername();
            String blackUsername = game.blackUsername();
            if (whiteUsername == null) {
                whiteUsername = "None";
            }
            if (blackUsername == null) {
                blackUsername = "None";
            }
            result.append(i).append(": ")
                    .append(game.gameName())
                    .append(" - White: ").append(whiteUsername)
                    .append(" | Black: ").append(blackUsername)
                    .append("\n");
            i++;
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }

    public String join(String... params) throws ClientException {
        assertLoggedIn();
        if (params.length == 2 && (params[1].equals("WHITE") || params[1].equals("BLACK"))) {
            updateGameMap();
            int gameID = gameMap.get(Integer.parseInt(params[0]));
            server.joinGame(new JoinGameRequest(params[1], gameID), user.authToken());
            printGame();
            return "";
        }
        throw new ClientException(400, "Expected: <id> <BLACK|WHITE>");
    }

    public String observe(String... params) throws ClientException {
        assertLoggedIn();
        if (params.length == 1) {
            printGame();
        }
        throw new ClientException(400, "Expected: <id>");
    }

    public String logout(String... params) throws ClientException {
        assertLoggedIn();
        state = State.LOGGEDOUT;
        server.logout(user.authToken());
        String result = String.format("%s has been logged out.", user.username());
        user = null;
        return result;
    }

    public String help() {
        if (state == State.LOGGEDOUT) {
            return """
                    register <username> <password> <email> - creates an account
                    login <username> <password> - log into your account
                    quit - exits the application
                    help - lists possible commands
                    """;
        }
        return """
                create <name> - creates a new game
                list - lists games
                join <id> <BLACK|WHITE> - join a game as the specified team
                observe <id> -  observe a game
                logout - logout of your account
                quit - exits the application
                help - lists possible commands
                """;
    }

    private void updateGameMap() throws ClientException {
        GameData[] games = server.listGames(user.authToken());
        gameMap.clear();
        for (int i = 1; i <= games.length; i++) {
            gameMap.put(i, games[i - 1].gameID());
        }
    }

    private void assertLoggedIn() throws ClientException {
        if (state == State.LOGGEDOUT) {
            throw new ClientException(400, "You must sign in first!");
        }
    }

    private void printGame() {
        ChessBoard board = new ChessBoard();
        String[] checkerColors = {SET_BG_COLOR_WHITE, SET_BG_COLOR_BLACK};
        board.resetBoard();
        System.out.print(SET_TEXT_BOLD);
        System.out.print(SET_BG_COLOR_LIGHT_GREY);
        System.out.print("  a  b  c  d  e  f  g  h  \n");
        for (int row = 1; row <= 8; row++) {
            System.out.print(SET_BG_COLOR_LIGHT_GREY);
            System.out.print(" " + row + " ");
            for (int col = 1; col <= 8; col++) {
                System.out.print(checkerColors[col % 2]);
                System.out.print(" " + getPiece(board, row, col) + " ");
            }
            System.out.print(SET_BG_COLOR_LIGHT_GREY);
            System.out.print(" " + row + " \n");
        }
    }

    private String getPiece(ChessBoard board, int row, int col) {
        ChessPiece piece = board.getPiece(new ChessPosition(row, col));
        if (piece == null) {
            return " ";
        }
        return piece.toString();
    }
}
