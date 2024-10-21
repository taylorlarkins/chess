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

    private String deleteData(Request req, Response res) throws DataAccessException {
        clearService.clear();
        res.status(200);
        return "";
    }

    private String registerUser(Request req, Response res) throws Exception {
        UserData user = serializer.fromJson(req.body(), UserData.class);
        try {
            AuthData auth = userService.register(user);
            return serializer.toJson(auth);
        } catch (ServiceException e) {
            res.status(e.getStatusCode());
            return serializer.toJson(new ExceptionMessage(e.getMessage()));
        }
    }

    private String login(Request req, Response res) {
        return new Gson().toJson("POST /session not implemented");
    }

    private String logout(Request req, Response res) {
        return new Gson().toJson("DELETE /session not implemented");
    }

    private String listGames(Request req, Response res) {
        return new Gson().toJson("GET /game not implemented");
    }

    private String createGame(Request req, Response res) {
        return new Gson().toJson("POST /game not implemented");
    }

    private String joinGame(Request req, Response res) {
        return new Gson().toJson("PUT /game not implemented");
    }
}