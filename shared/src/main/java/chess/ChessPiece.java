package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> moves = new ArrayList<ChessMove>();
        int r = myPosition.getRow();
        int c = myPosition.getColumn();

        switch (type) {
            case KING:
                moves = null;
                break;
            case QUEEN:
                moves = null;
                break;
            case BISHOP:
                moves = null;
                break;
            case KNIGHT:
                moves = null;
                break;
            case ROOK:
                moves = null;
                break;
            case PAWN:
                moves = null;
                break;
        }
        return moves;
    }

    public boolean isSelfOwned(ChessBoard board, ChessPosition pos) {
        ChessPiece piece = board.getPiece(pos);
        return piece != null && piece.getTeamColor() == pieceColor;
    }

    public boolean isInbounds(ChessPosition pos) {
        int r = pos.getRow();
        int c = pos.getColumn();
        return 1 < r && r < 8 && 1 < c && c < 8;
    }

    public boolean generateMove(ChessBoard board, ChessPosition pos) {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}
