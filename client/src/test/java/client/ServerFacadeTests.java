package client;

import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import request.CreateGameRequest;
import request.JoinGameRequest;
import server.Server;
import request.LoginRequest;
import ui.ChessClient;
import ui.ClientException;
import ui.ServerFacade;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;
    static ChessClient client;

    @BeforeAll
    public static void init() {
        server = new Server();
        int port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
        client = new ChessClient("http://localhost:" + port, null);
    }

    @BeforeEach
    public void testPreparation() {
        facade.clear();
    }

    @AfterAll
    static void stopServer() {
        facade.clear();
        server.stop();
    }

    @Test
    @DisplayName("Register User")
    public void register() throws Exception {
        AuthData authData = facade.register(new UserData("Player", "pass", "a@b.c"));
        assertTrue(authData.authToken().length() > 10);
    }

    @Test
    @DisplayName("Register Existing User")
    public void reRegister() throws Exception {
        facade.register(new UserData("Player", "pass", "a@b.c"));
        ClientException ex = assertThrows(ClientException.class, () ->
                facade.register(new UserData("Player", "pass", "a@b.c"))
        );
        assertEquals("Error: already taken", ex.getMessage());
    }

    @Test
    @DisplayName("Login User")
    public void loginUser() throws Exception {
        facade.register(new UserData("Player", "pass", "a@b.c"));
        AuthData auth = facade.login(new LoginRequest("Player", "pass"));
        assertEquals("Player", auth.username());
    }

    @Test
    @DisplayName("Login With Wrong Password")
    public void wrongPassword() throws Exception {
        facade.register(new UserData("Player", "pass", "a@b.c"));
        ClientException ex = assertThrows(ClientException.class, () ->
                facade.login(new LoginRequest("Player", "wrong"))
        );
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    @DisplayName("Logout")
    public void logout() throws Exception {
        facade.register(new UserData("Player", "pass", "a@b.c"));
        AuthData auth = facade.login(new LoginRequest("Player", "pass"));
        assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }

    @Test
    @DisplayName("Logout with Invalid Authorization")
    public void invalidLogout() throws Exception {
        facade.register(new UserData("Player", "pass", "a@b.c"));
        facade.login(new LoginRequest("Player", "pass"));
        ClientException ex = assertThrows(ClientException.class, () ->
                facade.logout("invalid authorization")
        );
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    @DisplayName("Create Game")
    public void createGame() throws Exception {
        facade.register(new UserData("Player", "pass", "a@b.c"));
        AuthData auth = facade.login(new LoginRequest("Player", "pass"));
        assertDoesNotThrow(() ->
                facade.createGame(new CreateGameRequest("TestGame"), auth.authToken())
        );
    }

    @Test
    @DisplayName("Create Game with Invalid Authorization")
    public void createGameInvalid() throws Exception {
        facade.register(new UserData("Player", "pass", "a@b.c"));
        facade.login(new LoginRequest("Player", "pass"));
        ClientException ex = assertThrows(ClientException.class, () ->
                facade.createGame(new CreateGameRequest("TestGame"), "invalid authorization")
        );
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    @DisplayName("List Games")
    public void listGames() throws Exception {
        String[] givenNames = {"Chess Game", "I Like Chess", "Cow"};
        facade.register(new UserData("Player", "pass", "a@b.c"));
        AuthData auth = facade.login(new LoginRequest("Player", "pass"));
        facade.createGame(new CreateGameRequest(givenNames[0]), auth.authToken());
        facade.createGame(new CreateGameRequest(givenNames[1]), auth.authToken());
        facade.createGame(new CreateGameRequest(givenNames[2]), auth.authToken());
        GameData[] result = facade.listGames(auth.authToken());
        String[] resultNames = {result[0].gameName(), result[1].gameName(), result[2].gameName()};
        Arrays.sort(givenNames);
        Arrays.sort(resultNames);
        assertArrayEquals(givenNames, resultNames);
    }

    @Test
    @DisplayName("List Games without Authorization")
    public void listGamesInvalid() throws Exception {
        facade.register(new UserData("Player", "pass", "a@b.c"));
        facade.login(new LoginRequest("Player", "pass"));
        ClientException ex = assertThrows(ClientException.class, () ->
                facade.listGames("invalid authorization")
        );
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    @DisplayName("Join Game")
    public void joinGame() throws Exception {
        facade.register(new UserData("Player1", "pass", "a@b.c"));
        AuthData auth1 = facade.login(new LoginRequest("Player1", "pass"));
        facade.createGame(new CreateGameRequest("TestGame1"), auth1.authToken());
        assertDoesNotThrow(() ->
                facade.joinGame(new JoinGameRequest("BLACK", 1), auth1.authToken())
        );
        facade.register(new UserData("Player2", "pass", "a@b.c"));
        AuthData auth2 = facade.login(new LoginRequest("Player2", "pass"));
        assertDoesNotThrow(() ->
                facade.joinGame(new JoinGameRequest("WHITE", 1), auth2.authToken())
        );
    }

    @Test
    @DisplayName("Join Game as Already Taken Color")
    public void joinGameAlreadyTaken() throws Exception {
        facade.register(new UserData("Player1", "pass", "a@b.c"));
        AuthData auth1 = facade.login(new LoginRequest("Player1", "pass"));
        facade.createGame(new CreateGameRequest("TestGame1"), auth1.authToken());
        facade.joinGame(new JoinGameRequest("BLACK", 1), auth1.authToken());
        facade.register(new UserData("Player2", "pass", "a@b.c"));
        AuthData auth2 = facade.login(new LoginRequest("Player2", "pass"));
        ClientException ex = assertThrows(ClientException.class, () ->
                facade.joinGame(new JoinGameRequest("BLACK", 1), auth2.authToken())
        );
        assertEquals("Error: already taken", ex.getMessage());
    }
}
