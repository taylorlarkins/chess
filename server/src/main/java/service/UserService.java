package service;

import dataaccess.DataAccessObject;
import model.AuthData;
import model.UserData;

public class UserService {
    DataAccessObject dataAccess;

    public UserService(DataAccessObject dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData user) {
        return null;
    }

    public AuthData login(UserData user) {
        return null;
    }

    public void logout(AuthData auth) {

    }
}
