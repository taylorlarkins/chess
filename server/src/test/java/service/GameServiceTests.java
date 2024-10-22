package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GameServiceTests {
    private static final UserDAO userDAO = new MemoryUserDAO();
    private static final AuthDAO authDAO = new MemoryAuthDAO();
    private static final GameDAO gameDAO = new MemoryGameDAO();
    private static final ClearService clearService = new ClearService(gameDAO, userDAO, authDAO);
    private static final UserService userService = new UserService(userDAO, authDAO);
    private static final GameService gameService = new GameService(gameDAO, userDAO, authDAO);

    private static UserData user1 = new UserData("Bob", "abc", "a@b.c");
    private static UserData user2 = new UserData("Sally", "efg", "e@f.g");
    private static AuthData user1Auth;
    private static AuthData user2Auth;

    @BeforeAll
    public static void setUp() throws Exception {
        user1Auth = userService.register(user1);
        user2Auth = userService.register(user2);
    }

    @AfterEach
    public void cleanUp() throws Exception {
        gameDAO.clear();
    }

    @Test
    @DisplayName("Create Game")
    public void createGame() throws Exception {
        int gameID = gameService.createGame("Game #1", user1Auth.authToken()).gameID();
        assertEquals(gameDAO.getGame(gameID).gameName(), "Game #1");
    }

    @Test
    @DisplayName("Unauthorized Game Creation")
    public void unauthorizedGameCreation() {
        ServiceException e = assertThrows(ServiceException.class, () ->
                gameService.createGame("UnauthorizedGame", "NotARealAuthToken")
        );
        assertEquals(e.getMessage(), "Error: unauthorized");
    }

    @Test
    @DisplayName("List Games")
    public void listGames() throws Exception {
        gameService.createGame("Game #1", user1Auth.authToken());
        gameService.createGame("Game #2", user2Auth.authToken());
        gameService.createGame("Game #3", user1Auth.authToken());
        GameData[] gameList = gameService.listGames(user2Auth.authToken()).games();
        for (GameData game : gameList) {
            assertEquals(game, gameDAO.getGame(game.gameID()));
        }
    }

    @Test
    @DisplayName("List Games with No Games")
    public void listGamesNoGames() throws Exception {
        GameData[] gameList = gameService.listGames(user2Auth.authToken()).games();
        assertEquals(gameList.length, 0);
    }

    @Test
    @DisplayName("Unauthorized List Games")
    public void unauthorizedListGames() throws Exception {
        gameService.createGame("Game #1", user1Auth.authToken());
        gameService.createGame("Game #2", user2Auth.authToken());
        ServiceException e = assertThrows(ServiceException.class, () ->
                gameService.listGames("FakeAuthToken")
        );
        assertEquals(e.getMessage(), "Error: unauthorized");
    }
}
