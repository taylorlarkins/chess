package chess;

import java.util.ArrayList;
import java.util.HashSet;
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
        switch (type) {
            case KING:
                return kingAndKnightMoves(board, myPosition, true);
            case QUEEN:
                HashSet<ChessMove> moves = rookAndBishopMoves(board, myPosition, true);
                moves.addAll(rookAndBishopMoves(board, myPosition, false));
                return moves;
            case BISHOP:
                return rookAndBishopMoves(board, myPosition, false);
            case KNIGHT:
                return kingAndKnightMoves(board, myPosition, false);
            case ROOK:
                return rookAndBishopMoves(board, myPosition, true);
            case PAWN:
            default:
                return null;
        }
    }

    public boolean isSelfOwned(ChessBoard board, ChessPosition pos) {
        ChessPiece piece = board.getPiece(pos);
        return piece != null && piece.getTeamColor() == pieceColor;
    }

    public boolean isEnemyOwned(ChessBoard board, ChessPosition pos) {
        ChessPiece piece = board.getPiece(pos);
        return piece != null && piece.getTeamColor() != pieceColor;
    }

    public boolean isInbounds(ChessPosition pos) {
        int r = pos.getRow();
        int c = pos.getColumn();
        return 1 <= r && r <= 8 && 1 <= c && c <= 8;
    }

    public HashSet<ChessMove> kingAndKnightMoves(ChessBoard board, ChessPosition myPosition, boolean isKing) {
        HashSet<ChessMove> moves = new HashSet<>();
        int[][] directions;
        if (isKing) {
            directions = new int[][]{{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};
        } else {
            directions = new int[][]{{2, -1}, {2, 1}, {1, 2}, {-1, 2}, {-2, 1}, {-2, -1}, {-1, -2}, {1, -2}};
        }
        for (int[] direction : directions) {
            ChessPosition candidate = new ChessPosition(myPosition.getRow() + direction[0], myPosition.getColumn() + direction[1]);
            if (isInbounds(candidate) && !isSelfOwned(board, candidate)) {
                moves.add(new ChessMove(myPosition, candidate, null));
            }
        }
        return moves;
    }

    public HashSet<ChessMove> rookAndBishopMoves(ChessBoard board, ChessPosition myPosition, boolean isRook) {
        HashSet<ChessMove> moves = new HashSet<>();
        int[][] direction_vectors;
        if (isRook) {
            direction_vectors = new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
        } else {
            direction_vectors = new int[][]{{1, 1}, {-1, 1}, {-1, -1}, {1, -1}};
        }
        for (int[] direction_vector : direction_vectors) {
            for (int i = 1; i < 8; i++) {
                ChessPosition candidate = new ChessPosition(myPosition.getRow() + direction_vector[0] * i, myPosition.getColumn() + direction_vector[1] * i);
                if (isInbounds(candidate) && !isSelfOwned(board, candidate)) {
                    moves.add(new ChessMove(myPosition, candidate, null));
                    if (isEnemyOwned(board, candidate)) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return moves;
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
