package server;

import model.GameData;

public record ListGamesResponse(GameData[] games) {
}
