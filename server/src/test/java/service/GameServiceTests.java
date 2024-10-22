package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.response.CreateGameResponse;
import server.request.JoinGameRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GameServiceTests {
    private static final UserDAO USER_DAO = new MemoryUserDAO();
    private static final AuthDAO AUTH_DAO = new MemoryAuthDAO();
    private static final GameDAO GAME_DAO = new MemoryGameDAO();
    private static final UserService USER_SERVICE = new UserService(USER_DAO, AUTH_DAO);
    private static final GameService GAME_SERVICE = new GameService(GAME_DAO, AUTH_DAO);

    private static final UserData USER_1 = new UserData("Bob", "abc", "a@b.c");
    private static final UserData USER_2 = new UserData("Sally", "efg", "e@f.g");
    private static AuthData user1Auth;
    private static AuthData user2Auth;

    @BeforeAll
    public static void setUp() throws Exception {
        user1Auth = USER_SERVICE.register(USER_1);
        user2Auth = USER_SERVICE.register(USER_2);
    }

    @AfterEach
    public void cleanUp() throws Exception {
        GAME_DAO.clear();
    }

    @Test
    @DisplayName("Create Game")
    public void createGame() throws Exception {
        int gameID = GAME_SERVICE.createGame("Game #1", user1Auth.authToken()).gameID();
        assertEquals("Game #1", GAME_DAO.getGame(gameID).gameName());
    }

    @Test
    @DisplayName("Unauthorized Game Creation")
    public void unauthorizedGameCreation() {
        ServiceException e = assertThrows(ServiceException.class, () ->
                GAME_SERVICE.createGame("UnauthorizedGame", "NotARealAuthToken")
        );
        assertEquals("Error: unauthorized", e.getMessage());
    }

    @Test
    @DisplayName("List Games")
    public void listGames() throws Exception {
        GAME_SERVICE.createGame("Game #1", user1Auth.authToken());
        GAME_SERVICE.createGame("Game #2", user2Auth.authToken());
        GAME_SERVICE.createGame("Game #3", user1Auth.authToken());
        GameData[] gameList = GAME_SERVICE.listGames(user2Auth.authToken()).games();
        for (GameData game : gameList) {
            assertEquals(GAME_DAO.getGame(game.gameID()), game);
        }
    }

    @Test
    @DisplayName("List Games with No Games")
    public void listGamesNoGames() throws Exception {
        GameData[] gameList = GAME_SERVICE.listGames(user2Auth.authToken()).games();
        assertEquals(0, gameList.length);
    }

    @Test
    @DisplayName("Unauthorized List Games")
    public void unauthorizedListGames() throws Exception {
        GAME_SERVICE.createGame("Game #1", user1Auth.authToken());
        GAME_SERVICE.createGame("Game #2", user2Auth.authToken());
        ServiceException e = assertThrows(ServiceException.class, () ->
                GAME_SERVICE.listGames("FakeAuthToken")
        );
        assertEquals("Error: unauthorized", e.getMessage());
    }

    @Test
    @DisplayName("Join Game")
    public void joinGame() throws Exception {
        CreateGameResponse response = GAME_SERVICE.createGame("Game #1", user1Auth.authToken());
        GAME_SERVICE.joinGame(new JoinGameRequest("WHITE", response.gameID()), user1Auth.authToken());
        GAME_SERVICE.joinGame(new JoinGameRequest("BLACK", response.gameID()), user2Auth.authToken());
        GameData game = GAME_DAO.getGame(response.gameID());
        assertEquals(USER_1.username(), game.whiteUsername());
        assertEquals(USER_2.username(), game.blackUsername());
    }

    @Test
    @DisplayName("Join White Already Taken")
    public void joinWhiteAlreadyTaken() throws Exception {
        CreateGameResponse response = GAME_SERVICE.createGame("Game #1", user1Auth.authToken());
        GAME_SERVICE.joinGame(new JoinGameRequest("WHITE", response.gameID()), user1Auth.authToken());
        ServiceException e = assertThrows(ServiceException.class, () ->
                GAME_SERVICE.joinGame(new JoinGameRequest("WHITE", response.gameID()), user2Auth.authToken())
        );
        assertEquals("Error: already taken", e.getMessage());

    }

    @Test
    @DisplayName("Join Black Already Taken")
    public void joinBlackAlreadyTaken() throws Exception {
        CreateGameResponse response = GAME_SERVICE.createGame("Game #1", user1Auth.authToken());
        GAME_SERVICE.joinGame(new JoinGameRequest("BLACK", response.gameID()), user1Auth.authToken());
        ServiceException e = assertThrows(ServiceException.class, () ->
                GAME_SERVICE.joinGame(new JoinGameRequest("BLACK", response.gameID()), user2Auth.authToken())
        );
        assertEquals("Error: already taken", e.getMessage());
    }

    @Test
    @DisplayName("Join Game Unauthorized")
    public void joinGameUnauthorized() throws Exception {
        CreateGameResponse response = GAME_SERVICE.createGame("Game #1", user1Auth.authToken());
        ServiceException e = assertThrows(ServiceException.class, () ->
                GAME_SERVICE.joinGame(
                        new JoinGameRequest("WHITE", response.gameID()), "InvalidAuthorization"
                )
        );
        assertEquals("Error: unauthorized", e.getMessage());
    }
}