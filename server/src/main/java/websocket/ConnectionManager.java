package websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

public class ConnectionManager {
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public void add(String username, Session session) {
        var connection = new Connection(username, session);
        connections.put(username, connection);
    }

    public void remove(String username) {
        connections.remove(username);
    }

    public void broadcast(String rootUser, ServerMessage notification) throws IOException {
        ArrayList<Connection> oldConnections = new ArrayList<Connection>();
        for (Connection conn : connections.values()) {
            if (conn.session.isOpen()) {
                if (!conn.username.equals(rootUser)) {
                    conn.send(notification.toString());
                }
            } else {
                oldConnections.add(conn);
            }
        }

        for (Connection conn : oldConnections) {
            connections.remove(conn.username);
        }
    }
}
