package ui;

import chess.ChessBoard;
import model.AuthData;
import model.GameData;
import model.UserData;
import request.CreateGameRequest;
import request.JoinGameRequest;
import request.LoginRequest;

import java.util.Arrays;
import java.util.HashMap;

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
        if (params.length == 1) {
            server.createGame(new CreateGameRequest(params[0]), user.authToken());
            return String.format("A game titled \"%s\" has been created.", params[0]);
        }
        throw new ClientException(400, "Expected: <game name>");
    }

    public String list(String... params) throws ClientException {
        GameData[] games = server.listGames(user.authToken());
        gameMap.clear();
        if (games.length == 0) {
            return "No existing games!";
        }
        StringBuilder result = new StringBuilder();
        int i = 1;
        for (GameData game : games) {
            gameMap.put(i, game.gameID());
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
        if (params.length == 2 && (params[1].equals("WHITE") || params[1].equals("BLACK"))) {
            int gameID = gameMap.get(Integer.parseInt(params[0]));
            server.joinGame(new JoinGameRequest(params[1], gameID), user.authToken());
            printGame();
            return "";
        }
        throw new ClientException(400, "Expected: <id> <BLACK|WHITE>");
    }

    public String observe(String... params) throws ClientException {
        return "Not implemented";
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

    private void assertLoggedIn() throws ClientException {
        if (state == State.LOGGEDOUT) {
            throw new ClientException(400, "You must sign in first!");
        }
    }

    private void printGame() {
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        System.out.print(board);
    }
}
