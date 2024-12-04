package websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import ui.ClientException;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

import static websocket.commands.UserGameCommand.CommandType.*;

public class WebSocketFacade extends Endpoint {
    private final Session session;

    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws ClientException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            //noinspection Convert2Lambda
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage notification = deserializeMessage(message);
                    notificationHandler.notify(notification);
                }
            });
        } catch (Exception ex) {
            throw new ClientException(500, ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void sendConnect(String authToken, int gameID) throws ClientException {
        try {
            UserGameCommand command = new UserGameCommand(CONNECT, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new ClientException(500, ex.getMessage());
        }
    }

    public void sendLeave(String authToken, int gameID) throws ClientException {
        try {
            UserGameCommand command = new UserGameCommand(LEAVE, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new ClientException(500, ex.getMessage());
        }
    }

    public void sendResign(String authToken, int gameID) throws ClientException {
        try {
            UserGameCommand command = new UserGameCommand(RESIGN, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new ClientException(500, ex.getMessage());
        }
    }

    public void sendMakeMove(String authToken, int gameID, ChessMove move) throws ClientException {
        try {
            UserGameCommand command = new MakeMoveCommand(MAKE_MOVE, authToken, gameID, move);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new ClientException(500, ex.getMessage());
        }
    }

    private ServerMessage deserializeMessage(String message) {
        ServerMessage.ServerMessageType messageType = new Gson().fromJson(message, ServerMessage.class).getServerMessageType();
        Class<?> clazz = switch (messageType) {
            case LOAD_GAME -> LoadGameMessage.class;
            case ERROR -> ErrorMessage.class;
            case NOTIFICATION -> NotificationMessage.class;
        };
        return (ServerMessage) new Gson().fromJson(message, clazz);
    }
}
