package service;

import dataaccess.DataAccessObject;
import model.AuthData;
import model.GameData;

public class GameService {
    DataAccessObject dataAccess;

    public GameService(DataAccessObject dataAccess) {
        this.dataAccess = dataAccess;
    }

    public GameData[] listGames(AuthData auth) {
        return null;
    }

    public GameData createGame(AuthData auth) {
        return null;
    }

    public void joinGame(AuthData auth) {

    }
}
