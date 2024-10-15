package server;

import com.google.gson.Gson;
import spark.*;

public class Server {

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

    private String deleteData(Request req, Response res) {
        return new Gson().toJson("DELETE /db not implemented");
    }

    private String registerUser(Request req, Response res) {
        return new Gson().toJson("POST /user not implemented");
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
