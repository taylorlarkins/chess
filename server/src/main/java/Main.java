import dataaccess.DataAccessObject;
import dataaccess.MemoryDAO;
import server.Server;
import service.ClearService;
import service.GameService;
import service.UserService;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();
        int port = server.run(8080);
        DataAccessObject dataAccess = new MemoryDAO();
        UserService userService = new UserService(dataAccess);
        GameService gameService = new GameService(dataAccess);
        ClearService clearService = new ClearService(dataAccess);
        System.out.println("♕ 240 Chess Server running on port " + port);
    }
}