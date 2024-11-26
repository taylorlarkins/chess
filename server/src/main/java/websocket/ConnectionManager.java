package websocket;

import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.Session;

public class ConnectionManager {
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public void add(String username, Session session) {
        var connection = new Connection(username, session);
        connections.put(username, connection);
    }

    public void remove(String username) {
        connections.remove(username);
    }

    public void broadcast() {
        //TODO
    }
}
