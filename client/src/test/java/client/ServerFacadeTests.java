package client;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import request.LoginRequest;
import ui.ChessClient;
import ui.ClientException;
import ui.ServerFacade;

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
        client = new ChessClient("http://localhost:" + port);
    }

    @BeforeEach
    public void testPreparation() {
        facade.clear();
    }

    @AfterAll
    static void stopServer() {
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
        AuthData auth = facade.login(new LoginRequest("Player", "pass"));
        ClientException ex = assertThrows(ClientException.class, () ->
                facade.logout("invalid authorization")
        );
        assertEquals("Error: unauthorized", ex.getMessage());
    }
}
