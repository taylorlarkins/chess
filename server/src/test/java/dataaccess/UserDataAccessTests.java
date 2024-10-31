package dataaccess;

import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;


public class UserDataAccessTests {
    private static UserDAO userDAO;

    public UserDataAccessTests() throws DataAccessException {
        userDAO = new SQLUserDAO();
    }

    @BeforeEach
    public void prepare() throws DataAccessException {
        userDAO.clear();
    }

    @Test
    @DisplayName("Clear User Data")
    public void clearData() throws DataAccessException {
        userDAO.createUser(new UserData("user123", "1234", "a@b.c"));
        userDAO.createUser(new UserData("iAmAUser", "4321", "b@c.d"));
        assertEquals(2, userDAO.getSize());
        userDAO.clear();
        assertEquals(0, userDAO.getSize());
    }

    @Test
    @DisplayName("Create a User")
    public void createUser() throws DataAccessException {
        assertDoesNotThrow(() -> userDAO.createUser(new UserData("user123", "1234", "a@b.c")));
        assertEquals(1, userDAO.getSize());
    }

    @Test
    @DisplayName("Create Duplicate User")
    public void createDuplicateUser() throws DataAccessException {
        userDAO.clear();
        userDAO.createUser(new UserData("user123", "1234", "a@b.c"));
        assertThrows(DataAccessException.class, () ->
                userDAO.createUser(new UserData("user123", "duplicate", "bad"))
        );
    }

    @Test
    @DisplayName("Get User")
    public void getUser() throws DataAccessException {
        userDAO.createUser(new UserData("user123", "1234", "a@b.c"));
        String hashedPassword = BCrypt.hashpw("1234", BCrypt.gensalt());
        UserData user = userDAO.getUser("user123");
        assertEquals("user123", user.username());
        assertTrue(BCrypt.checkpw("1234", hashedPassword));
        assertEquals("a@b.c", user.email());
    }

    @Test
    @DisplayName("Get Nonexistent User")
    public void getNonexistentUser() throws DataAccessException {
        assertNull(userDAO.getUser("nonExistentUser"));
    }
}