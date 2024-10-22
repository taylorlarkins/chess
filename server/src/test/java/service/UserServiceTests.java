package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import server.request.LoginRequest;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTests {
    private static final UserDAO USER_DAO = new MemoryUserDAO();
    private static final AuthDAO AUTH_DAO = new MemoryAuthDAO();
    private static final GameDAO GAME_DAO = new MemoryGameDAO();
    private static final ClearService CLEAR_SERVICE = new ClearService(GAME_DAO, USER_DAO, AUTH_DAO);
    private static final UserService USER_SERVICE = new UserService(USER_DAO, AUTH_DAO);

    @AfterEach
    public void cleanUp() throws Exception {
        CLEAR_SERVICE.clear();
    }

    @Test
    @DisplayName("Register User")
    public void registerUser() throws Exception {
        UserData user = new UserData("user123", "abc", "a@b.c");
        USER_SERVICE.register(user);
        assertEquals(user, USER_DAO.getUser(user.username()));
    }

    @Test
    @DisplayName("Register Duplicate Username")
    public void registerDuplicate() throws Exception {
        UserData user = new UserData("user123", "abc", "a@b.c");
        USER_SERVICE.register(user);
        ServiceException e = assertThrows(ServiceException.class, () -> USER_SERVICE.register(user));
        assertEquals("Error: already taken", e.getMessage());
    }

    @Test
    @DisplayName("Register Invalid User")
    public void registerInvalid() {
        UserData[] users = {
                new UserData(null, "abc", "a@b.c"),
                new UserData("user123", null, "a@b.c"),
                new UserData("user123", "abc", null),
                new UserData(null, null, null)
        };
        for (UserData user : users) {
            ServiceException e = assertThrows(ServiceException.class, () -> USER_SERVICE.register(user));
            assertEquals("Error: bad request", e.getMessage());
        }
    }

    @Test
    @DisplayName("Login")
    public void login() throws Exception {
        UserData user = new UserData("user123", "123", "a@b.c");
        USER_SERVICE.register(user);
        AuthData auth = USER_SERVICE.login(new LoginRequest(user.username(), user.password()));
        assertEquals(user.username(), auth.username());
        assertEquals(user.username(), AUTH_DAO.getAuth(auth.authToken()).username());
    }

    @Test
    @DisplayName("Incorrect Password")
    public void loginWithWrongPassword() throws Exception {
        UserData user = new UserData("user123", "123", "a@b.c");
        USER_SERVICE.register(user);
        ServiceException e = assertThrows(ServiceException.class, () ->
                USER_SERVICE.login(new LoginRequest(user.username(), "456"))
        );
        assertEquals("Error: unauthorized", e.getMessage());
    }

    @Test
    @DisplayName("Invalid User")
    public void loginInvalidUser() {
        ServiceException e = assertThrows(ServiceException.class, () ->
                USER_SERVICE.login(new LoginRequest("nonExistent", "blah"))
        );
        assertEquals("Error: unauthorized", e.getMessage());
    }

    @Test
    @DisplayName("Logout")
    public void logout() throws Exception {
        UserData user = new UserData("user123", "123", "a@b.c");
        AuthData auth = USER_SERVICE.register(user);
        USER_SERVICE.logout(auth.authToken());
        assertNull(AUTH_DAO.getAuth(auth.authToken()));
    }

    @Test
    @DisplayName("Logout Invalid Authorization")
    public void logoutInvalidAuthorization() throws Exception {
        UserData user = new UserData("user123", "123", "a@b.c");
        USER_SERVICE.register(user);
        ServiceException e = assertThrows(ServiceException.class, () ->
                USER_SERVICE.logout("notAnAuthToken")
        );
        assertEquals("Error: unauthorized", e.getMessage());
    }
}