import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.awt.Point;

public class PgnReader {

    /**
     * Find the tagName tag pair in a PGN game and return its value.
     *
     * @see http://www.saremba.de/chessgml/standards/pgn/pgn-complete.htm
     *
     * @param tagName the name of the tag whose value you want
     * @param game a `String` containing the PGN text of a chess game
     * @return the value in the named tag pair
     */
    public static String tagValue(String tagName, String game) {
        String pattern = tagName + " \"([a-zA-Z0-9, ]+)\"";
        // System.out.println(pattern);
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(game);
        String outstring = "";
        boolean found = false;
        while (m.find()) {
            found = true;
            outstring += m.group(1);
        }
        if (found) {
            return outstring;
        } else {
            return "NOT GIVEN";
        }
    }


    public static Point[] gpl(char[][] board, char pieceType, char color) {
        Point[] locs = new Point[2];
        int num = 0;
        if (color == 'b') {
            pieceType = Character.toLowerCase(pieceType);
        }
        // System.out.println("Color: " + color);
        // System.out.println("Piece Type: " + pieceType);
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                // System.out.println("Row: " + Integer.toString(i));
                // System.out.println("Column: " + Integer.toString(j));
                // System.out.println("Piece on Board: " + board[i][j]);
                if (board[i][j] == pieceType) {
                    locs[num] = new Point(i, j);
                    num += 1;
                }
            }
        }
        // System.out.println("INSERTED");
        // System.out.println(num);
        return locs;
    }

    public static String disProvider(String move) {
        Pattern p;
        p = Pattern.compile("[RBN]{1}([a-h1-8]?)x?[a-h]{1}[1-8]{1}\\S*");
        Matcher m = p.matcher(move);
        String disLoc = "-";
        if (m.find()) {
            disLoc = m.group(1);
        }
        return disLoc;
    }

    public static String getEndLoc(String move) {
        Pattern p;
        p = Pattern.compile("[QKsRBN]{1}[a-h1-8]?x?([a-h]{1}[1-8]{1})\\S*");
        Matcher m = p.matcher(move);
        while (m.find()) {
            return m.group(1);
        }
        return "0";
    }

    // Return the piece that can make the move required
    public static Point canMove(char[][] board, String move,
        Point[] locs, char pieceType, int endRow, int endCol) {
        if (pieceType == 'R' || pieceType == 'r') {
            // check if move provides which rook moved
            String disLoc = disProvider(move);
            if (disLoc != "-" && !disLoc.isEmpty()) {
                // find which one it came from
                if (Character.isDigit(disLoc.charAt(0))) {
                    /*a starting row was provided -- find
                    the rook currently on that row */
                    for (int i = 0; i < locs.length; i++) {
                        if ((int) locs[i].getX() == Integer.parseInt(disLoc)) {
                            return locs[i];
                        }
                    }
                } else {
                    /*a starting column was provided -- find
                    the rook currently in that col*/
                    for (int i = 0; i < locs.length; i++) {
                        if ((int) locs[i].getY()
                                == Character.getNumericValue(disLoc.charAt(0))
                                - 10) {
                            return locs[i];
                        }
                    }
                }
            }
            /* if it reaches this point, this means an initial row/col was not
            provided in the move*/
            /* at this point, iterate through the available rooks -- the first
            rook that can validly make the move is the one that made it --
            CAVEAT: only one rook may be able to move without
            jeopardizing king (ADD THIS LAST IF TIME AVAILABLE) */
            for (int i = 0; i < locs.length; i++) {
                // System.out.println("Location");
                /* System.out.println("Row: " +
                    Double.toString(locs[i].getX())); */
                /* System.out.println("Column: " +
                    Double.toString(locs[i].getY())); */
                if (endRow == (int) locs[i].getX()
                        || endCol == (int) locs[i].getY()) {
                    boolean clearPath = true;
                    /*check if there is anything
                        between initial pos of rook and end pos */
                    // first check if it rook moved vertically or horizontally
                    if (endRow == (int) locs[i].getX()) {
                        // this means it stayed on that same row
                        int colDelta = endCol - (int) locs[i].getY();
                        /* colDelta can be pos or neg. -- if pos,
                            it moved right. if neg, it moved left. */
                        if (colDelta > 0) {
                            // if rook moved right
                            for (int col = 1; col < colDelta + 1; col++) {
                                /*if there is anything other than -
                                    in the path, it is not a clear path and cannot move */
                                if (((int) locs[i].getY() + col) != endCol) {
                                    if (board[endRow][(int) locs[i].getY() + col] != '-') {
                                        clearPath = false;
                                    }
                                }
                            }
                        } else {
                            // if rook moved left
                            for (int col = 1; col < colDelta + 1; col++) {
                                if (((int) locs[i].getY() - col) != endCol) {
                                    if (board[endRow][(int) locs[i].getY() - col] != '-') {
                                        clearPath = false;
                                    }
                                }
                            }
                        }
                        if (clearPath) {
                            return locs[i];
                        }
                    } else {
                        // this means it stayed on the same column
                        int rowDelta = endRow - (int) locs[i].getX();
                        // rowDelta can be pos or neg. -- if pos, it moved up (towards black). if neg, it moved down (towards white)
                        if (rowDelta > 0) {
                            // rook moved up
                            for (int row = 1; row < rowDelta + 1; row++) {
                                if (((int) locs[i].getX() + row) != endRow) {
                                    if (board[(int) locs[i].getX() + row][endCol] != '-') {
                                        clearPath = false;
                                    }
                                }
                            }
                        } else {
                            // rook moved down
                            for (int row = 1; row < rowDelta + 1; row++) {
                                if (((int) locs[i].getX() - row) != endRow) {
                                    if (board[(int) locs[i].getX() - row][endCol] != '-') {
                                        clearPath = false;
                                    }
                                }
                            }
                        }
                        if (clearPath) {
                            return locs[i];
                        }
                    }
                }
            }
        } else if (pieceType == 'B' || pieceType == 'b') {
            String disLoc = disProvider(move);
            if (disLoc != "-" && !disLoc.isEmpty()) {
                // find which one it came from
                if (Character.isDigit(disLoc.charAt(0))) {
                    // a starting row was provided -- find the bishop currently on that row
                    for (int i = 0; i < locs.length; i++) {
                        if ((int) locs[i].getX() == Integer.parseInt(disLoc)) {
                            return locs[i];
                        }
                    }
                } else {
                    // a starting column was provided -- find the bishop currently in that col
                    for (int i = 0; i < locs.length; i++) {
                        if ((int) locs[i].getY() == Character.getNumericValue(disLoc.charAt(0)) - 10) {
                            return locs[i];
                        }
                    }
                }
            }
            // if it reaches this point, this means an initial row/col was not provided in the move
            // at this point, iterate through the available bishops -- the first bish that can validly make the move is the one that made it -- CAVEAT: only one bish may be able to move without jeopardizing king (ADD THIS LAST IF TIME AVAILABLE)
            for (int i = 0; i < locs.length; i++) {
                int rowDelta = Math.abs(endRow - (int) locs[i].getX());
                int colDelta = Math.abs(endCol - (int) locs[i].getY());
                if (rowDelta == colDelta) {
                    // check for clear path
                    int dirRowDelta = endRow - (int) locs[i].getX();
                    int dirColDelta = endCol - (int) locs[i].getY();
                    int rowAdd = 0;
                    int colAdd = 0;
                    boolean clearPath = true;
                    for (int x = 0; x < rowDelta; x++) {
                        rowAdd += dirRowDelta / rowDelta;
                        colAdd += dirColDelta / colDelta;
                        if (((int) locs[i].getX() + rowAdd != endRow) && ((int) locs[i].getY() + colAdd != endCol)) {
                            if (board[(int) locs[i].getX() + rowAdd][(int) locs[i].getY() + colAdd] != '-') {
                                clearPath = false;
                            }
                        }
                    }
                    if (clearPath) {
                        return locs[i];
                    }
                }
            }
        } else if (pieceType == 'N' || pieceType == 'n') {
            String disLoc = disProvider(move);
            if (disLoc != "-" && !disLoc.isEmpty()) {
                // find which one it came from
                if (Character.isDigit(disLoc.charAt(0))) {
                    // a starting row was provided -- find the knight currently on that row
                    for (int i = 0; i < locs.length; i++) {
                        if ((int) locs[i].getX() == Integer.parseInt(disLoc)) {
                            return locs[i];
                        }
                    }
                } else {
                    // a starting column was provided -- find the knight currently in that col
                    for (int i = 0; i < locs.length; i++) {
                        if ((int) locs[i].getY() == Character.getNumericValue(disLoc.charAt(0)) - 10) {
                            return locs[i];
                        }
                    }
                }
            }
            // if it reaches this point, this means an initial row/col was not provided in the move
            // at this point, iterate through the available knights -- the first knight that can validly make the move is the one that made it -- CAVEAT: only one knight may be able to move without jeopardizing king (ADD THIS LAST IF TIME AVAILABLE)
            for (int i = 0; i < locs.length; i++) {
                int rowDelta = Math.abs(endRow - (int) locs[i].getX());
                int colDelta = Math.abs(endCol - (int) locs[i].getY());
                if ((rowDelta == 2 && colDelta == 1) || (rowDelta == 1 && colDelta == 2)) {
                    return locs[i];
                }
            }
        }
        return new Point();
    }

    public static void debugBoard(char[][] board) {
        for (int i = 0; i < 8; i++) {
            String row = "";
            for (int j = 0; j < 8; j++) {
                row += board[i][j];
            }
            System.out.println(row);
        }
    }

    // Return the piece that can make the move required


    /**
     * Play out the moves in game and return a String with the game's
     * final position in Forsyth-Edwards Notation (FEN).
     *
     * @see http://www.saremba.de/chessgml/standards/pgn/pgn-complete.htm#c16.1
     *
     * @param game a `Strring` containing a PGN-formatted chess game or opening
     * @return the game's final position in FEN.
     */
    public static String finalPosition(String game) {
        String[] splitGame = game.split("\n");
        boolean sanStart = false;
        String sanText = "";
        for (int i = 0; i < splitGame.length; i++) {
            if (splitGame[i].trim().isEmpty()) {
                sanStart = true;
            }
            if (sanStart) {
                sanText = sanText + " " + splitGame[i];
            }
        }
        sanText = sanText.trim();
        // String movePattern = "\\d+\\. ([a-zA-ZO\\-0-9\\\\=+#]+\\s?[a-zA-Z0-9+#]+)";
        String movePattern = "\\d+\\. ([a-zA-ZO\\-0-9=+#?]+\\s?[a-zA-ZO\\-0-9=+#?]+)";
        Pattern p = Pattern.compile(movePattern);
        Matcher m = p.matcher(sanText);
        char[][] board = new char[8][8];
        char[][] prevMoveBoard = new char[8][8];
        String[] initial = "rnbqkbnr/pppppppp/--------/--------/--------/--------/PPPPPPPP/RNBQKBNR".split("/");
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = initial[i].charAt(j);
            }
        }
        int counter = 0;
        char color = 'o';
        boolean whiteKingRook = false;
        boolean whiteQueenRook = false;
        boolean whiteKing = false;
        boolean blackKingRook = false;
        boolean blackQueenRook = false;
        boolean blackKing = false;
        String lastMove = "";
        int pawnCounter = 0;
        while (m.find()) {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    prevMoveBoard[i][j] = board[i][j];
                }
            }
            counter += 1;
            String[] turnMoves = m.group(1).split(" ");
            // both moves specified
            if (turnMoves.length == 2) {
                color = 'w';
                // perform white move
                String whiteMove = turnMoves[0];
                if (Character.isLowerCase(whiteMove.charAt(0))) {
                    pawnCounter = 0;
                    // is pawn
                    if (whiteMove.contains("x")) {
                        // pawn moved diagonal
                        int startCol = Character.getNumericValue(whiteMove.charAt(0)) - 10;
                        int endCol = Character.getNumericValue(whiteMove.charAt(2)) - 10;
                        int row = 8 - Character.getNumericValue(whiteMove.charAt(3));
                        if (whiteMove.contains("=")) {
                            board[row][endCol] = whiteMove.charAt(whiteMove.indexOf('=') + 1);
                            board[row + 1][startCol] = '-';
                        } else {
                            board[row][endCol] = 'P';
                            board[row + 1][startCol] = '-';
                            // check if this is en passant pawn capture
                            if (row == 2) {
                                // check if one row below is a black pawn
                                if (board[row + 1][endCol] == 'p') {
                                    board[row + 1][endCol] = '-';
                                }
                            }
                        }
                    } else {
                        // pawn moved vertically
                        int col = Character.getNumericValue(whiteMove.charAt(0)) - 10;
                        int endRow = 8 - Character.getNumericValue(whiteMove.charAt(1));
                        boolean foundPawn = false;
                        int checkRow = endRow + 1;
                        while (!foundPawn) {
                            char pawn = board[checkRow][col];
                            if (pawn == 'P') {
                                foundPawn = true;
                                if (whiteMove.contains("=")) {
                                    board[checkRow][col] = '-';
                                    board[endRow][col] = whiteMove.charAt(whiteMove.indexOf('=') + 1);
                                } else {
                                    board[checkRow][col] = '-';
                                    board[endRow][col] = 'P';
                                }
                            } else {
                                checkRow += 1;
                            }
                        }
                    }
                } else if (whiteMove.contains("O")) {
                    pawnCounter += 1;
                    // check if kingside or queen side
                    if (whiteMove.contains("O-O-O")) {
                        // queenside castling
                        board[7][4] = '-';
                        board[7][2] = 'K';
                        board[7][3] = 'R';
                        board[7][0] = '-';
                    } else {
                        // kingside
                        board[7][4] = '-';
                        board[7][6] = 'K';
                        board[7][5] = 'R';
                        board[7][7] = '-';
                    }
                } else {
                    // non-pawn: get piece type
                    if (whiteMove.contains("x")) {
                        pawnCounter = 0;
                    } else {
                        pawnCounter += 1;
                    }
                    char pieceType = whiteMove.charAt(0);
                    String endLoc = getEndLoc(whiteMove);
                    // determine end location of piece
                    int endCol = Character.getNumericValue(endLoc.charAt(0)) - 10;
                    int endRow = 8 - Character.getNumericValue(endLoc.charAt(1));
                    // ROOK
                    if (pieceType == 'R') {
                        // find where rooks are
                        Point[] rooks = gpl(board, pieceType, color);
                        // now that we know where current rooks are, iterate through to see which one has a valid move to the end point
                        Point validRook = canMove(board, whiteMove, rooks, pieceType, endRow, endCol);
                        if ((int) validRook.getY() == 0) {
                            // queen side
                            whiteQueenRook = true;
                        } else {
                            whiteKingRook = true;
                        }
                        //actually move the piece
                        board[(int) validRook.getX()][(int) validRook.getY()] = '-';
                        board[endRow][endCol] = 'R';
                    // end rook
                    // bishop start
                    } else if (pieceType == 'B') {
                        Point[] bishops = gpl(board, pieceType, color);
                        // now that we know where bishops are, iterate through to see which bishop has a valid move to the end point
                        Point validBish = canMove(board, whiteMove, bishops, pieceType, endRow, endCol);
                        // actually move the piece
                        board[(int) validBish.getX()][(int) validBish.getY()] = '-';
                        board[endRow][endCol] = 'B';
                    // end bishop
                    // knight start
                    } else if (pieceType == 'N') {
                        Point[] knights = gpl(board, pieceType, color);
                        Point validKnight = canMove(board, whiteMove, knights, pieceType, endRow, endCol);
                        board[(int) validKnight.getX()][(int) validKnight.getY()] = '-';
                        board[endRow][endCol] = 'N';
                    // end knight
                    // queen start
                    } else if (pieceType == 'Q') {
                        for (int i = 0; i < 8; i++) {
                            for (int j = 0; j < 8; j++) {
                                if (board[i][j] == 'Q') {
                                    board[i][j] = '-';
                                    board[endRow][endCol] = 'Q';
                                }
                            }
                        }
                    // end queen
                    // king start
                    } else if (pieceType == 'K') {
                        whiteKing = true;
                        for (int i = 0; i < 8; i++) {
                            for (int j = 0; j < 8; j++) {
                                if (board[i][j] == 'K') {
                                    board[i][j] = '-';
                                    board[endRow][endCol] = 'K';
                                }
                            }
                        }
                    // end king
                    }
                }
                // perform black move
                String blackMove = turnMoves[1];
                lastMove = blackMove;
                color = 'b';
                if (Character.isLowerCase(blackMove.charAt(0))) {
                    pawnCounter = 0;
                    // is pawn
                    if (blackMove.contains("x")) {
                        // pawn moved diagonal
                        int startCol = Character.getNumericValue(blackMove.charAt(0)) - 10;
                        int endCol = Character.getNumericValue(blackMove.charAt(2)) - 10;
                        int row = 8 - Character.getNumericValue(blackMove.charAt(3));
                        if (blackMove.contains("=")) {
                            board[row][endCol] = blackMove.charAt(blackMove.indexOf('=') + 1);
                            board[row - 1][startCol] = '-';
                        } else {
                            board[row][endCol] = 'p';
                            board[row - 1][startCol] = '-';
                            if (row == 5) {
                                if (board[row - 1][endCol] == 'P') {
                                    board[row - 1][endCol] = '-';
                                }
                            }
                        }
                    } else {
                        // pawn moved vertically
                        int col = Character.getNumericValue(blackMove.charAt(0)) - 10;
                        int endRow = 8 - Character.getNumericValue(blackMove.charAt(1));
                        boolean foundPawn = false;
                        int checkRow = endRow - 1;
                        while (!foundPawn) {
                            char pawn = board[checkRow][col];
                            if (pawn == 'p') {
                                foundPawn = true;
                                if (blackMove.contains("=")) {
                                    board[checkRow][col] = '-';
                                    board[endRow][col] = blackMove.charAt(blackMove.indexOf('=') + 1);
                                } else {
                                    board[checkRow][col] = '-';
                                    board[endRow][col] = 'p';
                                }
                            } else {
                                checkRow -= 1;
                            }
                        }
                    }
                } else if (blackMove.contains("O")) {
                    pawnCounter += 1;
                    if (blackMove.contains("O-O-O")) {
                        board[0][4] = '-';
                        board[0][2] = 'K';
                        board[0][3] = 'R';
                        board[0][0] = '-';
                    } else {
                        board[0][4] = '-';
                        board[0][6] = 'K';
                        board[0][5] = 'R';
                        board[0][7] = '-';
                    }
                } else {
                    // non-pawn: get piece type
                    if (blackMove.contains("x")) {
                        pawnCounter = 0;
                    } else {
                        pawnCounter += 1;
                    }
                    char pieceType = blackMove.charAt(0);
                    String endLoc = getEndLoc(blackMove);

                    int endCol = Character.getNumericValue(endLoc.charAt(0)) - 10;
                    int endRow = 8 - Character.getNumericValue(endLoc.charAt(1));
                    // start rook
                    if (pieceType == 'R') {
                        // find where rooks are
                        Point[] rooks = gpl(board, pieceType, color);
                        Point validRook = canMove(board, blackMove, rooks, pieceType, endRow, endCol);
                        if ((int) validRook.getY() == 0) {
                            blackQueenRook = true;
                        } else {
                            blackKingRook = true;
                        }
                        board[(int) validRook.getX()][(int) validRook.getY()] = '-';
                        board[endRow][endCol] = 'r';
                    // end rook
                    } else if (pieceType == 'B') {
                        // bishop start
                        Point[] bishops = gpl(board, pieceType, color);
                        Point validBish = canMove(board, blackMove, bishops, pieceType, endRow, endCol);
                        board[(int) validBish.getX()][(int) validBish.getY()] = '-';
                        board[endRow][endCol] = 'b';
                    // end bishop
                    } else if (pieceType == 'N') {
                    // knight start
                    // find where knights are
                        Point[] knights = gpl(board, pieceType, color);
                        Point validKnight = canMove(board, blackMove, knights, pieceType, endRow, endCol);
                        board[(int) validKnight.getX()][(int) validKnight.getY()] = '-';
                        board[endRow][endCol] = 'n';
                    } else if (pieceType == 'Q') {
                        for (int i = 0; i < 8; i++) {
                            for (int j = 0; j < 8; j++) {
                                if (board[i][j] == 'q') {
                                    board[i][j] = '-';
                                    board[endRow][endCol] = 'q';
                                }
                            }
                        }
                    } else if (pieceType == 'K') {
                        blackKing = true;
                        for (int i = 0; i < 8; i++) {
                            for (int j = 0; j < 8; j++) {
                                if (board[i][j] == 'k') {
                                    board[i][j] = '-';
                                    board[endRow][endCol] = 'k';
                                }
                            }
                        }
                    }
                }
            } else {
                // only white move specified
                String whiteMove = turnMoves[0];
                lastMove = whiteMove;
                color = 'w';
                if (Character.isLowerCase(whiteMove.charAt(0))) {
                    pawnCounter = 0;
                    // is pawn
                    if (whiteMove.contains("x")) {
                        // pawn moved diagonal
                        int startCol = Character.getNumericValue(whiteMove.charAt(0)) - 10;
                        int endCol = Character.getNumericValue(whiteMove.charAt(2)) - 10;
                        int row = 8 - Character.getNumericValue(whiteMove.charAt(3));
                        if (whiteMove.contains("=")) {
                            board[row][endCol] = whiteMove.charAt(whiteMove.indexOf('=') + 1);
                            board[row + 1][startCol] = '-';
                        } else {
                            board[row][endCol] = 'P';
                            board[row + 1][startCol] = '-';
                            if (row == 2) {
                                // check if one row below is a black pawn
                                if (board[row + 1][endCol] == 'p') {
                                    board[row + 1][endCol] = '-';
                                }
                            }
                        }
                    } else {
                        // pawn moved vertically
                        int col = Character.getNumericValue(whiteMove.charAt(0)) - 10;
                        int endRow = 8 - Character.getNumericValue(whiteMove.charAt(1));
                        boolean foundPawn = false;
                        int checkRow = endRow + 1;
                        while (!foundPawn) {
                            char pawn = board[checkRow][col];
                            if (pawn == 'P') {
                                foundPawn = true;
                                if (whiteMove.contains("=")) {
                                    board[checkRow][col] = '-';
                                    board[endRow][col] = whiteMove.charAt(whiteMove.indexOf('=') + 1);
                                } else {
                                    board[checkRow][col] = '-';
                                    board[endRow][col] = 'P';
                                }
                            } else {
                                checkRow += 1;
                            }
                        }
                    }
                } else if (whiteMove.contains("O")) {
                    pawnCounter += 1;
                    // check if kingside or queen side
                    if (whiteMove.contains("O-O-O")) {
                        // queenside castling
                        board[7][4] = '-';
                        board[7][2] = 'K';
                        board[7][3] = 'R';
                        board[7][0] = '-';
                    } else {
                        // kingside
                        board[7][4] = '-';
                        board[7][6] = 'K';
                        board[7][5] = 'R';
                        board[7][7] = '-';
                    }
                } else {
                    // non-pawn: get piece type
                    if (whiteMove.contains("x")) {
                        pawnCounter = 0;
                    } else {
                        pawnCounter += 1;
                    }
                    char pieceType = whiteMove.charAt(0);
                    String endLoc = getEndLoc(whiteMove);
                    // determine end location of piece
                    int endCol = Character.getNumericValue(endLoc.charAt(0)) - 10;
                    int endRow = 8 - Character.getNumericValue(endLoc.charAt(1));
                    // ROOK
                    if (pieceType == 'R') {
                        // find where rooks are
                        Point[] rooks = gpl(board, pieceType, color);
                        // now that we know where current rooks are, iterate through to see which one has a valid move to the end point
                        Point validRook = canMove(board, whiteMove, rooks, pieceType, endRow, endCol);
                        //actually move the piece
                        board[(int) validRook.getX()][(int) validRook.getY()] = '-';
                        board[endRow][endCol] = 'R';
                    // end rook
                    // bishop start
                    } else if (pieceType == 'B') {
                        Point[] bishops = gpl(board, pieceType, color);
                        // now that we know where bishops are, iterate through to see which bishop has a valid move to the end point
                        Point validBish = canMove(board, whiteMove, bishops, pieceType, endRow, endCol);
                        // actually move the piece
                        board[(int) validBish.getX()][(int) validBish.getY()] = '-';
                        board[endRow][endCol] = 'B';
                    // end bishop
                    // knight start
                    } else if (pieceType == 'N') {
                        Point[] knights = gpl(board, pieceType, color);
                        Point validKnight = canMove(board, whiteMove, knights, pieceType, endRow, endCol);
                        board[(int) validKnight.getX()][(int) validKnight.getY()] = '-';
                        board[endRow][endCol] = 'N';
                    // end knight
                    // queen start
                    } else if (pieceType == 'Q') {
                        for (int i = 0; i < 8; i++) {
                            for (int j = 0; j < 8; j++) {
                                if (board[i][j] == 'Q') {
                                    board[i][j] = '-';
                                    board[endRow][endCol] = 'Q';
                                }
                            }
                        }
                    // end queen
                    // king start
                    } else if (pieceType == 'K') {
                        for (int i = 0; i < 8; i++) {
                            for (int j = 0; j < 8; j++) {
                                if (board[i][j] == 'K') {
                                    board[i][j] = '-';
                                    board[endRow][endCol] = 'K';
                                }
                            }
                        }
                    // end king
                    }
                }
            }
        } // end while loop
        // System.out.println(pgn);
        int dashes = 0;
        String masterString = "";
        for (int i = 0; i < 8; i++) {
            String rowString = "";
            dashes = 0;
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == '-') {
                    dashes += 1;
                } else {
                    if (dashes != 0 && j != 0) {
                        rowString += Integer.toString(dashes);
                        dashes = 0;
                    }
                    rowString += board[i][j];
                }
            }
            if (dashes > 0) {
                rowString += Integer.toString(dashes);
            }
            masterString += rowString + "/";
        }
        masterString = masterString.substring(0, masterString.length() - 1);
        return masterString;
    }

    /**
     * Reads the file named by path and returns its content as a String.
     *
     * @param path the relative or abolute path of the file to read
     * @return a String containing the content of the file
     */
    public static String fileContent(String path) {
        Path file = Paths.get(path);
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                // Add the \n that's removed by readline()
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
            System.exit(1);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        String game = fileContent(args[0]);
        System.out.format("Event: %s%n", tagValue("Event", game));
        System.out.format("Site: %s%n", tagValue("Site", game));
        System.out.format("Date: %s%n", tagValue("Date", game));
        System.out.format("Round: %s%n", tagValue("Round", game));
        System.out.format("White: %s%n", tagValue("White", game));
        System.out.format("Black: %s%n", tagValue("Black", game));
        System.out.format("Result: %s%n", tagValue("Result", game));
        System.out.println("Final Position:");
        System.out.println(finalPosition(game));

    }
}
