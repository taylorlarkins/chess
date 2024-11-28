package websocket;

import com.google.gson.Gson;
import ui.ClientException;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

import static websocket.commands.UserGameCommand.CommandType.CONNECT;

public class WebSocketFacade extends Endpoint {
    private Session session;
    private NotificationHandler notificationHandler;

    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws ClientException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler((MessageHandler.Whole<String>) message -> {
                ServerMessage notification = deserializeMessage(message);
                notificationHandler.notify(notification);
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