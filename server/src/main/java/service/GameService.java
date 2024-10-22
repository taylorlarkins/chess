package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.GameData;
import server.CreateGameResponse;
import server.JoinGameRequest;
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

    public void joinGame(JoinGameRequest request, String authToken) throws Exception {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new ServiceException(401, "Error: unauthorized");
        }
        GameData game = gameDAO.getGame(request.gameID());
        String requestedColor = request.playerColor();
        if (game == null || requestedColor == null) {
            throw new ServiceException(400, "Error: bad request");
        }
        if (requestedColor.equals("WHITE") && game.whiteUsername() != null
                || requestedColor.equals("BLACK") && game.blackUsername() != null) {
            throw new ServiceException(403, "Error: already taken");
        }
        if (requestedColor.equals("WHITE")) {
            gameDAO.updateGame(new GameData(
                    game.gameID(),
                    auth.username(),
                    game.blackUsername(),
                    game.gameName(),
                    game.game())
            );
        } else {
            gameDAO.updateGame(new GameData(
                    game.gameID(),
                    game.whiteUsername(),
                    auth.username(),
                    game.gameName(),
                    game.game())
            );
        }
    }
}