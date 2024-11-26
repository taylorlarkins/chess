package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;

@WebSocket
public class WebSocketHandler {

    private final websocket.ConnectionManager connections = new websocket.ConnectionManager();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        // TODO: figure out a way to properly deserialize messages
        UserGameCommand command = null; // replace with deserialized obj
        switch (command.getCommandType()) {
            case CONNECT -> connect();
            case LEAVE -> leave();
            case RESIGN -> resign();
        }
    }

    private void connect() {

    }

    private void leave() {

    }

    private void resign() {

    }

    private void makeMove() {

    }
}