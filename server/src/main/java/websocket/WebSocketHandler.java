package websocket;

import chess.ChessGame;
import chess.ChessMove;
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

import java.io.IOException;

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
            makeMove(new Gson().fromJson(message, MakeMoveCommand.class), session);
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
        if (auth == null) {
            connections.inform(command.getGameID(), command.getAuthToken(), new ErrorMessage("Error: unauthorized."));
        } else if (game == null) {
            connections.inform(command.getGameID(), command.getAuthToken(), new ErrorMessage("Error: invalid game id."));
        } else {
            String message = getMessage(auth, game);
            connections.inform(command.getGameID(), command.getAuthToken(), new LoadGameMessage(game.game()));
            connections.broadcast(command.getGameID(), command.getAuthToken(), new NotificationMessage(message));
        }
    }

    private String getMessage(AuthData auth, GameData game) {
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
        return message;
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
        GameData gameData = gameDAO.getGame(command.getGameID());
        String username = authDAO.getAuth(command.getAuthToken()).username();
        if (gameData.game().isOver()) {
            connections.inform(command.getGameID(), command.getAuthToken(), new ErrorMessage("Error: this game has already ended!"));
        } else if (username.equals(gameData.whiteUsername()) || username.equals(gameData.blackUsername())) {
            gameData.game().setOver(true);
            gameDAO.updateGame(gameData);
            String message = String.format("%s has resigned!", username);
            connections.inform(command.getGameID(), command.getAuthToken(), new NotificationMessage(message));
            connections.broadcast(command.getGameID(), command.getAuthToken(), new NotificationMessage(message));
        } else {
            connections.inform(command.getGameID(), command.getAuthToken(), new ErrorMessage("Error: you must be a player to resign!"));
        }
    }

    private void makeMove(MakeMoveCommand command, Session session) throws DataAccessException, IOException {
        GameData gameData = gameDAO.getGame(command.getGameID());
        AuthData authData = authDAO.getAuth(command.getAuthToken());
        if (authData == null) {
            connections.add(command.getGameID(), command.getAuthToken(), session);
            connections.inform(command.getGameID(), command.getAuthToken(), new ErrorMessage("Error: unauthorized."));
        } else if (gameData == null) {
            connections.inform(command.getGameID(), command.getAuthToken(), new ErrorMessage("Error: invalid game id."));
        } else if (gameData.game().isOver()) {
            connections.inform(command.getGameID(), command.getAuthToken(), new ErrorMessage("Error: the game is over!"));
        } else {
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
                connections.inform(command.getGameID(), command.getAuthToken(), new ErrorMessage("Error: It is not your turn!"));
            } else {
                try {
                    game.makeMove(move);
                    String message = String.format("%s made the following move: %s", username, move);
                    NotificationMessage moveNotification = new NotificationMessage(message);
                    NotificationMessage additionalNotification = null;
                    if (game.isInCheckmate(opposingColor)) {
                        message = String.format("%s is in checkmate! Good game!", opposingUsername);
                        additionalNotification = new NotificationMessage(message);
                        game.setOver(true);
                    } else if (game.isInStalemate(opposingColor)) {
                        message = "Stalemate! The game is over!";
                        additionalNotification = new NotificationMessage(message);
                        game.setOver(true);
                    } else if (game.isInCheck(opposingColor)) {
                        message = String.format("%s is in check!", opposingUsername);
                        additionalNotification = new NotificationMessage(message);
                    }
                    gameDAO.updateGame(gameData);
                    connections.broadcast(command.getGameID(), command.getAuthToken(), new LoadGameMessage(game));
                    connections.inform(command.getGameID(), command.getAuthToken(), new LoadGameMessage(game));
                    connections.broadcast(command.getGameID(), command.getAuthToken(), moveNotification);
                    if (additionalNotification != null) {
                        connections.broadcast(command.getGameID(), command.getAuthToken(), additionalNotification);
                        connections.inform(command.getGameID(), command.getAuthToken(), additionalNotification);
                    }
                } catch (InvalidMoveException ex) {
                    connections.inform(command.getGameID(), command.getAuthToken(), new ErrorMessage("Error: " + ex.getMessage()));
                }
            }
        }
    }
}