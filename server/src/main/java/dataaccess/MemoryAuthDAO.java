package dataaccess;

import model.AuthData;

import java.util.HashMap;

public class MemoryAuthDAO implements AuthDAO {
    private int nextAuthToken = 1;
    private HashMap<String, AuthData> authDataTable = new HashMap<>();

    @Override
    public void clear() throws DataAccessException {
        authDataTable.clear();
    }

    @Override
    public void createAuth(String username) throws DataAccessException {
        AuthData auth = new AuthData(nextAuthToken++ + "", username);
        authDataTable.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return authDataTable.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        authDataTable.remove(authToken);
    }

    @Override
    public int getSize() {
        return authDataTable.size();
    }
}
