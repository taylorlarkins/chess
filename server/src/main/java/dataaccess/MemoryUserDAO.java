package dataaccess;

import model.UserData;

import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {
    private final HashMap<String, UserData> userDataTable = new HashMap<>();

    @Override
    public void clear() throws DataAccessException {
        userDataTable.clear();
    }

    @Override
    public void createUser(UserData user) {
        userDataTable.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        return userDataTable.get(username);
    }

    @Override
    public int getSize() {
        return userDataTable.size();
    }
}