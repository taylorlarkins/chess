package ui;

import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.SQLGameDAO;
import model.AuthData;
import model.GameData;
import model.UserData;
import request.CreateGameRequest;
import request.JoinGameRequest;
import request.LoginRequest;
import websocket.NotificationHandler;
import websocket.WebSocketFacade;

import java.util.Arrays;
import java.util.HashMap;

import static ui.Role.OBSERVER;
import static ui.Role.WHITE_PLAYER;
import static ui.Role.BLACK_PLAYER;
import static ui.State.*;

public class ChessClient {
    private AuthData user = null;
    private final String serverUrl;
    private final ServerFacade server;
    private WebSocketFacade ws;
    private final NotificationHandler notificationHandler;
    private State state = LOGGEDOUT;
    private Role role = null;
    private Integer currentGameID = null;
    private GamePrinter gamePrinter;
    private final HashMap<Integer, Integer> gameMap;
    private GameDAO gameDAO;


    public ChessClient(String serverUrl, NotificationHandler notificationHandler) {
        this.serverUrl = serverUrl;
        this.notificationHandler = notificationHandler;
        server = new ServerFacade(this.serverUrl);
        gameMap = new HashMap<>();
        gamePrinter = new GamePrinter();
        try {
            gameDAO = new SQLGameDAO();
        } catch (DataAccessException ex) {
            gameDAO = new MemoryGameDAO();
        }
    }

    public State getState() {
        return state;
    }

    public Role getRole() {
        return role;
    }

    public String eval(String input) throws ClientException {
        String[] tokens = input.split(" ");
        String cmd = (tokens.length > 0) ? tokens[0].toLowerCase() : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch (cmd) {
            case "quit" -> quit();
            case "register" -> register(params);
            case "login" -> login(params);
            case "create" -> create(params);
            case "list" -> list();
            case "join" -> join(params);
            case "observe" -> observe(params);
            case "logout" -> logout();
            case "redraw" -> redraw();
            case "leave" -> leave();
            case "move" -> move(params);
            case "resign" -> resign();
            case "highlight" -> highlight(params);
            default -> help();
        };
    }

    public String quit() throws ClientException {
        assertLoggedOut();
        return "Goodbye!";
    }

    public String register(String... params) throws ClientException {
        if (params.length == 3) {
            server.register(new UserData(params[0], params[1], params[2]));
            user = server.login(new LoginRequest(params[0], params[1]));
            state = LOGGEDIN;
            return String.format("%s has been logged in!", user.username());
        }
        throw new ClientException(400, "Expected: <username> <password> <email>");
    }

    public String login(String... params) throws ClientException {
        if (params.length == 2) {
            user = server.login(new LoginRequest(params[0], params[1]));
            state = LOGGEDIN;
            return String.format("%s has been logged in!", user.username());
        }
        throw new ClientException(400, "Expected: <username> <password>");
    }

    public String create(String... params) throws ClientException {
        assertLoggedIn();
        if (params.length >= 1) {
            StringBuilder gameName = new StringBuilder();
            for (String word : params) {
                gameName.append(word).append(" ");
            }
            gameName.deleteCharAt(gameName.length() - 1);
            server.createGame(new CreateGameRequest(gameName.toString()), user.authToken());
            return String.format("A game titled \"%s\" has been created.", gameName);
        }
        throw new ClientException(400, "Expected: <game name>");
    }

    public String list() throws ClientException {
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
        if (params.length == 2 &&
                (params[1].equalsIgnoreCase("WHITE") || params[1].equalsIgnoreCase("BLACK"))) {
            updateGameMap();
            int gameID = getGameID(params[0]);
            server.joinGame(new JoinGameRequest(params[1], gameID), user.authToken());
            ws = new WebSocketFacade(serverUrl, notificationHandler);
            state = INGAME;
            ws.sendConnect(user.authToken(), gameID);
            if (params[1].equalsIgnoreCase("WHITE")) {
                role = WHITE_PLAYER;
            } else {
                role = BLACK_PLAYER;
            }
            currentGameID = gameID;
            return "";
        }
        throw new ClientException(400, "Expected: <id> <BLACK|WHITE>");
    }

    public String observe(String... params) throws ClientException {
        assertLoggedIn();
        if (params.length == 1) {
            updateGameMap();
            int gameID = getGameID(params[0]);
            ws = new WebSocketFacade(serverUrl, notificationHandler);
            state = INGAME;
            role = OBSERVER;
            ws.sendConnect(user.authToken(), gameID);
            currentGameID = gameID;
            return "";
        } else {
            throw new ClientException(400, "Expected: <id>");
        }
    }

    public String redraw() throws ClientException {
        assertInGame();
        try {
            gamePrinter.printGame(gameDAO.getGame(currentGameID).game(), role, null);
            return "";
        } catch (DataAccessException ex) {
            throw new ClientException(500, "Unable to retrieve board. Try again later.");
        }
    }

