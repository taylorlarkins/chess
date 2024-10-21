package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import server.LoginRequest;

public class UserService {
    private UserDAO userDAO;
    private AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData register(UserData user) throws Exception {
        if (user.username() == null || user.password() == null || user.email() == null) {
            throw new ServiceException(400, "Error: bad request");
        }
        if (userDAO.getUser(user.username()) == null) {
            userDAO.createUser(user);
            return authDAO.createAuth(user.username());
        }
        throw new ServiceException(403, "Error: already taken");
    }

    public AuthData login(LoginRequest request) throws Exception {
        UserData user = userDAO.getUser(request.username());
        if (user == null || !user.password().equals(request.password())) {
            throw new ServiceException(401, "Error: unauthorized");
        }
        return authDAO.createAuth(user.username());
    }

    public void logout(AuthData auth) {

    }
}