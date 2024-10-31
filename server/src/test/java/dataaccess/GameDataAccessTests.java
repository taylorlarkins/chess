package dataaccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class GameDataAccessTests {
    private static GameDAO gameDAO;

    public GameDataAccessTests() throws DataAccessException {
        gameDAO = new SQLGameDAO();
    }

    @BeforeEach
    public void prepare() throws DataAccessException {
        gameDAO.clear();
    }

    @Test
    @DisplayName("Clear User Data")
    public void clearData() throws DataAccessException {
        gameDAO.createGame("Game #1");
        gameDAO.createGame("Game #2");
        assertEquals(2, gameDAO.getSize());
        gameDAO.clear();
        assertEquals(0, gameDAO.getSize());
    }
}