package ui;

import model.AuthData;
import model.UserData;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public AuthData register(UserData user) throws ClientException {
        String path = "/user";
        return null;
    }


}
