package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import model.AuthData;
import server.CreateGameResponse;
import server.ListGamesResponse;

public class GameService {
    private GameDAO gameDAO;
    private UserDAO userDAO;
    private AuthDAO authDAO;

    public GameService(GameDAO gameDAO, UserDAO userDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public ListGamesResponse listGames(String authToken) throws Exception {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new ServiceException(401, "Error: unauthorized");
        }
        return new ListGamesResponse(gameDAO.listGames());
    }

    public CreateGameResponse createGame(String gameName, String authToken) throws Exception {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new ServiceException(401, "Error: unauthorized");
        }
        return new CreateGameResponse(gameDAO.createGame(gameName));
    }

    public void joinGame() {

    }
}