package ui;

import com.google.gson.Gson;
import model.AuthData;
import model.UserData;
import request.CreateGameRequest;
import request.LoginRequest;
import response.CreateGameResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public void clear() {
        try {
            String path = "/db";
            makeRequest("DELETE", path, null, null, null);
        } catch (ClientException ignore) {
        }
    }

    public AuthData register(UserData user) throws ClientException {
        String path = "/user";
        return makeRequest("POST", path, user, AuthData.class, null);
    }

    public AuthData login(LoginRequest req) throws ClientException {
        String path = "/session";
        return makeRequest("POST", path, req, AuthData.class, null);
    }

    public void logout(String authToken) throws ClientException {
        String path = "/session";
        makeRequest("DELETE", path, null, null, authToken);
    }

    public int createGame(CreateGameRequest req, String authToken) throws ClientException {
        String path = "/game";
        CreateGameResponse res = makeRequest("POST", path, req, CreateGameResponse.class, authToken);
        return res.gameID();
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) throws ClientException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);
            if (authToken != null) {
                writeAuthorizationHeader(authToken, http);
            }
            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw new ClientException(500, ex.getMessage());
        }
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private static void writeAuthorizationHeader(String authToken, HttpURLConnection http) {
        http.setRequestProperty("Authorization", authToken);
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ClientException {
        int status = http.getResponseCode();
        if (!isSuccessful(status)) {
            String message = switch (status) {
                case 400 -> "bad request";
                case 401 -> "unauthorized";
                case 403 -> "already taken";
                default -> "something went wrong.";
            };
            throw new ClientException(status, "Error: " + message);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
