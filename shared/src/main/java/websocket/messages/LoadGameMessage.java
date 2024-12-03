package websocket.messages;

import chess.ChessGame;

public class LoadGameMessage extends ServerMessage {
    private final ChessGame game;
    private final boolean whitePerspective;

    public LoadGameMessage(ChessGame game, boolean whitePerspective) {
        super(ServerMessageType.LOAD_GAME);
        this.game = game;
        this.whitePerspective = whitePerspective;
    }

    public ChessGame getChessGame() {
        return game;
    }

    public boolean whitePerspective() {
        return whitePerspective;
    }
}