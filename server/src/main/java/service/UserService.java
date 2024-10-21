package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;

public class UserService {
    private UserDAO userDAO;
    private AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData register(UserData user) throws Exception {
        if (user.username() == null || user.password() == null) {
            throw new ServiceException(400, "Error: bad request");
        }
        if (userDAO.getUser(user) == null) {
            userDAO.createUser(user);
            return authDAO.createAuth(user.username());
        }
        throw new ServiceException(403, "Error: already taken");
    }

    public AuthData login(UserData user) {
        return null;
    }

    public void logout(AuthData auth) {

    }
}
