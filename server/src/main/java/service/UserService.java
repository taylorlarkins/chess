package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import server.request.LoginRequest;

import java.util.UUID;

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
            String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
            userDataAccess.createUser(new UserData(user.username(), hashedPassword, user.email()));
            AuthData auth = new AuthData(generateToken(), user.username());
            authDataAccess.addAuth(auth);
            return auth;
        }
        throw new ServiceException(403, "Error: already taken");
    }

    public AuthData login(LoginRequest request) throws Exception {
        UserData user = userDataAccess.getUser(request.username());
        if (user == null || !verifyPassword(user.password(), request.password())) {
            throw new ServiceException(401, "Error: unauthorized");
        }
        AuthData auth = new AuthData(generateToken(), user.username());
        authDataAccess.addAuth(auth);
        return auth;
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

    private boolean verifyPassword(String hashedPassword, String givenPassword) {
        return BCrypt.checkpw(givenPassword, hashedPassword);
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }
}
