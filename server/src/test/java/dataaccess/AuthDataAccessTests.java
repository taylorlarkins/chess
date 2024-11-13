package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class AuthDataAccessTests {
    private static AuthDAO authDAO;
    private static final AuthData TESTAUTH1 = new AuthData("001", "user123");
    private static final AuthData TESTAUTH2 = new AuthData("002", "iAmAUser");

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

        authDAO.addAuth(TESTAUTH1);
        authDAO.addAuth(TESTAUTH2);
        assertEquals(2, authDAO.getSize());
        authDAO.clear();
        assertEquals(0, authDAO.getSize());
    }

    @Test
    @DisplayName("Create Authorization Token")
    public void createAuthToken() throws DataAccessException {
        assertDoesNotThrow(() -> authDAO.addAuth(TESTAUTH1));
        assertEquals(1, authDAO.getSize());
    }

    //negative addAuth test?

    @Test
    @DisplayName("Get Authorization")
    public void getAuth() throws DataAccessException {
        authDAO.addAuth(TESTAUTH1);
        AuthData getAuth = assertDoesNotThrow(() -> authDAO.getAuth(TESTAUTH1.authToken()));
        assertEquals(TESTAUTH1.authToken(), getAuth.authToken());
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
        authDAO.addAuth(TESTAUTH2);
        assertDoesNotThrow(() -> authDAO.deleteAuth(TESTAUTH2.authToken()));
        assertNull(authDAO.getAuth(TESTAUTH2.authToken()));
    }

    @Test
    @DisplayName("Delete Nonexistent Authorization")
    public void deleteNonexistentAuthorization() {
        assertDoesNotThrow(() -> authDAO.deleteAuth("NotAnAuthToken"));
    }
}