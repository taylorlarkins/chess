package server;

import spark.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.get("/game", this::listGames);
        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private Object listGames(Request req, Response res) {
        return "{ \"games\": [{\"gameID\": 1234, \"whiteUsername\":\"\", \"blackUsername\":\"\", \"gameName:\"\"} ]}";
    }
}
