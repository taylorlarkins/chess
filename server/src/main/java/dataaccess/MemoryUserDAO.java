package dataaccess;

import model.UserData;

import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {
    private HashMap<String, UserData> userDataTable = new HashMap<>();

    @Override
    public void clear() throws DataAccessException {
        userDataTable.clear();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        userDataTable.put(user.username(), user);
    }

    @Override
    public UserData getUser(UserData user) throws DataAccessException {
        return userDataTable.get(user.username());
    }
}
