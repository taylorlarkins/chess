package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClearServiceTests {
    private static final UserDAO USER_DAO = new MemoryUserDAO();
    private static final AuthDAO AUTH_DAO = new MemoryAuthDAO();
    private static final GameDAO GAME_DAO = new MemoryGameDAO();
    private static final ClearService CLEAR_SERVICE = new ClearService(GAME_DAO, USER_DAO, AUTH_DAO);

    @Test
    @DisplayName("Clear Data")
    public void clearData() throws DataAccessException {
        USER_DAO.createUser(new UserData("iAmAUser", "12345", "a@b.c"));
        AUTH_DAO.addAuth(new AuthData("001", "iAmAUser"));
        USER_DAO.createUser(new UserData("anotherUser", "hello", "b@c.d"));
        AUTH_DAO.addAuth(new AuthData("002", "anotherUser"));
        GAME_DAO.addGame(new GameData(
                        1,
                        null,
                        null,
                        "Game #1",
                        new ChessGame()
                )
        );
        GAME_DAO.addGame(new GameData(
                        2,
                        "user123",
                        "iAmAUser",
                        "Game #2",
                        new ChessGame()
                )
        );
        assertEquals(2, USER_DAO.getSize());
        assertEquals(2, AUTH_DAO.getSize());
        assertEquals(2, GAME_DAO.getSize());
        CLEAR_SERVICE.clear();
        assertEquals(0, USER_DAO.getSize());
        assertEquals(0, AUTH_DAO.getSize());
        assertEquals(0, GAME_DAO.getSize());
    }
}
