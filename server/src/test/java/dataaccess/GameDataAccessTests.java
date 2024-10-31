package dataaccess;

import model.GameData;
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

    @Test
    @DisplayName("Create Game")
    public void createGame() throws DataAccessException {
        assertDoesNotThrow(() -> gameDAO.createGame("Game #1"));
        assertEquals(1, gameDAO.getSize());
    }

    //negative createGame test?

    @Test
    @DisplayName("Get Game")
    public void getGame() throws DataAccessException {
        int gameID = gameDAO.createGame("Game #1");
        GameData game = assertDoesNotThrow(() -> gameDAO.getGame(gameID));
        assertEquals(gameID, game.gameID());
        assertEquals("Game #1", game.gameName());
    }
}