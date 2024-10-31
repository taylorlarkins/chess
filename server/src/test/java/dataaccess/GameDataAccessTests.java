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

    @Test
    @DisplayName("Get Nonexistent Game")
    public void getNonexistentGame() throws DataAccessException {
        assertNull(gameDAO.getGame(0));
    }

    @Test
    @DisplayName("List Games")
    public void listGames() throws DataAccessException {
        int[] gameIDs = {
                gameDAO.createGame("Game #1"),
                gameDAO.createGame("Game #2"),
                gameDAO.createGame("Game #3")
        };
        GameData[] games = assertDoesNotThrow(() -> gameDAO.listGames());
        assertEquals(3, games.length);
        for (int i = 0; i < 3; i++) {
            assertEquals(gameIDs[i], games[i].gameID());
        }
    }

    @Test
    @DisplayName("List Games When There Are No Games")
    public void listGamesNoGames() throws DataAccessException {
        GameData[] games = assertDoesNotThrow(() -> gameDAO.listGames());
        assertEquals(0, games.length);
    }

    @Test
    @DisplayName("Update Game")
    public void updateGame() throws DataAccessException {
        int gameID = gameDAO.createGame("Game #1");
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
}