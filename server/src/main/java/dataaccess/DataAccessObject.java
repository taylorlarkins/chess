package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

public interface DataAccessObject {
    void clear();

    UserData createUser(UserData user);

    UserData getUser(UserData user);

    GameData createGame(GameData game);

    GameData getGame(GameData game);

    GameData[] listGames();

    GameData updateGame();

    AuthData createAuth();

    AuthData getAuth(AuthData auth);

    void deleteAuth(AuthData auth);
}
