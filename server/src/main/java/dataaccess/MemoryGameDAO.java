package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.HashMap;

public class MemoryGameDAO implements GameDAO {
    private int nextId = 1;
    private final HashMap<Integer, GameData> gameDataTable = new HashMap<>();

    @Override
    public void clear() throws DataAccessException {
        gameDataTable.clear();
    }

    @Override
    public int createGame(String name) {
        GameData game = new GameData(nextId++, null, null, name, new ChessGame());
        gameDataTable.put(game.gameID(), game);
        return game.gameID();
    }

    @Override
    public GameData getGame(int gameID) {
        return gameDataTable.get(gameID);
    }

    @Override
    public GameData[] listGames() {
        GameData[] gameList = new GameData[gameDataTable.size()];
        int i = 0;
        for (GameData game : gameDataTable.values()) {
            gameList[i++] = game;
        }
        return gameList;
    }

    @Override
    public void updateGame(GameData updatedGame) {
        gameDataTable.put(updatedGame.gameID(), updatedGame);
    }

    @Override
    public int getSize() {
        return gameDataTable.size();
    }
}
