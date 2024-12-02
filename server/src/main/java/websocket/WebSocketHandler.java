package websocket;

import com.google.gson.Gson;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;
import websocket.messages.NotificationMessage;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private GameDAO gameDAO;
    private AuthDAO authDAO;

    public WebSocketHandler() {
        try {
            gameDAO = new SQLGameDAO();
            authDAO = new SQLAuthDAO();
        } catch (DataAccessException ex) {
            gameDAO = new MemoryGameDAO();
            authDAO = new MemoryAuthDAO();
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws DataAccessException, IOException {
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> connect(session, command);
            case LEAVE -> leave();
            case RESIGN -> resign();
        }
    }

    private void connect(Session session, UserGameCommand command) throws DataAccessException, IOException {
        connections.add(command.getAuthToken(), session);
        AuthData auth = authDAO.getAuth(command.getAuthToken());
        GameData game = gameDAO.getGame(command.getGameID());
        String team = null;
        if (auth.username().equals(game.whiteUsername())) {
            team = "WHITE";
        } else if (auth.username().equals(game.blackUsername())) {
            team = "BLACK";
        }
        String message;
        if (team == null) {
            message = String.format("%s has joined the game as an observer.", auth.username());
        } else {
            message = String.format("%s has joined the game as %s.", auth.username(), team);
        }
        connections.broadcast(command.getAuthToken(), new NotificationMessage(message));
    }

    private void leave() {

    }

    private void resign() {

    }

    private void makeMove() {

    }
}