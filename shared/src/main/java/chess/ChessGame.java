package chess;

import java.util.Collection;
import java.util.HashSet;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor turn;
    private ChessBoard board;
    private ChessPosition white_king_loc;
    private ChessPosition black_king_loc;

    public ChessGame() {
        turn = TeamColor.WHITE;
        board = new ChessBoard();
        board.resetBoard();
        white_king_loc = new ChessPosition(1, 5);
        black_king_loc = new ChessPosition(8, 5);
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        turn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) return null;
        TeamColor teamColor = piece.getTeamColor();
        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
        HashSet<ChessMove> legal_moves = new HashSet<>();
        for (ChessMove move : moves) {
            ChessPiece piece_at_target = board.getPiece(move.getEndPosition());
            board.movePiece(piece, move);
            if (piece.getPieceType() == ChessPiece.PieceType.KING) {
                updateKingLocation(piece.getTeamColor(), move.getEndPosition());
            }
            if (!isInCheck(teamColor)) {
                legal_moves.add(move);
            }
            if (piece.getPieceType() == ChessPiece.PieceType.KING) {
                updateKingLocation(piece.getTeamColor(), move.getStartPosition());
            }
            board.unMovePiece(piece, move, piece_at_target);
        }
        return legal_moves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        Collection<ChessMove> legal_moves = validMoves(move.getStartPosition());
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (legal_moves == null) {
            throw new InvalidMoveException(String.format("No valid moves starting from position %s.", move.getStartPosition()));
        } else if (turn != piece.getTeamColor()) {
            throw new InvalidMoveException(String.format("%s attempted to make a move but it is not their turn.", piece.getTeamColor()));
        } else if (!legal_moves.contains(move)) {
            throw new InvalidMoveException("Given move is invalid!");
        }
        board.movePiece(piece, move);
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            updateKingLocation(turn, move.getEndPosition());
        }
        setTeamTurn((turn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE);
    }

    public ChessPosition locateKing(TeamColor teamColor) {
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    return pos;
                }
            }
        }
        return null;
    }

    public void updateKingLocation(TeamColor teamColor, ChessPosition pos) {
        if (teamColor == TeamColor.WHITE) {
            white_king_loc = pos;
        } else {
            black_king_loc = pos;
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition king_location = (teamColor == TeamColor.WHITE) ? white_king_loc : black_king_loc;
        if (king_location == null) return false;
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition position = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(position);
                if (piece == null || piece.getTeamColor() == teamColor) continue;
                Collection<ChessMove> moves = piece.pieceMoves(board, position);
                for (ChessMove move : moves) {
                    if (move.getEndPosition().equals(king_location)) {
                        return true;
                    }
                }

            }
        }
        return false;
    }

    public boolean noValidMoves(TeamColor teamColor) {
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition position = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(position);
                if (piece == null || piece.getTeamColor() != teamColor) continue;
                Collection<ChessMove> valid_moves = validMoves(position);
                if (!valid_moves.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && noValidMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && noValidMoves(teamColor);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
        white_king_loc = locateKing(TeamColor.WHITE);
        black_king_loc = locateKing(TeamColor.BLACK);
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
