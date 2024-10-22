package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;

public class ClearService {
    private final GameDAO gameDataAccess;
    private final UserDAO userDataAccess;
    private final AuthDAO authDataAccess;

    public ClearService(GameDAO gameDataAccess, UserDAO userDataAccess, AuthDAO authDataAccess) {
        this.gameDataAccess = gameDataAccess;
        this.userDataAccess = userDataAccess;
        this.authDataAccess = authDataAccess;
    }

    public void clear() throws DataAccessException {
        gameDataAccess.clear();
        userDataAccess.clear();
        authDataAccess.clear();
    }
}