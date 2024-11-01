package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class AuthDataAccessTests {
    private static AuthDAO authDAO;
    private static final AuthData testAuth1 = new AuthData("001", "user123");
    private static final AuthData testAuth2 = new AuthData("002", "iAmAUser");

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

        authDAO.addAuth(testAuth1);
        authDAO.addAuth(testAuth2);
        assertEquals(2, authDAO.getSize());
        authDAO.clear();
        assertEquals(0, authDAO.getSize());
    }

    @Test
    @DisplayName("Create Authorization Token")
    public void createAuthToken() throws DataAccessException {
        assertDoesNotThrow(() -> authDAO.addAuth(testAuth1));
        assertEquals(1, authDAO.getSize());
    }

    //negative addAuth test?

    @Test
    @DisplayName("Get Authorization")
    public void getAuth() throws DataAccessException {
        authDAO.addAuth(testAuth1);
        AuthData getAuth = assertDoesNotThrow(() -> authDAO.getAuth(testAuth1.authToken()));
        assertEquals(testAuth1.authToken(), getAuth.authToken());
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
        authDAO.addAuth(testAuth2);
        assertDoesNotThrow(() -> authDAO.deleteAuth(testAuth2.authToken()));
        assertNull(authDAO.getAuth(testAuth2.authToken()));
    }

    @Test
    @DisplayName("Delete Nonexistent Authorization")
    public void deleteNonexistentAuthorization() {
        assertDoesNotThrow(() -> authDAO.deleteAuth("NotAnAuthToken"));
    }
}