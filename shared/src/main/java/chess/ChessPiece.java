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
        int[][] direction_vectors = switch (type) {
            case KING -> new int[][]{{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};
            case QUEEN -> new int[][]{{1, 1}, {-1, 1}, {-1, -1}, {1, -1}, {1, 0}, {0, 1}, {-1, 0}, {0, -1}};
            case BISHOP -> new int[][]{{1, 1}, {-1, 1}, {-1, -1}, {1, -1}};
            case KNIGHT -> new int[][]{{2, -1}, {2, 1}, {1, 2}, {-1, 2}, {-2, 1}, {-2, -1}, {-1, -2}, {1, -2}};
            case ROOK -> new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
            case PAWN -> getPawnDirectionVectors(board, myPosition);
        };
        int max_steps = switch (type) {
            case QUEEN, ROOK, BISHOP -> 7;
            default -> 1;
        };
        return calculateMoves(board, myPosition, direction_vectors, max_steps);
    }

    public boolean isEnemyOwned(ChessBoard board, ChessPosition pos) {
        ChessPiece piece = board.getPiece(pos);
        return piece != null && piece.getTeamColor() != pieceColor;
    }

    public boolean isEmpty(ChessBoard board, ChessPosition pos) {
        return board.getPiece(pos) == null;
    }

    public boolean isInbounds(ChessPosition pos) {
        int r = pos.getRow();
        int c = pos.getColumn();
        return 1 <= r && r <= 8 && 1 <= c && c <= 8;
    }

    public boolean isValidTarget(ChessBoard board, ChessPosition pos) {
        if (!isInbounds(pos)) return false;
        ChessPiece pieceAtTarget = board.getPiece(pos);
        return pieceAtTarget == null || pieceAtTarget.getTeamColor() != pieceColor;
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

    public int[][] getPawnDirectionVectors(ChessBoard board, ChessPosition myPosition) {
        ArrayList<int[]> direction_vectors = new ArrayList<>();
        int team_direction = (pieceColor == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int pawn_start_row = (pieceColor == ChessGame.TeamColor.WHITE) ? 2 : 7;
        ChessPosition left_attack = new ChessPosition(myPosition.getRow() + team_direction, myPosition.getColumn() - 1);
        ChessPosition right_attack = new ChessPosition(myPosition.getRow() + team_direction, myPosition.getColumn() + 1);
        ChessPosition single_advance = new ChessPosition(myPosition.getRow() + team_direction, myPosition.getColumn());
        ChessPosition double_advance = new ChessPosition(myPosition.getRow() + 2 * team_direction, myPosition.getColumn());
        if (isEmpty(board, single_advance)) {
            direction_vectors.add(new int[]{team_direction, 0});
        }
        if (myPosition.getRow() == pawn_start_row && isEmpty(board, single_advance) && !isEnemyOwned(board, double_advance)) {
            direction_vectors.add(new int[]{2 * team_direction, 0});
        }
        if (isEnemyOwned(board, left_attack)) {
            direction_vectors.add(new int[]{team_direction, -1});
        }
        if (isEnemyOwned(board, right_attack)) {
            direction_vectors.add(new int[]{team_direction, 1});
        }
        return direction_vectors.toArray(new int[direction_vectors.size()][]);
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
