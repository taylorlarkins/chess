package service;

import dataaccess.*;
import model.UserData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClearServiceTests {
    private static final UserDAO userDAO = new MemoryUserDAO();
    private static final AuthDAO authDAO = new MemoryAuthDAO();
    private static final GameDAO gameDAO = new MemoryGameDAO();
    private static final ClearService clearService = new ClearService(gameDAO, userDAO, authDAO);

    @Test
    @DisplayName("Clear Data")
    public void clearData() throws DataAccessException {
        userDAO.createUser(new UserData("iAmAUser", "12345", "a@b.c"));
        authDAO.createAuth("iAmAUser");
        userDAO.createUser(new UserData("anotherUser", "hello", "b@c.d"));
        authDAO.createAuth("anotherUser");
        gameDAO.createGame("Game1");
        gameDAO.createGame("Game2");
        assertEquals(2, userDAO.getSize());
        assertEquals(2, authDAO.getSize());
        assertEquals(2, gameDAO.getSize());
        clearService.clear();
        assertEquals(0, userDAO.getSize());
        assertEquals(0, authDAO.getSize());
        assertEquals(0, gameDAO.getSize());
    }
}
