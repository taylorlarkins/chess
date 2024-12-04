package ui;

import chess.*;

import java.util.Collection;
import java.util.HashSet;

import static ui.EscapeSequences.*;

public class GamePrinter {
    public final String[] checkerColors = {SET_BG_COLOR_BLACK, SET_BG_COLOR_WHITE};
    public final String[] highlightColors = {SET_BG_COLOR_DARK_GREEN, SET_BG_COLOR_GREEN};

    public void printGame(ChessGame game, Role role, ChessPosition highlightSource) {
        String perspective;
        String fileLabels;
        boolean whitePerspective = true;
        ChessBoard board = game.getBoard();
        if (role == Role.WHITE_PLAYER || role == Role.OBSERVER) {
            perspective = SET_TEXT_COLOR_WHITE;
            fileLabels = "    a  b  c  d  e  f  g  h    ";
        } else {
            perspective = SET_TEXT_COLOR_BLACK;
            fileLabels = "    h  g  f  e  d  c  b  a    ";
            whitePerspective = false;
        }
        System.out.print(perspective + SET_TEXT_BOLD + SET_BG_COLOR_LIGHT_GREY);
        System.out.println(SET_BG_COLOR_LIGHT_GREY + fileLabels + RESET_BG_COLOR);
        if (highlightSource == null) {
            printSquaresAndPieces(board, whitePerspective, perspective);
        } else {
            printHighlighting(game, whitePerspective, perspective, highlightSource);
        }
        System.out.print(SET_BG_COLOR_LIGHT_GREY + fileLabels + RESET_BG_COLOR);
        System.out.println(RESET_TEXT_COLOR + RESET_TEXT_BOLD_FAINT);
    }

    private void printSquaresAndPieces(ChessBoard board, boolean whitePerspective, String perspective) {
        if (whitePerspective) {
            for (int row = 8; row >= 1; row--) {
                System.out.print(SET_BG_COLOR_LIGHT_GREY + perspective);
                System.out.print(" " + row + " " + RESET_BG_COLOR);
                for (int col = 1; col <= 8; col++) {
                    System.out.print(checkerColors[(col + row) % 2]);
                    System.out.print(" " + getPiece(board, row, col) + " ");
                }
                System.out.println(SET_BG_COLOR_LIGHT_GREY + perspective + " " + row + " " + RESET_BG_COLOR);
            }
        } else {
            for (int row = 1; row <= 8; row++) {
                System.out.print(SET_BG_COLOR_LIGHT_GREY + perspective);
                System.out.print(" " + row + " " + RESET_BG_COLOR);
                for (int col = 8; col >= 1; col--) {
                    System.out.print(checkerColors[(col + row) % 2]);
                    System.out.print(" " + getPiece(board, row, col) + " ");
                }
                System.out.println(SET_BG_COLOR_LIGHT_GREY + perspective + " " + row + " " + RESET_BG_COLOR);
            }
        }
    }

    private void printHighlighting(ChessGame game, boolean whitePerspective, String perspective, ChessPosition highlightingSource) {
        ChessBoard board = game.getBoard();
        Collection<ChessMove> validMoves = game.validMoves(highlightingSource);
        HashSet<ChessPosition> validEndPositions = new HashSet<>();
        for (ChessMove pos : validMoves) {
            validEndPositions.add(pos.getEndPosition());
        }

        if (whitePerspective) {
            for (int row = 8; row >= 1; row--) {
                System.out.print(SET_BG_COLOR_LIGHT_GREY + perspective);
                System.out.print(" " + row + " " + RESET_BG_COLOR);
                for (int col = 1; col <= 8; col++) {
                    ChessPosition candidatePosition = new ChessPosition(row, col);
                    if (candidatePosition.equals(highlightingSource)) {
                        System.out.print(SET_BG_COLOR_YELLOW);
                    } else if (validEndPositions.contains(candidatePosition)) {
                        System.out.print(highlightColors[(col + row) % 2]);
                    } else {
                        System.out.print(checkerColors[(col + row) % 2]);
                    }
                    System.out.print(" " + getPiece(board, row, col) + " ");
                }
                System.out.println(SET_BG_COLOR_LIGHT_GREY + perspective + " " + row + " " + RESET_BG_COLOR);
            }
        } else {
            for (int row = 1; row <= 8; row++) {
                System.out.print(SET_BG_COLOR_LIGHT_GREY + perspective);
                System.out.print(" " + row + " " + RESET_BG_COLOR);
                for (int col = 8; col >= 1; col--) {
                    ChessPosition candidatePosition = new ChessPosition(row, col);
                    if (candidatePosition.equals(highlightingSource)) {
                        System.out.print(SET_BG_COLOR_YELLOW);
                    } else if (validEndPositions.contains(candidatePosition)) {
                        System.out.print(highlightColors[(col + row) % 2]);
                    } else {
                        System.out.print(checkerColors[(col + row) % 2]);
                    }
                    System.out.print(" " + getPiece(board, row, col) + " ");
                }
                System.out.println(SET_BG_COLOR_LIGHT_GREY + perspective + " " + row + " " + RESET_BG_COLOR);
            }
        }
    }

    private String getPiece(ChessBoard board, int row, int col) {
        ChessPiece piece = board.getPiece(new ChessPosition(row, col));
        if (piece == null) {
            return " ";
        }
        String color;
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            color = SET_TEXT_COLOR_BLUE;
        } else {
            color = SET_TEXT_COLOR_RED;
        }
        return color + piece;
    }
}
