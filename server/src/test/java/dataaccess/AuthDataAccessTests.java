package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class AuthDataAccessTests {
    private static AuthDAO authDAO;

    public AuthDataAccessTests() throws DataAccessException {
        authDAO = new SQLAuthDAO();
    }

    @BeforeEach
    public void prepare() throws DataAccessException {
        authDAO.clear();
    }

    @Test
    @DisplayName("Clear User Data")
    public void clearData() throws DataAccessException {
        authDAO.createAuth("user123");
        authDAO.createAuth("iAmAUser");
        assertEquals(2, authDAO.getSize());
        authDAO.clear();
        assertEquals(0, authDAO.getSize());
    }

    @Test
    @DisplayName("Create Authorization Token")
    public void createAuthToken() throws DataAccessException {
        assertDoesNotThrow(() -> authDAO.createAuth("user123"));
        assertEquals(1, authDAO.getSize());
    }

    //negative createAuth test?

    @Test
    @DisplayName("Get Authorization")
    public void getAuth() throws DataAccessException {
        AuthData createAuth = authDAO.createAuth("user123");
        AuthData getAuth = assertDoesNotThrow(() -> authDAO.getAuth(createAuth.authToken()));
        assertEquals(createAuth.authToken(), getAuth.authToken());
        assertEquals("user123", getAuth.username());
    }

    @Test
    @DisplayName("Get Nonexistent Authorization")
    public void getNonexistentAuth() throws DataAccessException {
        assertNull(authDAO.getAuth("nonExistentAuth"));
    }

    @Test
    @DisplayName("Delete Authorization")
    public void deleteAuthorization() throws DataAccessException {
        AuthData authData = authDAO.createAuth("user123");
        assertDoesNotThrow(() -> authDAO.deleteAuth(authData.authToken()));
        assertNull(authDAO.getAuth(authData.authToken()));
    }

    //negative deleteAuth test?
}