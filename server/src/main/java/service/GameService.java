package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import response.CreateGameResponse;
import request.JoinGameRequest;
import response.ListGamesResponse;

public class GameService {
    private final GameDAO gameDataAccess;
    private final AuthDAO authDataAccess;

    public GameService(GameDAO gameDataAccess, AuthDAO authDataAccess) {
        this.gameDataAccess = gameDataAccess;
        this.authDataAccess = authDataAccess;
    }

    public ListGamesResponse listGames(String authToken) throws Exception {
        AuthData auth = authDataAccess.getAuth(authToken);
        UserService.authenticate(auth);
        return new ListGamesResponse(gameDataAccess.listGames());
    }

    public CreateGameResponse createGame(String gameName, String authToken) throws Exception {
        AuthData auth = authDataAccess.getAuth(authToken);
        UserService.authenticate(auth);
        GameData game = new GameData(0 /* place holder */, null, null, gameName, new ChessGame());
        return new CreateGameResponse(gameDataAccess.addGame(game));
    }

    public void joinGame(JoinGameRequest request, String authToken) throws Exception {
        AuthData auth = authDataAccess.getAuth(authToken);
        UserService.authenticate(auth);
        GameData game = gameDataAccess.getGame(request.gameID());
        String requestedColor = request.playerColor();
        if (game == null || requestedColor == null) {
            throw new ServiceException(400, "Error: bad request");
        }
        if (requestedColor.equals("WHITE") && game.whiteUsername() != null
                || requestedColor.equals("BLACK") && game.blackUsername() != null) {
            throw new ServiceException(403, "Error: already taken");
        }
        if (requestedColor.equals("WHITE")) {
            gameDataAccess.updateGame(new GameData(
                    game.gameID(),
                    auth.username(),
                    game.blackUsername(),
                    game.gameName(),
                    game.game())
            );
        } else {
            gameDataAccess.updateGame(new GameData(
                    game.gameID(),
                    game.whiteUsername(),
                    auth.username(),
                    game.gameName(),
                    game.game())
            );
        }
    }
}