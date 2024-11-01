package dataaccess;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class GameDataAccessTests {
    private static GameDAO gameDAO;
    private static final GameData testGame1 = new GameData(
            1,
            null,
            null,
            "Game #1",
            new ChessGame()
    );
    private static final GameData testGame2 = new GameData(
            1,
            "user123",
            "iAmAUser",
            "Game #2",
            new ChessGame()
    );
    private static final GameData testGame3 = new GameData(
            1,
            "anotherUser",
            null,
            "Game #1",
            new ChessGame()
    );

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
        gameDAO.addGame(testGame1);
        gameDAO.addGame(testGame2);
        assertEquals(2, gameDAO.getSize());
        gameDAO.clear();
        assertEquals(0, gameDAO.getSize());
    }

    @Test
    @DisplayName("Create Game")
    public void createGame() throws DataAccessException {
        assertDoesNotThrow(() -> gameDAO.addGame(testGame1));
        assertEquals(1, gameDAO.getSize());
    }

    //negative addGame test?

    @Test
    @DisplayName("Get Game")
    public void getGame() throws DataAccessException {
        int gameID = gameDAO.addGame(testGame1);
        GameData game = assertDoesNotThrow(() -> gameDAO.getGame(gameID));
        assertEquals(gameID, game.gameID());
        assertEquals("Game #1", game.gameName());
    }

    @Test
    @DisplayName("Get Nonexistent Game")
    public void getNonexistentGame() throws DataAccessException {
        assertNull(gameDAO.getGame(0));
    }

    @Test
    @DisplayName("List Games")
    public void listGames() throws DataAccessException {
        int[] gameIDs = {
                gameDAO.addGame(testGame1),
                gameDAO.addGame(testGame2),
                gameDAO.addGame(testGame3)
        };
        GameData[] games = assertDoesNotThrow(() -> gameDAO.listGames());
        assertEquals(3, games.length);
        for (int i = 0; i < 3; i++) {
            assertEquals(gameIDs[i], games[i].gameID());
        }
    }

    @Test
    @DisplayName("List Games When There Are No Games")
    public void listGamesNoGames() {
        GameData[] games = assertDoesNotThrow(() -> gameDAO.listGames());
        assertEquals(0, games.length);
    }

    @Test
    @DisplayName("Update Game")
    public void updateGame() throws DataAccessException {
        int gameID = gameDAO.addGame(testGame1);
        ChessBoard customBoard = new ChessBoard();
        customBoard.addPiece(
                new ChessPosition(1, 1),
                new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN)
        );
        ChessGame chessGame = new ChessGame();
        chessGame.setBoard(customBoard);
        GameData modifiedGame = new GameData(
                gameID,
                "user123",
                "iAmAUser",
                "Renamed Game",
                chessGame
        );
        assertDoesNotThrow(() -> gameDAO.updateGame(modifiedGame));
        GameData updatedGame = gameDAO.getGame(gameID);
        assertEquals("user123", updatedGame.whiteUsername());
        assertEquals("iAmAUser", updatedGame.blackUsername());
        assertEquals(chessGame.getBoard(), updatedGame.game().getBoard());
    }

    @Test
    @DisplayName("Update Nonexistent Game")
    public void updateNonexistentGame() {
        GameData fakeGame = new GameData(
                0,
                null,
                null,
                "Fake Game",
                new ChessGame()
        );
        assertDoesNotThrow(() -> gameDAO.updateGame(fakeGame));
    }
}