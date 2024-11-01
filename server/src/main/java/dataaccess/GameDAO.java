package dataaccess;

import model.GameData;

public interface GameDAO {
    void clear() throws DataAccessException;

    int addGame(GameData game) throws DataAccessException;

    GameData getGame(int gameID) throws DataAccessException;

    GameData[] listGames() throws DataAccessException;

    void updateGame(GameData updatedGame) throws DataAccessException;

    int getSize() throws DataAccessException;
}