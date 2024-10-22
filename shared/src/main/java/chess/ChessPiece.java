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

        int[][] directionVectors = switch (type) {
            case KING -> new int[][]{{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};
            case QUEEN -> new int[][]{{1, 1}, {-1, 1}, {-1, -1}, {1, -1}, {1, 0}, {0, 1}, {-1, 0}, {0, -1}};
            case BISHOP -> new int[][]{{1, 1}, {-1, 1}, {-1, -1}, {1, -1}};
            case KNIGHT -> new int[][]{{2, -1}, {2, 1}, {1, 2}, {-1, 2}, {-2, 1}, {-2, -1}, {-1, -2}, {1, -2}};
            case ROOK -> new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
            default -> null;
        };

        int maxSteps = switch (type) {
            case QUEEN, ROOK, BISHOP -> 7;
            default -> 1;
        };

        return calculateMoves(board, myPosition, directionVectors, maxSteps);
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

    public HashSet<ChessMove> calculateMoves(ChessBoard board, ChessPosition myPosition, int[][] directionVectors, int maxSteps) {
        HashSet<ChessMove> moves = new HashSet<>();
        for (int[] directionVector : directionVectors) {
            for (int i = 1; i <= maxSteps; i++) {
                ChessPosition candidate = new ChessPosition(myPosition.getRow() + directionVector[0] * i, myPosition.getColumn() + directionVector[1] * i);
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
        int teamDirection = (pieceColor == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int pawnStartRow = (pieceColor == ChessGame.TeamColor.WHITE) ? 2 : 7;

        ChessPosition leftAttack = new ChessPosition(myPosition.getRow() + teamDirection, myPosition.getColumn() - 1);
        ChessPosition rightAttack = new ChessPosition(myPosition.getRow() + teamDirection, myPosition.getColumn() + 1);
        ChessPosition singleAdvance = new ChessPosition(myPosition.getRow() + teamDirection, myPosition.getColumn());
        ChessPosition doubleAdvance = new ChessPosition(myPosition.getRow() + 2 * teamDirection, myPosition.getColumn());

        HashSet<ChessPosition> validPosition = new HashSet<>();

        if (isEmpty(board, singleAdvance)) {
            validPosition.add(singleAdvance);
        }

        if (myPosition.getRow() == pawnStartRow && isEmpty(board, singleAdvance) && isEmpty(board, doubleAdvance)) {
            validPosition.add(doubleAdvance);
        }

        if (isEnemyOwned(board, leftAttack)) {
            validPosition.add(leftAttack);
        }

        if (isEnemyOwned(board, rightAttack)) {
            validPosition.add(rightAttack);
        }

        PieceType[] promotionOptions = {null};
        if (myPosition.getRow() + teamDirection == pawnStartRow + 6 * teamDirection) {
            promotionOptions = new PieceType[]{PieceType.QUEEN, PieceType.BISHOP, PieceType.ROOK, PieceType.KNIGHT};
        }

        for (ChessPosition targetPosition : validPosition) {
            for (PieceType option : promotionOptions) {
                moves.add(new ChessMove(myPosition, targetPosition, option));
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
