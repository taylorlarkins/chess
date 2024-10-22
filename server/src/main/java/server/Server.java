package server;

import com.google.gson.Gson;
import dataaccess.*;
import model.AuthData;
import model.UserData;
import service.ClearService;
import service.GameService;
import service.ServiceException;
import service.UserService;
import spark.*;

import javax.xml.crypto.Data;
import java.rmi.ServerException;

public class Server {
    private final Gson serializer = new Gson();
    private final UserDAO userDAO = new MemoryUserDAO();
    private final AuthDAO authDAO = new MemoryAuthDAO();
    private final GameDAO gameDAO = new MemoryGameDAO();
    private final UserService userService = new UserService(userDAO, authDAO);
    private final GameService gameService = new GameService(gameDAO, userDAO, authDAO);
    private final ClearService clearService = new ClearService(gameDAO, userDAO, authDAO);


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

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private String serviceExceptionHandler(ServiceException e, Response res) {
        res.status(e.getStatusCode());
        return serializer.toJson(new ExceptionMessage(e.getMessage()));
    }

    private String deleteData(Request req, Response res) throws DataAccessException {
        clearService.clear();
        res.status(200);
        return "";
    }

    private String registerUser(Request req, Response res) throws Exception {
        UserData user = serializer.fromJson(req.body(), UserData.class);
        try {
            return serializer.toJson(userService.register(user));
        } catch (ServiceException e) {
            return serviceExceptionHandler(e, res);
        }
    }

    private String login(Request req, Response res) throws Exception {
        LoginRequest loginRequest = serializer.fromJson(req.body(), LoginRequest.class);
        try {
            return serializer.toJson(userService.login(loginRequest));
        } catch (ServiceException e) {
            return serviceExceptionHandler(e, res);
        }
    }

    private String logout(Request req, Response res) throws Exception {
        String authToken = req.headers("authorization");
        try {
            userService.logout(authToken);
            return "";
        } catch (ServiceException e) {
            return serviceExceptionHandler(e, res);
        }
    }

    private String listGames(Request req, Response res) throws Exception {
        String authToken = req.headers("authorization");
        try {
            return serializer.toJson(gameService.listGames(authToken));
        } catch (ServiceException e) {
            return serviceExceptionHandler(e, res);
        }
    }

    private String createGame(Request req, Response res) throws Exception {
        String authToken = req.headers("authorization");
        String gameName = serializer.fromJson(req.body(), CreateGameRequest.class).gameName();
        try {
            return serializer.toJson(gameService.createGame(gameName, authToken));
        } catch (ServiceException e) {
            return serviceExceptionHandler(e, res);
        }
    }

    private String joinGame(Request req, Response res) throws Exception {
        String authToken = req.headers("authorization");
        JoinGameRequest joinGameRequest = serializer.fromJson(req.body(), JoinGameRequest.class);
        try {
            gameService.joinGame(joinGameRequest, authToken);
            return "";
        } catch (ServiceException e) {
            return serviceExceptionHandler(e, res);
        }
    }
}