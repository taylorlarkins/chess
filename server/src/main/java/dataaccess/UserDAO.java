package dataaccess;

import model.UserData;

public interface UserDAO {
    UserData createUser(UserData user);

    UserData getUser(UserData user);
}
