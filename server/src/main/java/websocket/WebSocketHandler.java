package websocket;

import com.google.gson.Gson;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;
import websocket.messages.LoadGameMessage;
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
            case LEAVE -> leave(command);
            case RESIGN -> resign();
        }
    }

    private void connect(Session session, UserGameCommand command) throws DataAccessException, IOException {
        connections.add(command.getGameID(), command.getAuthToken(), session);
        AuthData auth = authDAO.getAuth(command.getAuthToken());
        GameData game = gameDAO.getGame(command.getGameID());
        String team = null;
        boolean whitePerspective = true;
        if (auth.username().equals(game.whiteUsername())) {
            team = "WHITE";
        } else if (auth.username().equals(game.blackUsername())) {
            team = "BLACK";
            whitePerspective = false;
        }
        String message;
        if (team == null) {
            message = String.format("%s has joined the game as an observer.", auth.username());
        } else {
            message = String.format("%s has joined the game as %s.", auth.username(), team);
        }
        connections.inform(command.getGameID(), command.getAuthToken(), new LoadGameMessage(game.game(), whitePerspective));
        connections.broadcast(command.getGameID(), command.getAuthToken(), new NotificationMessage(message));
    }

    private void leave(UserGameCommand command) throws DataAccessException, IOException {
        String username = authDAO.getAuth(command.getAuthToken()).username();
        GameData game = gameDAO.getGame(command.getGameID());
        String whiteUser = game.whiteUsername();
        String blackUser = game.blackUsername();
        if (username.equals(whiteUser)) {
            whiteUser = null;
        }
        if (username.equals(blackUser)) {
            blackUser = null;
        }
        GameData updatedGame = new GameData(
                game.gameID(),
                whiteUser,
                blackUser,
                game.gameName(),
                game.game()
        );
        gameDAO.updateGame(updatedGame);
        connections.remove(command.getGameID(), command.getAuthToken());
        String message = String.format("%s has left the game.", username);
        connections.broadcast(command.getGameID(), command.getAuthToken(), new NotificationMessage(message));
    }

    private void resign() {

    }

    private void makeMove() {

    }
}