package dataaccess;

import model.UserData;

public interface UserDAO {
    void clear() throws DataAccessException;

    void createUser(UserData user) throws DataAccessException;

    UserData getUser(UserData user) throws DataAccessException;

    int getSize();
}
