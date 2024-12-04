package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import static ui.EscapeSequences.*;

public class GamePrinter {
    public void printGame(ChessBoard board, Role role) {
        String perspective;
        String fileLabels;
        boolean whitePerspective = true;
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
        printSquaresAndPieces(board, whitePerspective, perspective);
        System.out.print(SET_BG_COLOR_LIGHT_GREY + fileLabels + RESET_BG_COLOR);
        System.out.println(RESET_TEXT_COLOR + RESET_TEXT_BOLD_FAINT);
    }

    private void printSquaresAndPieces(ChessBoard board, boolean whitePerspective, String perspective) {
        String[] checkerColors = {SET_BG_COLOR_BLACK, SET_BG_COLOR_WHITE};
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
