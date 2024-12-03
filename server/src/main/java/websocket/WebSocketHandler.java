package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Collection;

import static websocket.commands.UserGameCommand.CommandType.MAKE_MOVE;

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
        if (command.getCommandType() == MAKE_MOVE) {
            makeMove(new Gson().fromJson(message, MakeMoveCommand.class));
        } else {
            switch (command.getCommandType()) {
                case CONNECT -> connect(session, command);
                case LEAVE -> leave(command);
                case RESIGN -> resign(command);
            }
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

    private void resign(UserGameCommand command) throws DataAccessException, IOException {
        String username = authDAO.getAuth(command.getAuthToken()).username();
        String message = String.format("%s has resigned!", username);
        connections.inform(command.getGameID(), command.getAuthToken(), new NotificationMessage(message));
        connections.broadcast(command.getGameID(), command.getAuthToken(), new NotificationMessage(message));
    }

    private void makeMove(MakeMoveCommand command) throws DataAccessException, IOException {
        GameData gameData = gameDAO.getGame(command.getGameID());
        ChessGame game = gameData.game();
        ChessMove move = command.getMove();
        ChessGame.TeamColor turnColor = game.getTeamTurn();
        String username = authDAO.getAuth(command.getAuthToken()).username();
        String opposingUsername = null;
        ChessGame.TeamColor userColor = null;
        ChessGame.TeamColor opposingColor = null;
        if (username.equals(gameData.whiteUsername())) {
            userColor = ChessGame.TeamColor.WHITE;
            opposingColor = ChessGame.TeamColor.BLACK;
            opposingUsername = gameData.blackUsername();
        }
        if (username.equals(gameData.blackUsername())) {
            userColor = ChessGame.TeamColor.BLACK;
            opposingColor = ChessGame.TeamColor.WHITE;
            opposingUsername = gameData.whiteUsername();
        }

        if (turnColor != userColor) {
            connections.inform(command.getGameID(), command.getAuthToken(), new ErrorMessage("It is not your turn!"));
        } else {
            try {
                game.makeMove(move);
                gameDAO.updateGame(gameData);
                String message = String.format("%s made the following move: %s", username, move);
                NotificationMessage moveNotification = new NotificationMessage(message);
                connections.broadcast(command.getGameID(), command.getAuthToken(), moveNotification);
                connections.inform(command.getGameID(), command.getAuthToken(), moveNotification);
                if (game.isInCheckmate(opposingColor)) {
                    message = String.format("%s is in checkmate! Good game!", opposingUsername);
                    NotificationMessage checkmateNotification = new NotificationMessage(message);
                    connections.broadcast(command.getGameID(), command.getAuthToken(), checkmateNotification);
                    connections.inform(command.getGameID(), command.getAuthToken(), checkmateNotification);
                } else if (game.isInStalemate(opposingColor)) {
                    message = "Stalemate! The game is over!";
                    NotificationMessage stalemateNotification = new NotificationMessage(message);
                    connections.broadcast(command.getGameID(), command.getAuthToken(), stalemateNotification);
                    connections.inform(command.getGameID(), command.getAuthToken(), stalemateNotification);
                } else if (game.isInCheck(opposingColor)) {
                    message = String.format("%s is in check!", opposingUsername);
                    NotificationMessage checkNotification = new NotificationMessage(message);
                    connections.broadcast(command.getGameID(), command.getAuthToken(), checkNotification);
                    connections.inform(command.getGameID(), command.getAuthToken(), checkNotification);
                }
            } catch (InvalidMoveException ex) {
                connections.inform(command.getGameID(), command.getAuthToken(), new ErrorMessage(ex.getMessage()));
            }
        }
    }
}