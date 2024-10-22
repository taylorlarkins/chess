package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import server.request.LoginRequest;

public class UserService {
    private final UserDAO userDataAccess;
    private final AuthDAO authDataAccess;

    public UserService(UserDAO userDataAccess, AuthDAO authDataAccess) {
        this.userDataAccess = userDataAccess;
        this.authDataAccess = authDataAccess;
    }

    public AuthData register(UserData user) throws Exception {
        if (user.username() == null || user.password() == null || user.email() == null) {
            throw new ServiceException(400, "Error: bad request");
        }
        if (userDataAccess.getUser(user.username()) == null) {
            userDataAccess.createUser(user);
            return authDataAccess.createAuth(user.username());
        }
        throw new ServiceException(403, "Error: already taken");
    }

    public AuthData login(LoginRequest request) throws Exception {
        UserData user = userDataAccess.getUser(request.username());
        if (user == null || !user.password().equals(request.password())) {
            throw new ServiceException(401, "Error: unauthorized");
        }
        return authDataAccess.createAuth(user.username());
    }

    public void logout(String authToken) throws Exception {
        AuthData auth = authDataAccess.getAuth(authToken);
        authenticate(auth);
        authDataAccess.deleteAuth(authToken);
    }

    public static void authenticate(AuthData auth) throws Exception {
        if (auth == null) {
            throw new ServiceException(401, "Error: unauthorized");
        }
    }
}
