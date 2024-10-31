package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

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
        //authDAO.createUser(new UserData("user123", "1234", "a@b.c"));
        //authDAO.createUser(new UserData("iAmAUser", "4321", "b@c.d"));
        assertEquals(2, authDAO.getSize());
        authDAO.clear();
        assertEquals(0, authDAO.getSize());
    }
}