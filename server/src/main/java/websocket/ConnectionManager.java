package websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

public class ConnectionManager {
    // CHANGE TO BE BASED OFF OF GAME ID
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public void add(String authToken, Session session) {
        var connection = new Connection(authToken, session);
        connections.put(authToken, connection);
    }

    public void remove(String username) {
        connections.remove(username);
    }

    public void broadcast(String rootUserAuthToken, ServerMessage notification) throws IOException {
        ArrayList<Connection> oldConnections = new ArrayList<>();
        for (Connection conn : connections.values()) {
            if (conn.session.isOpen()) {
                if (!conn.authToken.equals(rootUserAuthToken)) {
                    conn.send(notification.toString());
                }
            } else {
                oldConnections.add(conn);
            }
        }

        for (Connection conn : oldConnections) {
            connections.remove(conn.authToken);
        }
    }
}
