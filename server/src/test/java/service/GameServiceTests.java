package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.CreateGameResponse;
import server.JoinGameRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GameServiceTests {
    private static final UserDAO userDAO = new MemoryUserDAO();
    private static final AuthDAO authDAO = new MemoryAuthDAO();
    private static final GameDAO gameDAO = new MemoryGameDAO();
    private static final UserService userService = new UserService(userDAO, authDAO);
    private static final GameService gameService = new GameService(gameDAO, authDAO);

    private static final UserData user1 = new UserData("Bob", "abc", "a@b.c");
    private static final UserData user2 = new UserData("Sally", "efg", "e@f.g");
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
        assertEquals("Game #1", gameDAO.getGame(gameID).gameName());
    }

    @Test
    @DisplayName("Unauthorized Game Creation")
    public void unauthorizedGameCreation() {
        ServiceException e = assertThrows(ServiceException.class, () ->
                gameService.createGame("UnauthorizedGame", "NotARealAuthToken")
        );
        assertEquals("Error: unauthorized", e.getMessage());
    }

    @Test
    @DisplayName("List Games")
    public void listGames() throws Exception {
        gameService.createGame("Game #1", user1Auth.authToken());
        gameService.createGame("Game #2", user2Auth.authToken());
        gameService.createGame("Game #3", user1Auth.authToken());
        GameData[] gameList = gameService.listGames(user2Auth.authToken()).games();
        for (GameData game : gameList) {
            assertEquals(gameDAO.getGame(game.gameID()), game);
        }
    }

    @Test
    @DisplayName("List Games with No Games")
    public void listGamesNoGames() throws Exception {
        GameData[] gameList = gameService.listGames(user2Auth.authToken()).games();
        assertEquals(0, gameList.length);
    }

    @Test
    @DisplayName("Unauthorized List Games")
    public void unauthorizedListGames() throws Exception {
        gameService.createGame("Game #1", user1Auth.authToken());
        gameService.createGame("Game #2", user2Auth.authToken());
        ServiceException e = assertThrows(ServiceException.class, () ->
                gameService.listGames("FakeAuthToken")
        );
        assertEquals("Error: unauthorized", e.getMessage());
    }

    @Test
    @DisplayName("Join Game")
    public void joinGame() throws Exception {
        CreateGameResponse response = gameService.createGame("Game #1", user1Auth.authToken());
        gameService.joinGame(new JoinGameRequest("WHITE", response.gameID()), user1Auth.authToken());
        gameService.joinGame(new JoinGameRequest("BLACK", response.gameID()), user2Auth.authToken());
        GameData game = gameDAO.getGame(response.gameID());
        assertEquals(user1.username(), game.whiteUsername());
        assertEquals(user2.username(), game.blackUsername());
    }

    @Test
    @DisplayName("Join White Already Taken")
    public void joinWhiteAlreadyTaken() throws Exception {
        CreateGameResponse response = gameService.createGame("Game #1", user1Auth.authToken());
        gameService.joinGame(new JoinGameRequest("WHITE", response.gameID()), user1Auth.authToken());
        ServiceException e = assertThrows(ServiceException.class, () ->
                gameService.joinGame(new JoinGameRequest("WHITE", response.gameID()), user2Auth.authToken())
        );
        assertEquals("Error: already taken", e.getMessage());

    }

    @Test
    @DisplayName("Join Black Already Taken")
    public void joinBlackAlreadyTaken() throws Exception {
        CreateGameResponse response = gameService.createGame("Game #1", user1Auth.authToken());
        gameService.joinGame(new JoinGameRequest("BLACK", response.gameID()), user1Auth.authToken());
        ServiceException e = assertThrows(ServiceException.class, () ->
                gameService.joinGame(new JoinGameRequest("BLACK", response.gameID()), user2Auth.authToken())
        );
        assertEquals("Error: already taken", e.getMessage());
    }

    @Test
    @DisplayName("Join Game Unauthorized")
    public void joinGameUnauthorized() throws Exception {
        CreateGameResponse response = gameService.createGame("Game #1", user1Auth.authToken());
        ServiceException e = assertThrows(ServiceException.class, () ->
                gameService.joinGame(
                        new JoinGameRequest("WHITE", response.gameID()), "InvalidAuthorization"
                )
        );
        assertEquals("Error: unauthorized", e.getMessage());
    }
}