package dataaccess;

import model.AuthData;

import java.util.HashMap;

public class MemoryAuthDAO implements AuthDAO {
    private final HashMap<String, AuthData> authDataTable = new HashMap<>();

    @Override
    public void clear() throws DataAccessException {
        authDataTable.clear();
    }

    @Override
    public void addAuth(AuthData authData) {
        authDataTable.put(authData.authToken(), authData);
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
}