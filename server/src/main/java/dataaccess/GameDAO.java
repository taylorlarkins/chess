package dataaccess;

import model.GameData;

public interface GameDAO {
    void clear() throws DataAccessException;

    int createGame(String name) throws DataAccessException;

    GameData getGame(int gameID) throws DataAccessException;

    GameData[] listGames() throws DataAccessException;

    GameData updateGame() throws DataAccessException;
}
