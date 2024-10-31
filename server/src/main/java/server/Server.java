package server;

import com.google.gson.Gson;
import dataaccess.*;
import model.UserData;
import server.request.CreateGameRequest;
import server.request.JoinGameRequest;
import server.request.LoginRequest;
import service.ClearService;
import service.GameService;
import service.ServiceException;
import service.UserService;
import spark.*;

public class Server {
    private final Gson serializer = new Gson();
    private UserDAO userDataAccess;
    private AuthDAO authDataAccess;
    private GameDAO gameDataAccess;
    private UserService userService;
    private GameService gameService;
    private ClearService clearService;

    public Server() {
        try {
            userDataAccess = new SQLUserDAO();
            authDataAccess = new SQLAuthDAO();
            gameDataAccess = new SQLGameDAO();
        } catch (DataAccessException ex) {
            userDataAccess = new MemoryUserDAO();
            authDataAccess = new MemoryAuthDAO();
            gameDataAccess = new MemoryGameDAO();
        }
        userService = new UserService(userDataAccess, authDataAccess);
        gameService = new GameService(gameDataAccess, authDataAccess);
        clearService = new ClearService(gameDataAccess, userDataAccess, authDataAccess);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", this::deleteData);
        Spark.post("/user", this::registerUser);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.exception(ServiceException.class, this::serviceExceptionHandler);
        Spark.exception(DataAccessException.class, this::dataAccessExceptionHandler);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private void serviceExceptionHandler(ServiceException e, Request req, Response res) {
        res.status(e.getStatusCode());
        res.body(serializer.toJson(new ExceptionMessage(e.getMessage())));
    }

    private void dataAccessExceptionHandler(DataAccessException e, Request req, Response res) {
        res.status(0);
        res.body(serializer.toJson(new ExceptionMessage(e.getMessage())));
    }

    private String deleteData(Request req, Response res) throws DataAccessException {
        clearService.clear();
        res.status(200);
        return "";
    }

    private String registerUser(Request req, Response res) throws Exception {
        UserData user = serializer.fromJson(req.body(), UserData.class);
        return serializer.toJson(userService.register(user));
    }

    private String login(Request req, Response res) throws Exception {
        LoginRequest loginRequest = serializer.fromJson(req.body(), LoginRequest.class);
        return serializer.toJson(userService.login(loginRequest));
    }

    private String logout(Request req, Response res) throws Exception {
        String authToken = req.headers("authorization");
        userService.logout(authToken);
        return "";
    }

    private String listGames(Request req, Response res) throws Exception {
        String authToken = req.headers("authorization");
        return serializer.toJson(gameService.listGames(authToken));
    }

    private String createGame(Request req, Response res) throws Exception {
        String authToken = req.headers("authorization");
        String gameName = serializer.fromJson(req.body(), CreateGameRequest.class).gameName();
        return serializer.toJson(gameService.createGame(gameName, authToken));
    }

    private String joinGame(Request req, Response res) throws Exception {
        String authToken = req.headers("authorization");
        JoinGameRequest joinGameRequest = serializer.fromJson(req.body(), JoinGameRequest.class);
        gameService.joinGame(joinGameRequest, authToken);
        return "";
    }
}