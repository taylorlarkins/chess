package dataaccess;

import model.GameData;

public interface GameDAO {
    GameData createGame(GameData game);

    GameData getGame(GameData game);

    GameData[] listGames();

    void updateGame(GameData game);
}
