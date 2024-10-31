package dataaccess;

import model.AuthData;

import java.util.UUID;

public class SQLAuthDAO extends SQLDAO implements AuthDAO {
    public SQLAuthDAO() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public void clear() throws DataAccessException {
        String statement = "TRUNCATE auth";
        executeUpdate(statement);
    }

    @Override
    public AuthData createAuth(String username) throws DataAccessException {
        String statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        String authToken = UUID.randomUUID().toString();
        executeUpdate(statement, authToken, username);
        return new AuthData(authToken, username);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {

    }

    @Override
    public int getSize() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement("SELECT COUNT(*) FROM auth;")) {
                var rs = ps.executeQuery();
                rs.next();
                return rs.getInt(1);

            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }
    }
}