    public String leave() throws ClientException {
        assertInGame();
        ws.sendLeave(user.authToken(), currentGameID);
        state = LOGGEDIN;
        role = null;
        currentGameID = null;
        return "";
    }

    public String move(String... params) throws ClientException {
        assertInGame();
        assertPlayer();
        if (params.length == 2 || params.length == 3) {
            ChessPosition start = convertNotationToPosition(params[0]);
            ChessPosition end = convertNotationToPosition(params[1]);
            ChessPiece.PieceType promotion = null;
            if (params.length == 3) {
                promotion = getPromotion(params[2]);
            }
            ws.sendMakeMove(user.authToken(), currentGameID, new ChessMove(start, end, promotion));
            return "";
        } else {
            throw new ClientException(400, "Expected: <start square> <end square>");
        }
    }

    public String resign() throws ClientException {
        assertInGame();
        assertPlayer();
        ws.sendResign(user.authToken(), currentGameID);
        return "";
    }

    public String highlight(String... params) throws ClientException {
        assertInGame();
        if (params.length == 1) {
            try {
                ChessPosition positionInQuestion = convertNotationToPosition(params[0]);
                gamePrinter.printGame(gameDAO.getGame(currentGameID).game(), role, positionInQuestion);
                return "";
            } catch (DataAccessException ex) {
                throw new ClientException(500, "Unable to retrieve board. Try again later.");
            }
        } else {
            throw new ClientException(400, "Expected: <id>");
        }
    }

    public String logout() throws ClientException {
        assertLoggedIn();
        state = LOGGEDOUT;
        server.logout(user.authToken());
        String result = String.format("%s has been logged out.", user.username());
        user = null;
        return result;
    }

    public String help() {
        if (state == LOGGEDOUT) {
            return """
                    register <username> <password> <email> - creates an account
                    login <username> <password> - log into your account
                    quit - exits the application
                    help - lists possible commands
                    """;
        }
        if (state == LOGGEDIN) {
            return """
                    create <name> - creates a new game
                    list - lists games
                    join <id> <BLACK|WHITE> - join a game as the specified team
                    observe <id> -  observe a game
                    logout - logout of your account
                    help - lists possible commands
                    """;
        }
        return """
                redraw - redraws the game board
                leave - exit the game
                move <start square> <end square> <promotion: Q|R|B|N> - move a piece
                resign - forfeit the game
                highlight <start square> - highlights legal moves
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

    private ChessPosition convertNotationToPosition(String notation) throws ClientException {
        char fileChar = Character.toLowerCase(notation.charAt(0));
        int file = switch (fileChar) {
            case 'a' -> 1;
            case 'b' -> 2;
            case 'c' -> 3;
            case 'd' -> 4;
            case 'e' -> 5;
            case 'f' -> 6;
            case 'g' -> 7;
            case 'h' -> 8;
            default -> throw new ClientException(400, "Expected: <start square> <end square>");
        };
        int rank = notation.charAt(1) - '0';
        return new ChessPosition(rank, file);
    }

    private ChessPiece.PieceType getPromotion(String option) throws ClientException {
        char proposal = Character.toUpperCase(option.charAt(0));
        return switch (proposal) {
            case 'Q' -> ChessPiece.PieceType.QUEEN;
            case 'R' -> ChessPiece.PieceType.ROOK;
            case 'N' -> ChessPiece.PieceType.KNIGHT;
            case 'B' -> ChessPiece.PieceType.BISHOP;
            default -> throw new ClientException(400, "Expected: <start square> <end square> <promotion: Q|R|B|N>");
        };
    }

    private int getGameID(String input) throws ClientException {
        Integer gameID;
        try {
            int inputID = Integer.parseInt(input);
            gameID = gameMap.get(inputID);
        } catch (NumberFormatException ex) {
            throw new ClientException(400, "Invalid game ID!");
        }
        if (gameID == null) {
            throw new ClientException(400, "Invalid game ID!");
        }
        return gameID;
    }

    private void assertLoggedIn() throws ClientException {
        if (state != LOGGEDIN) {
            if (state == INGAME) {
                throw new ClientException(400, "You must leave the current game to do that!");
            }
            throw new ClientException(400, "You must sign in first!");
        }
    }

    private void assertLoggedOut() throws ClientException {
        if (state != LOGGEDOUT) {
            throw new ClientException(400, "You must sign out first!");
        }
    }

    private void assertInGame() throws ClientException {
        if (state != INGAME) {
            if (state == LOGGEDIN) {
                throw new ClientException(400, "You must be in a game to do that!");
            }
            throw new ClientException(400, "You must sign in first!");
        }
    }

    private void assertPlayer() throws ClientException {
        if (role != WHITE_PLAYER && role != BLACK_PLAYER) {
            throw new ClientException(400, "You must be a player to do that!");
        }
    }
}