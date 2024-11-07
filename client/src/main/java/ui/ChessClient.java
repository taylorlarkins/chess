package ui;

import model.AuthData;
import model.UserData;
import server.request.LoginRequest;

import java.util.Arrays;

public class ChessClient {
    private final ServerFacade server;
    private State state = State.LOGGEDOUT;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
    }

    public State getState() {
        return state;
    }

    public String eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
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
        } catch (ClientException ex) {
            return ex.getMessage();
        }
    }

    public String register(String... params) throws ClientException {
        if (params.length == 3) {
            AuthData auth = server.register(new UserData(params[0], params[1], params[2]));
            return String.format("%s has been registered!", auth.username());
        }
        throw new ClientException(400, "Expected: <username> <password> <email>");
    }

    public String login(String... params) throws ClientException {
        if (params.length == 2) {
            AuthData auth = server.login(new LoginRequest(params[0], params[1]));
            return String.format("%s has been logged in!", auth.username());
        }
        throw new ClientException(400, "Expected: <username> <password>");
    }

    public String create(String... params) throws ClientException {
        return "Not implemented";
    }

    public String list(String... params) throws ClientException {
        return "Not implemented";
    }

    public String join(String... params) throws ClientException {
        return "Not implemented";
    }

    public String observe(String... params) throws ClientException {
        return "Not implemented";
    }

    public String logout(String... params) throws ClientException {
        return "Not implemented";
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
                join <id> <black|white> - join a game as the specified team
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
}
