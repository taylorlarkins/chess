package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO {
    private final HashMap<String, AuthData> authDataTable = new HashMap<>();

    @Override
    public void clear() throws DataAccessException {
        authDataTable.clear();
    }

    @Override
    public AuthData createAuth(String username) {
        AuthData auth = new AuthData(generateToken(), username);
        authDataTable.put(auth.authToken(), auth);
        return auth;
    }

    @Override
    public AuthData getAuth(String authToken) {
        return authDataTable.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) {
        authDataTable.remove(authToken);
    }

    @Override
    public int getSize() {
        return authDataTable.size();
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}