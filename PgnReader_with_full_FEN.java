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


	public static Point[] getPieceLocations(char[][] board, char piece_type, char color) {
		Point[] locs = new Point[2];
		int num = 0;
		piece_type = (color == 'b') ? Character.toLowerCase(piece_type) : piece_type;
		// System.out.println("Color: " + color);
		// System.out.println("Piece Type: " + piece_type);
		for (int i=0; i<8; i++) {
			for (int j=0; j<8; j++) {
				// System.out.println("Row: " + Integer.toString(i));
				// System.out.println("Column: " + Integer.toString(j));
				// System.out.println("Piece on Board: " + board[i][j]);
				if (board[i][j] == piece_type) {
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
		Pattern p = Pattern.compile("[RBN]{1}([a-h1-8]?)x?[a-h]{1}[1-8]{1}\\S*");
		Matcher m = p.matcher(move);
		String dis_loc = "-";
		if (m.find()) {
			dis_loc = m.group(1);
		}
		return dis_loc;
	}

	public static String getEndLoc(String move) {
		Pattern p = Pattern.compile("[QKsRBN]{1}[a-h1-8]?x?([a-h]{1}[1-8]{1})\\S*");
		Matcher m = p.matcher(move);
		while (m.find()) {
			return m.group(1);
		}
		return "0";
	}

	// Return the piece that can make the move required
	public static Point canMove(char[][] board, String move, Point[] locs, char piece_type, int end_row, int end_col) {
		if (piece_type == 'R' || piece_type == 'r') {
			// check if move provides which rook moved
			String dis_loc = disProvider(move);
			if (dis_loc != "-" && !dis_loc.isEmpty()) {
				// find which one it came from
				if (Character.isDigit(dis_loc.charAt(0))) {
					// a starting row was provided -- find the rook currently on that row
					for (int i=0; i<locs.length; i++) {
						if ((int) locs[i].getX() == Integer.parseInt(dis_loc)) {
							return locs[i];
						}
					}
				} else {
					// a starting column was provided -- find the rook currently in that col
					for (int i=0; i<locs.length; i++) {
						if ((int) locs[i].getY() == Character.getNumericValue(dis_loc.charAt(0))-10) {
							return locs[i];
						}
					}
				}
			}
			// if it reaches this point, this means an initial row/col was not provided in the move
			// at this point, iterate through the available rooks -- the first rook that can validly make the move is the one that made it -- CAVEAT: only one rook may be able to move without jeopardizing king (ADD THIS LAST IF TIME AVAILABLE)
			for (int i=0; i<locs.length; i++) {
				// System.out.println("Location");
				// System.out.println("Row: " + Double.toString(locs[i].getX()));
				// System.out.println("Column: " + Double.toString(locs[i].getY()));
				if (end_row == (int) locs[i].getX()	|| end_col == (int) locs[i].getY()) {
					boolean clear_path = true;
					//check if there is anything between initial pos of rook and end pos
					// first check if it rook moved vertically or horizontally
					if (end_row == (int) locs[i].getX()) {
						// this means it stayed on that same row
						int col_delta = end_col - (int) locs[i].getY();
						// col_delta can be pos or neg. -- if pos, it moved right. if neg, it moved left.
						if (col_delta > 0) {
							// if rook moved right
							for (int col=1; col<col_delta+1; col++) {
								// if there is anything other than - in the path, it is not a clear path and cannot move
								if (((int) locs[i].getY()+col) != end_col) {
									if (board[end_row][(int) locs[i].getY()+col] != '-') {
										clear_path = false;
									}
								}
							}
						} else {
							// if rook moved left
							for (int col=1; col<col_delta+1; col++) {
								if (((int) locs[i].getY()-col) != end_col) {
									if (board[end_row][(int) locs[i].getY()-col] != '-') {
										clear_path = false;
									}
								}
							}
						}
						if (clear_path) {
							return locs[i];
						}
					} else {
						// this means it stayed on the same column
						int row_delta = end_row - (int) locs[i].getX();
						// row_delta can be pos or neg. -- if pos, it moved up (towards black). if neg, it moved down (towards white)
						if (row_delta > 0) {
							// rook moved up
							for (int row=1; row<row_delta+1; row++) {
								if (((int) locs[i].getX()+row) != end_row) {
									if (board[(int) locs[i].getX()+row][end_col] != '-') {
										clear_path = false;
									}
								}
							}
						} else {
							// rook moved down
							for (int row=1; row<row_delta+1; row++) {
								if (((int) locs[i].getX()-row) != end_row) {
									if (board[(int) locs[i].getX()-row][end_col] != '-') {
										clear_path = false;
									}
								}
							}
						}
						if (clear_path) {
							return locs[i];
						}
					}
				}
			}
		} else if (piece_type == 'B' || piece_type == 'b') {
			String dis_loc = disProvider(move);
			if (dis_loc != "-" && !dis_loc.isEmpty()) {
				// find which one it came from
				if (Character.isDigit(dis_loc.charAt(0))) {
					// a starting row was provided -- find the bishop currently on that row
					for (int i=0; i<locs.length; i++) {
						if ((int) locs[i].getX() == Integer.parseInt(dis_loc)) {
							return locs[i];
						}
					}
				} else {
					// a starting column was provided -- find the bishop currently in that col
					for (int i=0; i<locs.length; i++) {
						if ((int) locs[i].getY() == Character.getNumericValue(dis_loc.charAt(0))-10) {
							return locs[i];
						}
					}
				}
			}
			// if it reaches this point, this means an initial row/col was not provided in the move
			// at this point, iterate through the available bishops -- the first bish that can validly make the move is the one that made it -- CAVEAT: only one bish may be able to move without jeopardizing king (ADD THIS LAST IF TIME AVAILABLE)
			for (int i=0; i<locs.length; i++) {
				int row_delta = Math.abs(end_row - (int) locs[i].getX());
				int col_delta = Math.abs(end_col - (int) locs[i].getY());
				if (row_delta == col_delta) {
					// check for clear path
					int dir_row_delta = end_row - (int) locs[i].getX();
					int dir_col_delta = end_col - (int) locs[i].getY();
					int row_add = 0;
					int col_add = 0;
					boolean clear_path = true;
					for (int x=0; x<row_delta; x++) {
						row_add += dir_row_delta/row_delta;
						col_add += dir_col_delta/col_delta;
						if (((int) locs[i].getX()+row_add != end_row) && ((int) locs[i].getY()+col_add != end_col)) {
							if (board[(int) locs[i].getX() + row_add][(int) locs[i].getY() + col_add] != '-') {
								clear_path = false;
							}
						}
					}
					if (clear_path) {
						return locs[i];
					}
				}
			}
		} else if (piece_type == 'N' || piece_type == 'n') {
			String dis_loc = disProvider(move);
			if (dis_loc != "-" && !dis_loc.isEmpty()) {
				// find which one it came from
				if (Character.isDigit(dis_loc.charAt(0))) {
					// a starting row was provided -- find the knight currently on that row
					for (int i=0; i<locs.length; i++) {
						if ((int) locs[i].getX() == Integer.parseInt(dis_loc)) {
							return locs[i];
						}
					}
				} else {
					// a starting column was provided -- find the knight currently in that col
					for (int i=0; i<locs.length; i++) {
						if ((int) locs[i].getY() == Character.getNumericValue(dis_loc.charAt(0))-10) {
							return locs[i];
						}
					}
				}
			}
			// if it reaches this point, this means an initial row/col was not provided in the move
			// at this point, iterate through the available knights -- the first knight that can validly make the move is the one that made it -- CAVEAT: only one knight may be able to move without jeopardizing king (ADD THIS LAST IF TIME AVAILABLE)
			
			for (int i=0; i<locs.length; i++) {
				int row_delta = Math.abs(end_row - (int) locs[i].getX());
				int col_delta = Math.abs(end_col - (int) locs[i].getY());
				if ((row_delta == 2 && col_delta == 1) || (row_delta == 1 && col_delta == 2)) {
					return locs[i];
				}
			}
		}
		return new Point();
	}

	public static void debugBoard(char[][] board) {
		for (int i=0; i<8; i++) {
			String row = "";
			for (int j=0; j<8; j++) {
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
		String[] split_game = game.split("\n");
		boolean san_start = false;
		String san_text = "";
		for (int i=0; i<split_game.length; i++) {
			if (split_game[i].trim().isEmpty()) {
				san_start = true;
			}
			if (san_start) {
				san_text = san_text + " " + split_game[i];
			}
		}
		san_text = san_text.trim();
		// String move_pattern = "\\d+\\. ([a-zA-ZO\\-0-9\\\\=+#]+\\s?[a-zA-Z0-9+#]+)";
		String move_pattern = "\\d+\\. ([a-zA-ZO\\-0-9=+#?]+\\s?[a-zA-ZO\\-0-9=+#?]+)";
		Pattern p = Pattern.compile(move_pattern);
		Matcher m = p.matcher(san_text);
		char[][] board = new char[8][8];
		char[][] prev_move_board = new char[8][8];
		String[] initial = "rnbqkbnr/pppppppp/--------/--------/--------/--------/PPPPPPPP/RNBQKBNR".split("/");
		for (int i=0; i < 8; i++) {
			for (int j=0; j < 8; j++) {
				board[i][j] = initial[i].charAt(j);
			}
		}
		int counter = 0;
		char color = 'o';
		boolean white_king_rook = false;
		boolean white_queen_rook = false;
		boolean white_king = false;
		boolean black_king_rook = false;
		boolean black_queen_rook = false;
		boolean black_king = false;
		String last_move = "";
		int pawn_counter = 0;
		while (m.find()) {
			for (int i=0; i<8; i++) {
				for (int j=0; j<8; j++) {
					prev_move_board[i][j] = board[i][j];
				}
			}
			counter += 1;
			String[] turn_moves = m.group(1).split(" ");
			// both moves specified
			if (turn_moves.length == 2) {
				color = 'w';
				// perform white move
				String white_move = turn_moves[0];
				if (Character.isLowerCase(white_move.charAt(0))) {
					pawn_counter = 0;
					// is pawn
					if (white_move.contains("x")) {
						// pawn moved diagonal
						int start_col = Character.getNumericValue(white_move.charAt(0))-10;
						int end_col = Character.getNumericValue(white_move.charAt(2))-10;
						int row = 8 - Character.getNumericValue(white_move.charAt(3));
						if (white_move.contains("=")) {
							board[row][end_col] = white_move.charAt(white_move.indexOf('=')+1);
							board[row+1][start_col] = '-';
						} else {
							board[row][end_col] = 'P';
							board[row+1][start_col] = '-';
							// check if this is en passant pawn capture
							if (row == 2) {
								// check if one row below is a black pawn
								if (board[row+1][end_col] == 'p') {
									board[row+1][end_col] = '-';
								}
							}
						} 
					} else {
						// pawn moved vertically
						int col = Character.getNumericValue(white_move.charAt(0))-10;
						int end_row = 8 - Character.getNumericValue(white_move.charAt(1));
						boolean found_pawn = false;
						int check_row = end_row+1;
						while (!found_pawn) {
							char pawn = board[check_row][col];
							if (pawn == 'P') {
								found_pawn = true;
								if (white_move.contains("=")) {
									board[check_row][col] = '-';
									board[end_row][col] = white_move.charAt(white_move.indexOf('=')+1);
								} else {
									board[check_row][col] = '-';
									board[end_row][col] = 'P';
								}
							} else {
								check_row += 1;
							}
						}
					}
				} else if (white_move.contains("O")) {
					pawn_counter += 1;
					// check if kingside or queen side
					if (white_move.contains("O-O-O")) {
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
					if (white_move.contains("x")) {
						pawn_counter = 0;
					} else {
						pawn_counter += 1;
					}
					char piece_type = white_move.charAt(0);
					String end_loc = getEndLoc(white_move);
					// determine end location of piece
					int end_col = Character.getNumericValue(end_loc.charAt(0)) - 10;
					int end_row = 8 - Character.getNumericValue(end_loc.charAt(1));
					// ROOK
					if (piece_type == 'R') {
			  			// find where rooks are
						Point[] rooks = getPieceLocations(board, piece_type, color);
					    // now that we know where current rooks are, iterate through to see which one has a valid move to the end point
					    Point valid_rook = canMove(board, white_move, rooks, piece_type, end_row, end_col);
					    if ((int) valid_rook.getY() == 0) {
					    	// queen side
					    	white_queen_rook = true;
					    } else {
					    	white_king_rook = true;
					    }
					    //actually move the piece
					   	board[(int) valid_rook.getX()][(int) valid_rook.getY()] = '-';
					   	board[end_row][end_col] = 'R';
					// end rook
					// bishop start
					} else if (piece_type == 'B') {
						Point[] bishops = getPieceLocations(board, piece_type, color);
						// now that we know where bishops are, iterate through to see which bishop has a valid move to the end point
						Point valid_bish = canMove(board, white_move, bishops, piece_type, end_row, end_col);
						// actually move the piece
						board[(int) valid_bish.getX()][(int) valid_bish.getY()] = '-';
						board[end_row][end_col] = 'B';
					// end bishop
					// knight start
					} else if (piece_type == 'N') {
						Point[] knights = getPieceLocations(board, piece_type, color);
						Point valid_knight = canMove(board, white_move, knights, piece_type, end_row, end_col);
						board[(int) valid_knight.getX()][(int) valid_knight.getY()] = '-';
						board[end_row][end_col] = 'N';
					// end knight
					// queen start
					} else if (piece_type == 'Q') {
						for (int i=0; i<8; i++) {
							for (int j=0; j<8; j++) {
								if (board[i][j] == 'Q') {
									board[i][j] = '-';
									board[end_row][end_col] = 'Q';
								}
							}
						}
					// end queen
					// king start
					} else if (piece_type == 'K') {
						white_king = true;
						for (int i=0; i<8; i++) {
							for (int j=0; j<8; j++) {
								if (board[i][j] == 'K') {
									board[i][j] = '-';
									board[end_row][end_col] = 'K';
								}
							}
						}
					// end king
					}
				}	
				// perform black move
				String black_move = turn_moves[1];
				last_move = black_move;
				color = 'b';
				if (Character.isLowerCase(black_move.charAt(0))) {
					pawn_counter = 0;
					// is pawn
					if (black_move.contains("x")) {
						// pawn moved diagonal
						int start_col = Character.getNumericValue(black_move.charAt(0))-10;
						int end_col = Character.getNumericValue(black_move.charAt(2))-10;
						int row = 8 - Character.getNumericValue(black_move.charAt(3));
						if (black_move.contains("=")) {
							board[row][end_col] = black_move.charAt(black_move.indexOf('=')+1);
							board[row-1][start_col] = '-';
						} else {
							board[row][end_col] = 'p';
							board[row-1][start_col] = '-';
							if (row == 5) {
								if (board[row-1][end_col] == 'P') {
									board[row-1][end_col] = '-';
								}
							}
						}
					} else {
						// pawn moved vertically
						int col = Character.getNumericValue(black_move.charAt(0))-10;
						int end_row = 8 - Character.getNumericValue(black_move.charAt(1));
						boolean found_pawn = false;
						int check_row = end_row-1;
						while (!found_pawn) {
							char pawn = board[check_row][col];
							if (pawn == 'p') {
								found_pawn = true;
								if (black_move.contains("=")) {
									board[check_row][col] = '-';
									board[end_row][col] = black_move.charAt(black_move.indexOf('=')+1);
								} else {
									board[check_row][col] = '-';
									board[end_row][col] = 'p';
								}
							} else {
								check_row -= 1;
							}
						}
					}
				} else if (black_move.contains("O")) {
					pawn_counter += 1;
					if (black_move.contains("O-O-O")) {
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
					if (black_move.contains("x")) {
						pawn_counter = 0;
					} else {
						pawn_counter += 1;
					}
					char piece_type = black_move.charAt(0);
					String end_loc = getEndLoc(black_move);

					int end_col = Character.getNumericValue(end_loc.charAt(0)) - 10;
					int end_row = 8 - Character.getNumericValue(end_loc.charAt(1));
					// start rook
					if (piece_type == 'R') {
			  			// find where rooks are
						Point[] rooks = getPieceLocations(board, piece_type, color);
						Point valid_rook = canMove(board, black_move, rooks, piece_type, end_row, end_col);
						if ((int) valid_rook.getY() == 0) {
							black_queen_rook = true;
						} else {
							black_king_rook = true;
						}
						board[(int) valid_rook.getX()][(int) valid_rook.getY()] = '-';
						board[end_row][end_col] = 'r';
					// end rook
					} else if (piece_type == 'B') {
						// bishop start
						Point[] bishops = getPieceLocations(board, piece_type, color);
						Point valid_bish = canMove(board, black_move, bishops, piece_type, end_row, end_col);
						board[(int) valid_bish.getX()][(int) valid_bish.getY()] = '-';
						board[end_row][end_col] = 'b';
					// end bishop
					} else if (piece_type == 'N') {
					// knight start
					// find where knights are
						Point[] knights = getPieceLocations(board, piece_type, color);
						Point valid_knight = canMove(board, black_move, knights, piece_type, end_row, end_col);
						board[(int) valid_knight.getX()][(int) valid_knight.getY()] = '-';
						board[end_row][end_col] = 'n';
					} else if (piece_type == 'Q') {
						for (int i=0; i<8; i++) {
							for (int j=0; j<8; j++) {
								if (board[i][j] == 'q') {
									board[i][j] = '-';
									board[end_row][end_col] = 'q';
								}
							}
						}
					} else if (piece_type == 'K') {
						black_king = true;
						for (int i=0; i<8; i++) {
							for (int j=0; j<8; j++) {
								if (board[i][j] == 'k') {
									board[i][j] = '-';
									board[end_row][end_col] = 'k';
								}
							}
						}
					}
				}	
			} else {
				// only white move specified
				String white_move = turn_moves[0];
				last_move = white_move;
				color = 'w';
				if (Character.isLowerCase(white_move.charAt(0))) {
					pawn_counter = 0;
					// is pawn
					if (white_move.contains("x")) {
						// pawn moved diagonal
						int start_col = Character.getNumericValue(white_move.charAt(0))-10;
						int end_col = Character.getNumericValue(white_move.charAt(2))-10;
						int row = 8 - Character.getNumericValue(white_move.charAt(3));
						if (white_move.contains("=")) {
							board[row][end_col] = white_move.charAt(white_move.indexOf('=')+1);
							board[row+1][start_col] = '-';
						} else {
							board[row][end_col] = 'P';
							board[row+1][start_col] = '-';
							if (row == 2) {
								// check if one row below is a black pawn
								if (board[row+1][end_col] == 'p') {
									board[row+1][end_col] = '-';
								}
							}
						} 
					} else {
						// pawn moved vertically
						int col = Character.getNumericValue(white_move.charAt(0))-10;
						int end_row = 8 - Character.getNumericValue(white_move.charAt(1));
						boolean found_pawn = false;
						int check_row = end_row+1;
						while (!found_pawn) {
							char pawn = board[check_row][col];
							if (pawn == 'P') {
								found_pawn = true;
								if (white_move.contains("=")) {
									board[check_row][col] = '-';
									board[end_row][col] = white_move.charAt(white_move.indexOf('=')+1);
								} else {
									board[check_row][col] = '-';
									board[end_row][col] = 'P';
								}
							} else {
								check_row += 1;
							}
						}
					}
				} else if (white_move.contains("O")) {
					pawn_counter += 1;
					// check if kingside or queen side
					if (white_move.contains("O-O-O")) {
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
					if (white_move.contains("x")) {
						pawn_counter = 0;
					} else {
						pawn_counter += 1;
					}
					char piece_type = white_move.charAt(0);
					String end_loc = getEndLoc(white_move);
					// determine end location of piece
					int end_col = Character.getNumericValue(end_loc.charAt(0)) - 10;
					int end_row = 8 - Character.getNumericValue(end_loc.charAt(1));
					// ROOK
					if (piece_type == 'R') {
			  			// find where rooks are
						Point[] rooks = getPieceLocations(board, piece_type, color);
					    // now that we know where current rooks are, iterate through to see which one has a valid move to the end point
					    Point valid_rook = canMove(board, white_move, rooks, piece_type, end_row, end_col);
					    //actually move the piece
					   	board[(int) valid_rook.getX()][(int) valid_rook.getY()] = '-';
					   	board[end_row][end_col] = 'R';
					// end rook
					// bishop start
					} else if (piece_type == 'B') {
						Point[] bishops = getPieceLocations(board, piece_type, color);
						// now that we know where bishops are, iterate through to see which bishop has a valid move to the end point
						Point valid_bish = canMove(board, white_move, bishops, piece_type, end_row, end_col);
						// actually move the piece
						board[(int) valid_bish.getX()][(int) valid_bish.getY()] = '-';
						board[end_row][end_col] = 'B';
					// end bishop
					// knight start
					} else if (piece_type == 'N') {
						Point[] knights = getPieceLocations(board, piece_type, color);
						Point valid_knight = canMove(board, white_move, knights, piece_type, end_row, end_col);
						board[(int) valid_knight.getX()][(int) valid_knight.getY()] = '-';
						board[end_row][end_col] = 'N';
					// end knight
					// queen start
					} else if (piece_type == 'Q') {
						for (int i=0; i<8; i++) {
							for (int j=0; j<8; j++) {
								if (board[i][j] == 'Q') {
									board[i][j] = '-';
									board[end_row][end_col] = 'Q';
								}
							}
						}
					// end queen
					// king start
					} else if (piece_type == 'K') {
						for (int i=0; i<8; i++) {
							for (int j=0; j<8; j++) {
								if (board[i][j] == 'K') {
									board[i][j] = '-';
									board[end_row][end_col] = 'K';
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
		String master_string = "";
		for (int i=0; i<8; i++) {
			String row_string = "";
			dashes = 0;
			for (int j=0; j<8; j++) {
				if (board[i][j] == '-') {
					dashes += 1;
				} else {
					if (dashes != 0 && j != 0) {
						row_string += Integer.toString(dashes);
						dashes = 0;
					}
					row_string += board[i][j];
				}
			}
			if (dashes > 0) {
				row_string += Integer.toString(dashes);
			}
			master_string += row_string + "/";
		}
		master_string = master_string.substring(0, master_string.length() - 1);
		char active_move = (color == 'w') ? 'b' : 'w';
		master_string += " " + active_move + " ";
		int castling_avail = 0;
		if (!white_king) {
			// white king hasn't moved -- check if rooks have
			if (!white_king_rook) {
				castling_avail += 1;
				master_string += 'K';
				}
			if (!white_queen_rook) {
				// check if anything blocking queenside castle
				castling_avail += 1;
				master_string += 'Q';
			}
		}
		if (!black_king) {
			// white king hasn't moved -- check if rooks have
			if (!black_king_rook) {
				castling_avail += 1;
				master_string += 'k';
				}
			if (!black_queen_rook) {
				// check if anything blocking queenside castle
				castling_avail += 1;
				master_string += 'q';
			}
		}
		if (castling_avail == 0) {
			master_string += "- ";
		}

		// check if last thing moved was pawn
		if (Character.isLowerCase(last_move.charAt(0))) {
			// check color
			if (color == 'b') {
				// black was last move -- check if the pawn moved two spaces
				int row = 8 - Character.getNumericValue(last_move.charAt(1));
				int col = Character.getNumericValue(last_move.charAt(0)) - 10;
				if (board[row][col] == 'p' && board[row-2][col] == '-' && prev_move_board[row-2][col] == 'p') {
					master_string += last_move.charAt(0);
					master_string += Integer.toString(row-1);
				} else {
					master_string += "- ";
				}
			} else {
				int row = 8 - Character.getNumericValue(last_move.charAt(1));
				int col = Character.getNumericValue(last_move.charAt(0)) - 10;
				if (board[row][col] == 'P' && board[row+2][col] == '-' && prev_move_board[row+2][col] == 'P') {
					master_string += last_move.charAt(0);
					master_string += Integer.toString(row+1);
				} else {
					master_string += "- ";
				}
			}
		} else {
			master_string += "- ";
		}
		master_string += Integer.toString(pawn_counter) + " ";
		master_string += Integer.toString(counter);
		return master_string;
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
