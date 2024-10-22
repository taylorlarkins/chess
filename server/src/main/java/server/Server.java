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
    private final UserDAO userDataAccess = new MemoryUserDAO();
    private final AuthDAO authDataAccess = new MemoryAuthDAO();
    private final GameDAO gameDataAccess = new MemoryGameDAO();
    private final UserService userService = new UserService(userDataAccess, authDataAccess);
    private final GameService gameService = new GameService(gameDataAccess, authDataAccess);
    private final ClearService clearService = new ClearService(gameDataAccess, userDataAccess, authDataAccess);


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
        Spark.exception(ServiceException.class, this::exceptionHandler);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private void exceptionHandler(ServiceException e, Request req, Response res) {
        res.status(e.getStatusCode());
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