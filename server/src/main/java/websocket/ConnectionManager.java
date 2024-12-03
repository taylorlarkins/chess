package websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

public class ConnectionManager {
    public final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Connection>> connections = new ConcurrentHashMap<>();

    public void add(Integer gameID, String authToken, Session session) {
        Connection connection = new Connection(authToken, session);
        if (!connections.containsKey(gameID)) {
            connections.put(gameID, new ConcurrentHashMap<>());
        }
        connections.get(gameID).put(authToken, connection);
    }

    public void remove(Integer gameID, String authToken) {
        ConcurrentHashMap<String, Connection> gameConnections = connections.get(gameID);
        gameConnections.remove(authToken);
    }

    public void broadcast(Integer gameID, String rootUserAuthToken, ServerMessage notification) throws IOException {
        ArrayList<Connection> oldConnections = new ArrayList<>();
        ConcurrentHashMap<String, Connection> gameConnections = connections.get(gameID);
        for (Connection conn : gameConnections.values()) {
            if (conn.session.isOpen()) {
                if (!conn.authToken.equals(rootUserAuthToken)) {
                    conn.send(notification.toString());
                }
            } else {
                oldConnections.add(conn);
            }
        }
        for (Connection conn : oldConnections) {
            gameConnections.remove(conn.authToken);
        }
        if (gameConnections.isEmpty()) {
            connections.remove(gameID);
        }
    }
}
