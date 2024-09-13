package chess;

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

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
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
        if (type == PieceType.PAWN) {
            return getPawnMoves(board, myPosition);
        }
        int[][] direction_vectors = switch (type) {
            case KING -> new int[][]{{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};
            case QUEEN -> new int[][]{{1, 1}, {-1, 1}, {-1, -1}, {1, -1}, {1, 0}, {0, 1}, {-1, 0}, {0, -1}};
            case BISHOP -> new int[][]{{1, 1}, {-1, 1}, {-1, -1}, {1, -1}};
            case KNIGHT -> new int[][]{{2, -1}, {2, 1}, {1, 2}, {-1, 2}, {-2, 1}, {-2, -1}, {-1, -2}, {1, -2}};
            case ROOK -> new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
            default -> null;
        };
        int max_steps = switch (type) {
            case QUEEN, ROOK, BISHOP -> 7;
            default -> 1;
        };
        return calculateMoves(board, myPosition, direction_vectors, max_steps);
    }

    public boolean isEnemyOwned(ChessBoard board, ChessPosition pos) {
        if (outOfBounds(pos)) return false;
        ChessPiece piece = board.getPiece(pos);
        return piece != null && piece.getTeamColor() != pieceColor;
    }

    public boolean isEmpty(ChessBoard board, ChessPosition pos) {
        if (outOfBounds(pos)) return false;
        return board.getPiece(pos) == null;
    }

    public boolean outOfBounds(ChessPosition pos) {
        int r = pos.getRow();
        int c = pos.getColumn();
        return 1 > r || r > 8 || 1 > c || c > 8;
    }

    public boolean isValidTarget(ChessBoard board, ChessPosition pos) {
        if (outOfBounds(pos)) return false;
        ChessPiece pieceAtTarget = board.getPiece(pos);
        return pieceAtTarget == null || pieceAtTarget.getTeamColor() != pieceColor;
    }

    public boolean shouldPromote(ChessPosition pos) {
        if (type != PieceType.PAWN) return false;
        if (pieceColor == ChessGame.TeamColor.WHITE) {
            return pos.getRow() == 8;
        } else {
            return pos.getRow() == 1;
        }
    }

    public HashSet<ChessMove> calculateMoves(ChessBoard board, ChessPosition myPosition, int[][] direction_vectors, int max_steps) {
        HashSet<ChessMove> moves = new HashSet<>();

        for (int[] direction_vector : direction_vectors) {
            for (int i = 1; i <= max_steps; i++) {
                ChessPosition candidate = new ChessPosition(myPosition.getRow() + direction_vector[0] * i, myPosition.getColumn() + direction_vector[1] * i);
                if (isValidTarget(board, candidate)) {
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

    public HashSet<ChessMove> getPawnMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> moves = new HashSet<>();
        int team_direction = (pieceColor == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int pawn_start_row = (pieceColor == ChessGame.TeamColor.WHITE) ? 2 : 7;

        ChessPosition left_attack = new ChessPosition(myPosition.getRow() + team_direction, myPosition.getColumn() - 1);
        ChessPosition right_attack = new ChessPosition(myPosition.getRow() + team_direction, myPosition.getColumn() + 1);
        ChessPosition single_advance = new ChessPosition(myPosition.getRow() + team_direction, myPosition.getColumn());
        ChessPosition double_advance = new ChessPosition(myPosition.getRow() + 2 * team_direction, myPosition.getColumn());

        HashSet<ChessPosition> validPosition = new HashSet<>();

        if (isEmpty(board, single_advance)) {
            validPosition.add(single_advance);
        }

        if (myPosition.getRow() == pawn_start_row && isEmpty(board, single_advance) && isEmpty(board, double_advance)) {
            validPosition.add(double_advance);
        }

        if (isEnemyOwned(board, left_attack)) {
            validPosition.add(left_attack);
        }

        if (isEnemyOwned(board, right_attack)) {
            validPosition.add(right_attack);
        }

        PieceType[] promotion_options = {null};
        if (shouldPromote(single_advance)) {
            promotion_options = new PieceType[]{PieceType.QUEEN, PieceType.BISHOP, PieceType.ROOK, PieceType.KNIGHT};
        }

        for (ChessPosition target_position : validPosition) {
            for (PieceType piece : promotion_options) {
                moves.add(new ChessMove(myPosition, target_position, piece));
            }
        }

        return moves;
    }

    @Override
    public String toString() {
        String result = switch (type) {
            case KING -> "K";
            case QUEEN -> "Q";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case ROOK -> "R";
            case PAWN -> "P";
        };

        return (pieceColor == ChessGame.TeamColor.WHITE) ? result : result.toLowerCase();
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
