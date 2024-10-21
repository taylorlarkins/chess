package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import server.LoginRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserServiceTests {
    private static final UserDAO userDAO = new MemoryUserDAO();
    private static final AuthDAO authDAO = new MemoryAuthDAO();
    private static final GameDAO gameDAO = new MemoryGameDAO();
    private static final ClearService clearService = new ClearService(gameDAO, userDAO, authDAO);
    private static final UserService userService = new UserService(userDAO, authDAO);

    @AfterEach
    public void cleanUp() throws Exception {
        clearService.clear();
    }

    @Test
    @DisplayName("Register User")
    public void registerUser() throws Exception {
        UserData user = new UserData("user123", "abc", "a@b.c");
        userService.register(user);
        assertEquals(userDAO.getUser(user.username()), user);
    }

    @Test
    @DisplayName("Register Duplicate Username")
    public void registerDuplicate() throws Exception {
        UserData user = new UserData("user123", "abc", "a@b.c");
        userService.register(user);
        Exception e = assertThrows(ServiceException.class, () -> userService.register(user));
        assertEquals(e.getMessage(), "Error: already taken");
    }

    @Test
    @DisplayName("Register Invalid User")
    public void registerInvalid() throws Exception {
        UserData[] users = {
                new UserData(null, "abc", "a@b.c"),
                new UserData("user123", null, "a@b.c"),
                new UserData("user123", "abc", null),
                new UserData(null, null, null)
        };
        for (UserData user : users) {
            Exception e = assertThrows(ServiceException.class, () -> userService.register(user));
            assertEquals(e.getMessage(), "Error: bad request");
        }
    }

    @Test
    @DisplayName("Login")
    public void login() throws Exception {
        UserData user = new UserData("user123", "123", "a@b.c");
        userService.register(user);
        AuthData auth = userService.login(new LoginRequest(user.username(), user.password()));
        assertEquals(user.username(), auth.username());
        assertEquals(user.username(), authDAO.getAuth(auth.authToken()).username());
    }

    @Test
    @DisplayName("Incorrect Password")
    public void loginWithWrongPassword() throws Exception {
        UserData user = new UserData("user123", "123", "a@b.c");
        userService.register(user);
        Exception e = assertThrows(ServiceException.class, () ->
                userService.login(new LoginRequest(user.username(), "456"))
        );
        assertEquals(e.getMessage(), "Error: unauthorized");
    }

    @Test
    @DisplayName("Invalid User")
    public void loginInvalidUser() {
        Exception e = assertThrows(ServiceException.class, () ->
                userService.login(new LoginRequest("nonExistent", "blah"))
        );
        assertEquals(e.getMessage(), "Error: unauthorized");
    }
}
