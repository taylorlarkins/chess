package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.HashMap;

public class MemoryDAO implements DataAccessObject {
    private HashMap<String, UserData> userDataTable = new HashMap<>();
    private HashMap<String, AuthData> authDataTable = new HashMap<>();
    private HashMap<Integer, GameData> gameDataTable = new HashMap<>();

    public void clear() {
        userDataTable.clear();
        authDataTable.clear();
        gameDataTable.clear();
    }

    public UserData createUser(UserData user) {
        return null;
    }

    public UserData getUser(UserData user) {
        return null;
    }

    public GameData createGame(GameData game) {
        return null;
    }

    public GameData getGame(GameData game) {
        return null;
    }

    public GameData[] listGames() {
        return new GameData[0];
    }

    public GameData updateGame() {
        return null;
    }

    public AuthData createAuth() {
        return null;
    }

    public AuthData getAuth(AuthData auth) {
        return null;
    }

    public void deleteAuth(AuthData auth) {

    }
}
