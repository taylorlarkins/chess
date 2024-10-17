package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.HashMap;

public class MemoryGameDAO implements GameDAO {
    private int nextId = 1;
    private HashMap<Integer, GameData> gameDataTable = new HashMap<>();

    @Override
    public void clear() throws DataAccessException {
        gameDataTable.clear();
    }

    @Override
    public int createGame(String name) throws DataAccessException {
        GameData game = new GameData(nextId++, null, null, name, new ChessGame());
        gameDataTable.put(game.gameID(), game);
        return game.gameID();
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return gameDataTable.get(gameID);
    }

    @Override
    public GameData[] listGames() throws DataAccessException {
        GameData[] gameList = new GameData[gameDataTable.size()];
        int i = 0;
        for (GameData game : gameDataTable.values()) {
            gameList[i++] = game;
        }
        return gameList;
    }

    @Override
    public GameData updateGame() throws DataAccessException {
        return null;
    }
}
