// When a pawn delivers a check and there is no way to get out of it other than a pawn en passant-ing the checking pawn
// It thinks it is checkmate.



public class Board implements Cloneable{
	Piece[][] board = new Piece[8][8];
	byte[][] blockingCheckSquares;
	boolean[][] enPassants = new boolean[2][8];
	boolean[] inCheck = new boolean[]{false, false};
	boolean[][] rooksMoved = new boolean[][]{{false, false}, {false, false}};
	boolean[] kingsMoved = new boolean[]{false, false};
	boolean turn = false;
	boolean gameOver = false;
	byte fiftyMoveRule = 0;
	Linker positionList;
	Linker positionListAllMoves;
	// Constructor
	public Board() {
		this.init();
	}
	// Sets up all the variables
	public void init() {
//		this.turn = true;
//		board = new Piece[][]{{new Piece(Type.EMPTY, true), new Piece(Type.ROOK, true), new Piece(Type.BISHOP, true), new Piece(Type.QUEEN, true), new Piece(Type.KING, true), new Piece(Type.BISHOP, true), new Piece(Type.KNIGHT, true), new Piece(Type.ROOK, true), }, {new Piece(Type.PAWN, true), new Piece(Type.PAWN, true), new Piece(Type.PAWN, true), new Piece(Type.EMPTY, true), new Piece(Type.EMPTY, true), new Piece(Type.PAWN, true), new Piece(Type.EMPTY, true), new Piece(Type.EMPTY, true), }, {new Piece(Type.EMPTY, true), new Piece(Type.EMPTY, true), new Piece(Type.KNIGHT, true), new Piece(Type.EMPTY, true), new Piece(Type.PAWN, true), new Piece(Type.EMPTY, true), new Piece(Type.PAWN, true), new Piece(Type.PAWN, true), }, {new Piece(Type.EMPTY, true), new Piece(Type.EMPTY, true), new Piece(Type.EMPTY, true), new Piece(Type.EMPTY, true), new Piece(Type.EMPTY, true), new Piece(Type.EMPTY, true), new Piece(Type.EMPTY, true), new Piece(Type.EMPTY, true), }, {new Piece(Type.EMPTY, true), new Piece(Type.EMPTY, true), new Piece(Type.EMPTY, true), new Piece(Type.EMPTY, true), new Piece(Type.EMPTY, true), new Piece(Type.EMPTY, false), new Piece(Type.PAWN, false), new Piece(Type.PAWN, false), }, {new Piece(Type.EMPTY, true), new Piece(Type.EMPTY, true), new Piece(Type.KNIGHT, false), new Piece(Type.PAWN, false), new Piece(Type.EMPTY, true), new Piece(Type.PAWN, false), new Piece(Type.EMPTY, false), new Piece(Type.EMPTY, false), }, {new Piece(Type.PAWN, false), new Piece(Type.PAWN, false), new Piece(Type.PAWN, false), new Piece(Type.EMPTY, false), new Piece(Type.PAWN, false), new Piece(Type.KING, false), new Piece(Type.EMPTY, false), new Piece(Type.EMPTY, false), }, {new Piece(Type.ROOK, false), new Piece(Type.EMPTY, false), new Piece(Type.BISHOP, false), new Piece(Type.QUEEN, false), new Piece(Type.EMPTY, false), new Piece(Type.BISHOP, false), new Piece(Type.ROOK, false), new Piece(Type.EMPTY, false)}};

			
//		rooksMoved[0][0] = true;
//		rooksMoved[0][1] = true;
//		rooksMoved[1][0] = true;
//		rooksMoved[1][1] = true;
//			
			
//		 Sets the rest of the board to empty, this is easier to handle compared to null
		for(byte y = 0; y < 7; y++) {
			for(byte x = 0; x < 8; x++) {
				board[y][x] = new Piece(Type.EMPTY, true);
			}
		}
		board[0] = new Piece[]{new Piece(Type.ROOK, true), new Piece(Type.KNIGHT, true), new Piece(Type.BISHOP, true), new Piece(Type.QUEEN, true), new Piece(Type.KING, true), new Piece(Type.BISHOP, true), new Piece(Type.KNIGHT, true), new Piece(Type.ROOK, true)};
		board[7] = new Piece[]{new Piece(Type.ROOK, false), new Piece(Type.KNIGHT, false), new Piece(Type.BISHOP, false), new Piece(Type.QUEEN, false), new Piece(Type.KING, false), new Piece(Type.BISHOP, false), new Piece(Type.KNIGHT, false), new Piece(Type.ROOK, false)};
		
		for(byte x = 0; x < 8; x++) {
			// Sets the enPassants to false, these become true for one turn if a pawn double jumps
			enPassants[0][x] = false;
			enPassants[1][x] = false;
			// Set up the rows of pawns.
			board[1][x] = new Piece(Type.PAWN, true);
			board[6][x] = new Piece(Type.PAWN, false);
		}
		positionList = new Linker(this.copyBoard());
		positionListAllMoves = new Linker(this.copyBoard());
	}
	
	public void print() {
		Piece[][][] past = positionListAllMoves.toArray();
		for(int position = (past.length > 15 ? past.length-15 : 0); position < past.length; position++) {
			System.out.println();System.out.println();
			for(byte y = 0; y < past[position].length; y++) {
				System.out.println();
				for(byte x = 0; x < past[position][y].length; x++) {
					System.out.print(String.format("%0$7s", past[position][y][x].type));
				}
			}
		System.out.println("\nthis.turn = " + this.turn + ";");
		System.out.print("board = new Piece[][]{");
			for(byte y = 0; y < past[position].length; y++) {
				System.out.print("{");
				for(byte x = 0; x < past[position][y].length; x++) {
					System.out.print("new Piece(Type." + past[position][y][x].type + ", " + past[position][y][x].color + (y == 7 && x == 7 ? ")}" : "), "));
				}
				System.out.print(y == 7 ? "};" : "}, ");
			}
		}
	}
	
	public Board makeCopy() {
		Board output = new Board();
		output.board = this.copyBoard();
		output.enPassants = this.enPassants.clone();
		output.inCheck = this.inCheck.clone();
		output.rooksMoved = this.rooksMoved.clone();
		output.kingsMoved = this.kingsMoved.clone();
		output.gameOver = true && this.gameOver;
		output.fiftyMoveRule = (byte) (this.fiftyMoveRule+0);
		output.positionList = this.positionList.copy(this.positionList);
		return output;
	}
	
	public boolean compareGamePositions(Piece[][] pos1, Piece[][] pos2) {
		for(byte y = 0; y < 8; y++) {
			for(byte x = 0; x < 8; x++) {
				if(pos1[y][x].type != pos2[y][x].type) {
					return false;
				}
				if(pos1[y][x].type != Type.EMPTY) {
					if(pos1[y][x].color != pos2[y][x].color) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public Piece[][] copyBoard() {
		Piece[][] output = new Piece[8][8];
		for(byte y = 0; y < 8; y++) {
			for(byte x = 0; x < 8; x++) {
				output[y][x] = board[y][x].makeCopy();
			}
		}
		return output;
	}
	
	public boolean drawByInsuffientMaterial() {
		byte whitePieceCount = 0;
		byte blackPieceCount = 0;
		byte whiteBishopCount = 0;
		byte blackBishopCount = 0;
		boolean whiteBishopSquareColor = true;
		boolean blackBishopSquareColor = true;
		for(byte y = 0; y < 8; y++) {
			for(byte x = 0; x < 8; x++) {
				if(board[y][x].type == Type.KNIGHT || board[y][x].type == Type.BISHOP || board[y][x].type == Type.KING) {
					whitePieceCount += board[y][x].color ? 0 : 1;
					blackPieceCount += board[y][x].color ? 1 : 0;
					if(board[y][x].type == Type.BISHOP) {
						if(board[y][x].color) {
							blackBishopCount++;
							blackBishopSquareColor = (y+x)%2 == 0 ? true : false;
						} else {
							whiteBishopCount++;
							whiteBishopSquareColor = (y+x)%2 == 0 ? true : false;
						}
					}
				} else {
					if(board[y][x].type == Type.EMPTY) {
						continue;
					} else {
						return false;
					}
				}
			}
		}
		if(whitePieceCount + blackPieceCount < 4) {
			return true;
		} else {
			if(whitePieceCount == 2 || blackPieceCount == 2) {
				return false;
			}
			if(whiteBishopCount == 1 && blackBishopCount == 1 && whiteBishopSquareColor != blackBishopSquareColor) {
				return false;
			}
			return true;
		}
	}
	
	public void printError(boolean color, byte[] movesTried) {
		byte[] kingPosition = getPiecePosition(Type.KING, color);
		System.out.println("King position is null? " + (kingPosition == null));
		System.out.println("King position 0 is null? " + (kingPosition == null));
		System.out.println("King position 1 is null? " + (kingPosition == null));
		Piece[][][] past = positionListAllMoves.toArray();
		for(int position = (past.length > 15 ? past.length-15 : 0); position < past.length; position++) {
			System.out.println();System.out.println();
			for(byte y = 0; y < past[position].length; y++) {
				System.out.println();
				for(byte x = 0; x < past[position][y].length; x++) {
					System.out.print(String.format("%0$7s", past[position][y][x].type));
				}
			}
		System.out.println("\nthis.turn = " + this.turn + ";");
		System.out.print("board = new Piece[][]{");
			for(byte y = 0; y < past[position].length; y++) {
				System.out.print("{");
				for(byte x = 0; x < past[position][y].length; x++) {
					System.out.print("new Piece(Type." + past[position][y][x].type + ", " + past[position][y][x].color + (y == 7 && x == 7 ? ")}" : "), "));
				}
				System.out.print(y == 7 ? "};" : "}, ");
			}
		}
		Board test = new Board();
		test.board = past[past.length-1];
		if(test.getPossibleMovesFromCoords(movesTried[0], movesTried[1]) != null) {
		byte[][] possibleMoves = test.getPossibleMovesFromCoords(movesTried[0], movesTried[1]);
			for(int i = 0; i < possibleMoves.length; i++) {
				System.out.println("Possible Move: " + movesTried[0] + " " + movesTried[1] + " " + possibleMoves[i][0] + " " + possibleMoves[i][1]);
			}
		}
		System.out.print("};\n\n\n");
		System.out.println(movesTried[0] + " " + movesTried[1] + " " + movesTried[2] + " " + movesTried[3]);
		System.out.println("King position 0: " + (kingPosition[0]));
		System.out.println("King position 1: " + (kingPosition[1]));
	}	
	// Does a move for a player
	public byte doMove(byte indexY1, byte indexX1, byte indexY2, byte indexX2, byte promotion) {
		if(indexY2 == 0 && (indexX2 == 7 || indexX2 == 0)) {
			rooksMoved[indexY2 == 0 ? 1 : 0][indexX2 == 0 ? 0 : 1] = true;
		}
		// Things for 3 move repetition and 50 move rule;
		boolean captureOrPawn = board[indexY1][indexX1].type == Type.PAWN || board[indexY2][indexX2].type != Type.EMPTY;
		if(captureOrPawn) {
			positionList = new Linker(copyBoard());
			fiftyMoveRule = 0;
		}
		turn = !turn;
		for(byte i = 0; i < 2; i++) {
			for(byte j = 0; j < 8; j++) {
				enPassants[i][j] = false;
			}
		}
		boolean color = !board[indexY1][indexX1].color;
		if(board[indexY1][indexX1].type == Type.KING) {
			kingsMoved[board[indexY1][indexX1].color ? 1 : 0] = true;
		}
		if(board[indexY1][indexX1].type == Type.ROOK) {
			rooksMoved[board[indexY1][indexX1].color ? 1 : 0][indexX1 == 0 ? 0 : 1] = true;
		}
		if(board[indexY1][indexX1].type != Type.PAWN) {
			board[indexY2][indexX2] = board[indexY1][indexX1].makeCopy();
			board[indexY1][indexX1].type = Type.EMPTY;
		} else {
			// If the pawn is capturing something
			if(indexY2 == 0 || indexY2 == 7) {
				Type[] promotionPieces = new Type[]{Type.QUEEN, Type.ROOK, Type.BISHOP, Type.KNIGHT};
				board[indexY2][indexX2].type = promotionPieces[promotion];
				board[indexY2][indexX2].color = board[indexY1][indexX1].color ? true : false; // Dereferencing
				board[indexY1][indexX1].type = Type.EMPTY;
			} else if(indexX1 != indexX2) {
				if(board[indexY2][indexX2].type == Type.EMPTY) {
					// Capture the pawn
					board[indexY1][indexX2].type = Type.EMPTY;
					// Move the pawn
					board[indexY2][indexX2] = board[indexY1][indexX1].makeCopy();
					// Delete the old pawn
					board[indexY1][indexX1].type = Type.EMPTY;
				} else {
					board[indexY2][indexX2] = this.board[indexY1][indexX1].makeCopy();
					board[indexY1][indexX1].type = Type.EMPTY;
				}
			} else {
				board[indexY2][indexX2] = this.board[indexY1][indexX1].makeCopy();
				board[indexY1][indexX1].type = Type.EMPTY;
				if(abs((byte) (indexY1 - indexY2)) == 2) {
					enPassants[board[indexY2][indexX2].color ? 1 : 0][indexX2] = true;
				}
			}
		}
		// Handling castling
		if(board[indexY2][indexX2].type == Type.KING && abs((byte) (indexX2 - indexX1)) == 2) {
			board[indexY2][indexX2 - indexX1 > 0 ? 5 : 3] = board[indexY2][indexX2 - indexX1 > 0 ? 7 : 0].makeCopy();
			board[indexY2][indexX2 - indexX1 > 0 ? 7 : 0].type = Type.EMPTY;
		}
		
		positionListAllMoves.append(this.copyBoard());
		
		byte[] kingPosition = getPiecePosition(Type.KING, color);
		try {
			inCheck[color ? 1 : 0] = !isSquareSafe(kingPosition[0], kingPosition[1], color);
		} catch(Exception e) {
			this.print();
			this.printError(color, new byte[]{indexY1, indexX1, indexY2, indexX2});
			
			System.out.println(e.toString());
			
		}
			
		
		if(inCheck[color ? 1 : 0]) {
			blockingCheckSquares = getCheckBlockingSquares(color);
			if(isStalemate(color)) {
				gameOver = true;
				return (byte) (6 + (color ? 1 : 0));
			}
		} else {
			blockingCheckSquares = null;
			if(isStalemate(color)) {
				gameOver = true;
				return 5;
			}
		}
		if(!captureOrPawn) {
			positionList.append(copyBoard());
			if(++fiftyMoveRule == 50) {
				gameOver = true;
				return 4;
			}
			Piece[][][] positions = positionList.toArray();
			byte threeMoveRepetition = 0;
			for(int i = 0; i < positions.length; i++) {
				threeMoveRepetition+= compareGamePositions(positions[i], board) ? 1 : 0;
			}
			if(threeMoveRepetition >= 3) {
				gameOver = true;
				return 3;
			}
		}
		if(drawByInsuffientMaterial()) {
			return 2;
		}
		return 1;
	}
	
	public Board tryMove(byte indexY1, byte indexX1, byte indexY2, byte indexX2, byte promotion) {
		// Things for 3 move repetition and 50 move rule;
		Board newGame = this.makeCopy();
		boolean captureOrPawn = newGame.board[indexY1][indexX1].type == Type.PAWN || newGame.board[indexY2][indexX2].type != Type.EMPTY;
		if(captureOrPawn) {
			newGame.positionList = new Linker(copyBoard());
			newGame.fiftyMoveRule = 0;
		}
		newGame.turn = !newGame.turn;
		for(byte i = 0; i < 2; i++) {
			for(byte j = 0; j < 8; j++) {
				newGame.enPassants[i][j] = false;
			}
		}
		if(newGame.board[indexY1][indexX1].type == Type.KING) {
			newGame.kingsMoved[newGame.board[indexY1][indexX1].color ? 1 : 0] = true;
		}
		if(newGame.board[indexY1][indexX1].type == Type.ROOK) {
			newGame.rooksMoved[newGame.board[indexY1][indexX1].color ? 1 : 0][indexX1 == 0 ? 0 : 1] = true;
		}
		if(newGame.board[indexY1][indexX1].type != Type.PAWN) {
			newGame.board[indexY2][indexX2] = newGame.board[indexY1][indexX1].makeCopy();
			newGame.board[indexY1][indexX1].type = Type.EMPTY;
		} else {
			// If the pawn is capturing something
			if(indexY2 == 0 || indexY2 == 7) {
				Type[] promotionPieces = new Type[]{Type.QUEEN, Type.ROOK, Type.BISHOP, Type.KNIGHT};
				newGame.board[indexY2][indexX2].type = promotionPieces[promotion];
				newGame.board[indexY2][indexX2].color = newGame.board[indexY1][indexX1].color ? true : false; // Dereferencing
				newGame.board[indexY1][indexX1].type = Type.EMPTY;
			} else if(indexX1 != indexX2) {
				if(newGame.board[indexY2][indexX2].type == Type.EMPTY) {
					// Capture the pawn
					newGame.board[indexY1][indexX2].type = Type.EMPTY;
					// Move the pawn
					newGame.board[indexY2][indexX2] = newGame.board[indexY1][indexX1].makeCopy();
					// Delete the old pawn
					newGame.board[indexY1][indexX1].type = Type.EMPTY;
				} else {
					newGame.board[indexY2][indexX2] = newGame.board[indexY1][indexX1].makeCopy();
					newGame.board[indexY1][indexX1].type = Type.EMPTY;
				}
			} else {
				newGame.board[indexY2][indexX2] = newGame.board[indexY1][indexX1].makeCopy();
				newGame.board[indexY1][indexX1].type = Type.EMPTY;
				if(abs((byte) (indexY1 - indexY2)) == 2) {
					newGame.enPassants[newGame.board[indexY2][indexX2].color ? 1 : 0][indexX2] = true;
				}
			}
		}
		// Handling castling
		if(newGame.board[indexY2][indexX2].type == Type.KING && abs((byte) (indexX2 - indexX1)) == 2) {
			newGame.board[indexY2][indexX2 - indexX1 > 0 ? 5 : 3] = newGame.board[indexY2][indexX2 - indexX1 > 0 ? 0 : 7].makeCopy();
			newGame.board[indexY2][indexX2 - indexX1 > 0 ? 7 : 0].type = Type.EMPTY;
		}
		
		return newGame;
	}
	
	public int getScoreFromPiece(Type piece) {
		switch(piece) {
			case PAWN:
				return 1;
			case KNIGHT:
				return 3;
			case BISHOP:
				return 3;
			case ROOK:
				return 5;
			case QUEEN:
				return 9;
			default:
				return 0;
		}
	}
	
	public int getPlayerScore(boolean player) {
		int output = 0;
		for(byte y = 0; y < 8; y++) {
			for(byte x = 0; x < 8; x++) {
				if(board[y][x].color == player) {
					output += getScoreFromPiece(board[y][x].type);
				}
			}
		}
		return output;
	}
	// Checks if the position is stalemate
	public boolean isStalemate(boolean color) {
		for(byte y = 0; y < 8; y++) {
			for(byte x = 0; x < 8; x++) {
				if(board[y][x].color == color) {
					if(getPossibleMovesFromCoords(y, x) != null && getPossibleMovesFromCoords(y, x).length != 0) {
						return false;
					}
				}
			}
		}
		return true;
	}
	// Gets where a Type can move from its position
	public byte[][] getPossibleMovesFromCoords(byte indexY, byte indexX) {
		if(board[indexY][indexX].color != turn) {
			return null;
		} else {
			//try {
				switch(board[indexY][indexX].type) {
					case PAWN:
						return getPawnMoves(indexY, indexX, false);
					case KNIGHT:
						return getKnightMoves(indexY, indexX, false);
					case BISHOP:
						return getBishopMoves(indexY, indexX, false);
					case ROOK:
						return getRookMoves(indexY, indexX, false);
					case QUEEN:
						return getQueenMoves(indexY, indexX, false);
					case KING:
						return getKingMoves(indexY, indexX, false);
					default:
						return null;
				}
		}
	}
	// Takes absolute value of number
	public byte abs(byte num) {
		return (byte) (num < 0 ? -num : num);
	}
	// Checks if a Type is pinned to the king
	public byte[] getPinnedDirection(byte indexY, byte indexX) {
		// Get the position of the king of the same color of the Type of interest
		byte[] kingPosition = getPiecePosition(Type.KING, board[indexY][indexX].color);
		// Get the position of the king relative to the Type of interest to see if it can be pinned.
		byte[] kingPositionRelative = new byte[]{(byte) (kingPosition[0] - indexY), (byte) (kingPosition[1] - indexX)};
		// If the king could be pinned, this variable decides what direction to check for(rook, bishop)
		kingPositionRelative[0] = (byte) (kingPositionRelative[0] ==  0 ? 0 : (kingPositionRelative[0] < 0 ? 1 : -1));
		kingPositionRelative[1] = (byte) (kingPositionRelative[1] ==  0 ? 0 : (kingPositionRelative[1] < 0 ? 1 : -1));
		byte[] pinnedDirection = kingPositionRelative;
		// If the king can not be pinned
		if(!(abs(pinnedDirection[0]) == abs(pinnedDirection[1]) || pinnedDirection[0] == 0 || pinnedDirection[1] == 0)) {
			return null;
		}
		byte currentY;
		byte currentX;
		// Move it one so it doesn't start on the Type of interest
		currentY = (byte) (kingPosition[0] + pinnedDirection[0]);
		currentX = (byte) (kingPosition[1] + pinnedDirection[1]);
		boolean passedPiece = false;
		// While the current x and y are on the board
		while(currentY >= 0 && currentY < 8 && currentX >= 0 && currentX < 8) {
			if(currentY == indexY && currentX == indexX) {
				currentY += pinnedDirection[0];
				currentX += pinnedDirection[1];
				passedPiece = true;
				continue;
			
			// If there is a Type blocking or pinning the pin
			} else if(board[currentY][currentX].type != Type.EMPTY) {
				// If the Type can be a pinner and is the opposite color, then return that it is pinned and the direction
				if((board[currentY][currentX].type == Type.ROOK || 
						board[currentY][currentX].type == Type.QUEEN) && 
						board[currentY][currentX].color != board[indexY][indexX].color &&
						passedPiece &&
						(pinnedDirection[0] == 0 || pinnedDirection[1] == 0)) {
						return pinnedDirection;
				} else if((board[currentY][currentX].type == Type.QUEEN ||
					board[currentY][currentX].type == Type.BISHOP) && 
					board[currentY][currentX].color != board[indexY][indexX].color &&
					passedPiece &&
					(pinnedDirection[0] != 0 && pinnedDirection[1] != 0)) {
					return pinnedDirection;
				}  else {
					break;
				}
			} else {
				currentY += pinnedDirection[0];
				currentX += pinnedDirection[1];
			}
		}
		return null;
	}
	// Gets the position of a Type given the Type type and the color
	// This only finds the first Type from top to bottom, left to right, but it is only used for kings which don't have duplicates.
	public byte[] getPiecePosition(Type type, boolean color) {
		for(byte y = 0; y < 8; y++) {
			for(byte x = 0; x < 8; x++) {
				if(board[y][x].type == type && board[y][x].color == color) {
					return new byte[]{y, x};
				}
			}
		}
		return null;
	}
	// Returns the first 'count' items of and array
	public byte[][] resizeArray(byte[][] array, byte count) {
		if(array == null) {
			return null;
		}
		byte[][] output = new byte[count][2];
		for(byte i  = 0; i < count; i++) {
			output[i] = array[i];
		}
		return output;
	}
	// Checks to see if an array of moves contains a square
	public boolean arrayContains(byte[][] array, byte[] value) {
		for(byte i = 0; i < array.length; i++) {
			if(array[i][0] == value[0] && array[i][1] == value[1]) {
				return true;
			}
		}
		return false;
	}
	// Creates and array of squares that can be used to block a check
	public byte[][] getCheckBlockingSquares(boolean color) {
		byte[] kingPosition = getPiecePosition(Type.KING, color);
		// Used to see if the checker is a knight, if so, you can only capture the knight
		byte[] isKnight = null;
		// Initializing it so if it doesn't change, we know there is no check.
		byte[] checkingType = null;
		// See if there is more than one checker, if so, the king has to move, you can't block it
		boolean added = false;
		for(byte y = 0; y < 8; y++) {
			for(byte x = 0; x < 8; x++) {
				if(board[y][x].type != Type.EMPTY && 
				   board[y][x].color != color && 
				   arrayContains(getCheckingSquares(y, x), kingPosition)) {
					if(!added) {
						if(board[y][x].type == Type.KNIGHT) {
							isKnight = new byte[]{y, x};
						}
						added = !added;
						checkingType = new byte[]{y, x};
					} else {
						return new byte[][]{{-1, -1}};
					}
				}
			}
		}
		// Since you can't block a knight, if a knight is checking you, you must take it
		if(isKnight != null) {
			return new byte[][]{isKnight};
		}
		// If there is no check
		if(checkingType == null) {
			return null;
		}
		byte[] attackDirection = new byte[]{(byte) (checkingType[0] - kingPosition[0]), (byte) (checkingType[1] - kingPosition[1])};
		// Round down to -1, 0 or 1 too get the attack direction
		attackDirection[0] = (byte) (attackDirection[0] == 0 ? 0 : (attackDirection[0] < 0 ? -1 : 1));
		attackDirection[1] = (byte) (attackDirection[1] == 0 ? 0 : (attackDirection[1] < 0 ? -1 : 1));
		byte currentY = (byte) (kingPosition[0] + attackDirection[0]);
		byte currentX = (byte) (kingPosition[1] + attackDirection[1]);
		byte[][] blockingSquares = new byte[8][2];
		byte count = 0;
		while(currentY >= 0 && currentY < 8 && currentX >= 0 && currentX < 8) {
			if(board[currentY][currentX].type != Type.EMPTY) {
				blockingSquares[count++] = new byte[]{currentY, currentX};
				break;
			}
			blockingSquares[count++] = new byte[]{currentY, currentX};
			currentY += attackDirection[0];
			currentX += attackDirection[1];
		}
		return resizeArray(blockingSquares, count);
	}
	// If a player is in check, this is called and 'ands' the possible moves for a Type with the blocking moves
	public byte[][] getBlockingMovesFromPossibleMoves(byte[][] possibleMoves, boolean color, boolean pawn) {
		// If the player is not in check, then any move is okay
		if(blockingCheckSquares == null) {
			return possibleMoves;
		}
		// If the person is in check by more than one piece
		if(blockingCheckSquares[0][0] == -1) {
			return null;
		}
		byte[][] result = new byte[possibleMoves.length][2];
		byte count = 0;
		// If the move square is equal to a check block square, then the player can move there.
		for(byte i = 0; i < blockingCheckSquares.length; i++) {
			for(byte j = 0; j < possibleMoves.length; j++) {
				if((possibleMoves[j][0] == blockingCheckSquares[i][0] && 
				   possibleMoves[j][1] == blockingCheckSquares[i][1]) || 
				  (pawn &&
				   board[possibleMoves[j][0]][possibleMoves[j][1]].type == Type.EMPTY &&
				   possibleMoves[j][0] + (this.turn ? -1 : 1) == blockingCheckSquares[i][0] &&
				   possibleMoves[j][1] == blockingCheckSquares[i][1] &&
				   this.enPassants[this.turn ? 0 : 1][possibleMoves[j][1]])) {
					result[count++] = possibleMoves[j];
				}
			}
		}
		return resizeArray(result, count);
	}
	// Get the squares a Type is checking
	public byte[][] getCheckingSquares(byte indexY, byte indexX) {
		switch(board[indexY][indexX].type) {
			case PAWN:
				return getPawnMoves(indexY, indexX, true);
			case KNIGHT:
				return getKnightMoves(indexY, indexX, true);
			case BISHOP:
				return getBishopMoves(indexY, indexX, true);
			case ROOK:
				return getRookMoves(indexY, indexX, true);
			case QUEEN:
				return getQueenMoves(indexY, indexX, true);
			case KING:
				return getKingMoves(indexY, indexX, true);
			default:
				return null;
		}
	}
	// Get if Type is checking a square, just needs color input
	public boolean isSquareSafe(byte indexY, byte indexX, boolean color) {
		for(byte y = 0; y < 8; y++) {
			for(byte x = 0; x < 8; x++) {
				if(board[y][x].color != color && board[y][x].type != Type.EMPTY) {
					byte[][] moves = getCheckingSquares(y, x);
					for(byte i = 0; i < moves.length; i++) {
						if(moves[i][0] == indexY && moves[i][1] == indexX) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	// This gets the moves a pawn can make given its position
	public byte[][] getPawnMoves(byte indexY, byte indexX, boolean getDefence) {
		// The moves that the pawn can make
		byte[][] possibleMoves = new byte[4][2];
		// Count of the moves added to the array so far, used to resize the array at the end
		byte count = 0;
		// The direction the pawn can move
		byte pawnMoveDirection = (byte) (board[indexY][indexX].color ? 1 : -1);
		// The row the pawn needs to be on to double jump
		byte pawnDoubleJumpRow = (byte) (board[indexY][indexX].color ? 1 : 6);
		// The row the pawn needs to be on to en passant
		byte enPassantRow = (byte) (board[indexY][indexX].color ? 4 : 3);
		byte[] pinnedDirection = getPinnedDirection(indexY, indexX);
		// Used to see if a king can move somewhere
		if(getDefence) {
			for(byte i = -1; i < 2; i+=2) {
				// If the capture is on the board, is capturing a Type, or defending a friendly
				if(indexX + i >= 0 && indexX + i < 8) {
					possibleMoves[count++] = new byte[]{(byte) (indexY+pawnMoveDirection), (byte) (indexX+i)};
				}
			}
			return resizeArray(possibleMoves, count);
		// If the pawn is not pinned
		} else if(pinnedDirection == null) {
			if(board[indexY+pawnMoveDirection][indexX].type == Type.EMPTY) {
				possibleMoves[count++] = new byte[]{(byte) (indexY+pawnMoveDirection), indexX};
			}
			if(indexY == pawnDoubleJumpRow && board[indexY+2*pawnMoveDirection][indexX].type == Type.EMPTY && board[indexY+pawnMoveDirection][indexX].type == Type.EMPTY) {
				possibleMoves[count++] = new byte[]{(byte) (indexY+2*pawnMoveDirection), indexX};
			}
			// For each left and right capture
			for(byte i = -1; i < 2; i+=2) {
				// If the capture is on the board, is capturing a Type, or defending a friendly
				if(indexX + i >= 0 && 
				   indexX + i < 8 && 
				   board[indexY+pawnMoveDirection][indexX+i].type != Type.EMPTY &&
				   (board[indexY+pawnMoveDirection][indexX+i].color != board[indexY][indexX].color)) {
					possibleMoves[count++] = new byte[]{(byte) (indexY+pawnMoveDirection), (byte) (indexX+i)};
				}
			}
			// Check if a pawn can en passant
			if(indexY == enPassantRow) {
				for(byte i = -1; i < 2; i+=2) {
					if(indexX + i >= 0 && indexX + i < 8) {
						if(enPassants[board[indexY][indexX].color ? 0 : 1][indexX + i]) {
							possibleMoves[count++] = new byte[]{(byte) (indexY+pawnMoveDirection), (byte) (indexX+i)};
						}
					}
				}
			}
		// If the pawn is pinned from behind or in front
		} else if(pinnedDirection[1] == 0) {
			if(board[indexY+pawnMoveDirection][indexX].type == Type.EMPTY) {
				possibleMoves[count++] = new byte[]{(byte) (indexY+pawnMoveDirection), indexX};
			}
			if(indexY == pawnDoubleJumpRow && board[indexY+2*pawnMoveDirection][indexX].type == Type.EMPTY && board[indexY+pawnMoveDirection][indexX].type == Type.EMPTY) {
				possibleMoves[count++] = new byte[]{(byte) (indexY+2*pawnMoveDirection), indexX};
			}
		// If a pawn is pinned from the side, it can not move
		} else if(pinnedDirection[0] == 0) {
			return null;
		// If the pawn is pinned diagonally, it can only take pieces in that diagonal
		} else {
			/**
			 * If a pawn is pinned from in front from the left, it can only take left
			 * If a pawn is pinned from behind from the left, it can only take right
			 * If a pawn is pinned in front from the right, it can only take right
			 * If a pawn is pinned from behind the right, it can only take left 
			 **/
			// -1 Means pinned from behind, 1 means pinned from infront
			byte pinnedFromBehindOrInFront = (byte) (pinnedDirection[0] == pawnMoveDirection ? -1 : 1);
			// -1 Means pinned from left, 1 means pinned from right
			byte pinnedFromLeftOrRight = (byte) -pinnedDirection[1];
			// Which way the pawn can move given its pin
			byte moveDirection = (byte) (pinnedFromBehindOrInFront * pinnedFromLeftOrRight);
			// If the x position is on the board and is taking an enemy Type.
			if((board[indexY+pawnMoveDirection][indexX+moveDirection].type != Type.EMPTY && 
			   board[indexY+pawnMoveDirection][indexX+moveDirection].color != board[indexY][indexX].color) &&
			   indexX+moveDirection >= 0 && indexX+moveDirection < 8) {
				possibleMoves[count++] = new byte[]{(byte) (indexY+pawnMoveDirection), (byte) (indexX+moveDirection)};
			}
			// If the pawn is on the en passant row, and can take on the board, and the pawn it is trying to take just moved up
			if(indexY == enPassantRow && indexX+moveDirection >= 0 && 
			   indexX+moveDirection < 8 && 
			   enPassants[board[indexY][indexX].color ? 0 : 1][indexX+moveDirection]) {
				possibleMoves[count++] = new byte[]{(byte) (indexY+pawnMoveDirection), (byte) (indexX+moveDirection)};
			}
		}
		if(inCheck[board[indexY][indexX].color ? 1 : 0]) {
			return getBlockingMovesFromPossibleMoves(resizeArray(possibleMoves, count), board[indexY][indexX].color, true);
		} else {
			return resizeArray(possibleMoves, count);
		}
	}
	// This gets the moves a pawn can make given its position
	public byte[][] getKnightMoves(byte indexY, byte indexX, boolean getDefence) {
		// The knight moves componetized
		byte[] knightMovesY = new byte[]{-2, -2, 2, 2, -1, 1, -1, 1};
		byte[] knightMovesX = new byte[]{1, -1, 1, -1, -2, -2, 2, 2};
		// All the possible moves the knight can make
		byte[][] possibleMoves = new byte[8][2];
		// The direction the knight is pinned
		byte[] pinnedDirection = getPinnedDirection(indexY, indexX);
		// Used to resize the possibleMoves array
		byte count = 0;
		// Used to see if the other color king can move to a square
		if(getDefence) {
			for(byte i = 0; i < 8; i++) {
				if(indexY + knightMovesY[i] >= 0 && 
				   indexY + knightMovesY[i] < 8 && 
				   indexX + knightMovesX[i] >= 0 && 
				   indexX + knightMovesX[i] < 8) {
					possibleMoves[count++] = new byte[]{(byte) (indexY + knightMovesY[i]), (byte) (indexX + knightMovesX[i])};
				}
			}
			return resizeArray(possibleMoves, count);
		// If the knight is not pinned
		} else if(pinnedDirection == null) {
			// For each knight move, see if it is a legal move
			for(byte i = 0; i < 8; i++) {
				if(indexY + knightMovesY[i] >= 0 && 
				   indexY + knightMovesY[i] < 8 && 
				   indexX + knightMovesX[i] >= 0 && 
				   indexX + knightMovesX[i] < 8) {
					if(board[indexY + knightMovesY[i]][indexX + knightMovesX[i]].color != board[indexY][indexX].color ||
					   board[indexY + knightMovesY[i]][indexX + knightMovesX[i]].type == Type.EMPTY) {
						possibleMoves[count++] = new byte[]{(byte) (indexY + knightMovesY[i]), (byte) (indexX + knightMovesX[i])};
					}
				}
			}
		// Knights can not move if they are pinned
		} else {
			return null;
		}
		if(inCheck[board[indexY][indexX].color ? 1 : 0]) {
			return getBlockingMovesFromPossibleMoves(resizeArray(possibleMoves, count), board[indexY][indexX].color, false);
		} else {
			return resizeArray(possibleMoves, count);
		}
	}
	// This gets the moves a bishop can make given its position
	public byte[][] getBishopMoves(byte indexY, byte indexX, boolean getDefence) {
		byte[] bishopMovesY = new byte[]{1, 1, -1, -1};
		byte[] bishopMovesX = new byte[]{-1, 1, -1, 1};
		byte[] pinnedDirection = getPinnedDirection(indexY, indexX);
		byte[][] possibleMoves = new byte[14][2];
		byte count = 0;
		byte currentY;
		byte currentX;
		// If not pinned
		if(pinnedDirection == null || getDefence) {
			for(byte i = 0; i < 4; i++) {
				currentY = (byte) (indexY + bishopMovesY[i]);
				currentX = (byte) (indexX + bishopMovesX[i]);
				while(currentY >= 0 && currentY < 8 && currentX >= 0 && currentX < 8) {
					if(board[currentY][currentX].type != Type.EMPTY) {
						if(board[currentY][currentX].color != board[indexY][indexX].color || getDefence) {
							possibleMoves[count++] = new byte[] {currentY, currentX};
						}
						// Got rid of the NOT and switched the bodies
						if(getDefence && board[currentY][currentX].type == Type.KING) {
							currentY += bishopMovesY[i];
							currentX += bishopMovesX[i];
						} else {
							break;
						}
					} else {
						possibleMoves[count++] = new byte[] {currentY, currentX};
						currentY += bishopMovesY[i];
						currentX += bishopMovesX[i];
					}
				}
			}
			if(getDefence) {
				return resizeArray(possibleMoves, count);
			}
		} else if(pinnedDirection[0] == 0 || pinnedDirection[1] == 0) {
			return null;
		// If pinned diagonally 
		} else {
			byte[][] bishopMoves = new byte[][]{pinnedDirection, {(byte) -pinnedDirection[0], (byte) -pinnedDirection[1]}};
			for(byte i = -1; i < 2; i+=2) {
				currentY = (byte) (indexY + bishopMoves[(i+1)/2][1]);
				currentX = (byte) (indexX + bishopMoves[(i+1)/2][0]);
				while(currentY >= 0 && currentY < 8 && currentX >= 0 && currentX < 8) {
					if(board[currentY][currentX].type != Type.EMPTY) {
						if(board[currentY][currentX].color != board[indexY][indexX].color) {
							possibleMoves[count++] = new byte[] {currentY, currentX};
						}
						break;
					} else {
						possibleMoves[count++] = new byte[] {currentY, currentX};
						currentY += bishopMoves[(i+1)/2][1];
						currentX += bishopMoves[(i+1)/2][0];
					}
				}
			}
		}
		if(inCheck[board[indexY][indexX].color ? 1 : 0]) {
			return getBlockingMovesFromPossibleMoves(resizeArray(possibleMoves, count), board[indexY][indexX].color, false);
		} else {
			return resizeArray(possibleMoves, count);
		}
	}
	// This gets the moves a bishop can make given its position
	public byte[][] getRookMoves(byte indexY, byte indexX, boolean getDefence) {
		byte[] rookMovesY = new byte[]{1, -1, 0, 0};
		byte[] rookMovesX = new byte[]{0, 0, -1, 1};
		byte[] pinnedDirection = getPinnedDirection(indexY, indexX);
		byte[][] possibleMoves = new byte[20][2];
		byte count = 0;
		byte currentY;
		byte currentX;
		// If not pinned
		if(pinnedDirection == null || getDefence) {
			for(byte i = 0; i < 4; i++) {
				currentY = (byte) (indexY + rookMovesY[i]);
				currentX = (byte) (indexX + rookMovesX[i]);
				while(currentY >= 0 && currentY < 8 && currentX >= 0 && currentX < 8) {
					if(board[currentY][currentX].type != Type.EMPTY) {
						if((board[currentY][currentX].color != board[indexY][indexX].color || getDefence)) {
							possibleMoves[count++] = new byte[] {currentY, currentX};
						}
						// Got rid of the NOT and switched the bodies
						if(getDefence && board[currentY][currentX].type == Type.KING && board[currentY][currentX].color != board[indexY][indexX].color) {
							currentY += rookMovesY[i];
							currentX += rookMovesX[i];
						} else {
							break;
						}
					} else {
						if(currentY != indexY || currentX != indexX) {
							possibleMoves[count++] = new byte[] {currentY, currentX};
						}
						currentY += rookMovesY[i];
						currentX += rookMovesX[i];
					}
				}
			}
			if(getDefence) {
				return resizeArray(possibleMoves, count);
			}
		// If the rook is pinned on a column or row
		} else if(pinnedDirection[0] == 0 || pinnedDirection[1] == 0) {
			byte[][] rookMoves = new byte[][]{pinnedDirection, {(byte) -pinnedDirection[0], (byte) -pinnedDirection[1]}};
			for(byte i = -1; i < 2; i+=2) {
				currentY = (byte) (indexY + rookMoves[(i+1)/2][0]);
				currentX = (byte) (indexX + rookMoves[(i+1)/2][1]);
				while(currentY >= 0 && currentY < 8 && currentX >= 0 && currentX < 8) {
					if(board[currentY][currentX].type != Type.EMPTY) {
						if(board[currentY][currentX].color != board[indexY][indexX].color && (currentY != indexY && currentX != indexX)) {
							possibleMoves[count++] = new byte[] {currentY, currentX};
						}
						break;
					} else {
						if(currentY != indexY && currentX != indexX) {
							possibleMoves[count++] = new byte[] {currentY, currentX};
						}
						currentY += rookMoves[(i+1)/2][0];
						currentX += rookMoves[(i+1)/2][1];
					}
				}
			}
		// If pinned diagonally, the rook can not move
		} else {
			return null;
		}
		if(inCheck[board[indexY][indexX].color ? 1 : 0]) {
			return getBlockingMovesFromPossibleMoves(resizeArray(possibleMoves, count), board[indexY][indexX].color, false);
		} else {
			return resizeArray(possibleMoves, count);
		}
	}
	// This gets the moves a queen can make given its position
	public byte[][] getQueenMoves(byte indexY, byte indexX, boolean getDefence) {
		// The queen moves are really just the sum of the rook and bishop moves
		byte[][] rookMoves = getRookMoves(indexY, indexX, getDefence);
		byte[][] bishopMoves = getBishopMoves(indexY, indexX, getDefence);
		byte[][] queenMoves = new byte[(rookMoves == null ? 0 : rookMoves.length) + (bishopMoves == null ? 0 : bishopMoves.length)][2];
		for(byte i = 0; i < (rookMoves == null ? 0 : rookMoves.length); i++) {
			queenMoves[i] = rookMoves[i];
		}
		for(byte i = 0; i < (bishopMoves == null ? 0 : bishopMoves.length); i++) {
			queenMoves[(rookMoves == null ? 0 : rookMoves.length) + i] = bishopMoves[i];
		}
		return queenMoves;
	}
	// This gets the moves a king can make given its position
	public byte[][] getKingMoves(byte indexY, byte indexX, boolean getDefence) {
		byte[] kingMovesY = new byte[]{-1, 0, 1, 1, 1, 0, -1, -1};
		byte[] kingMovesX = new byte[]{-1, -1, -1, 0, 1, 1, 1, 0};
		byte[][] possibleMoves = new byte[10][2];
		byte count = 0;
		if(getDefence) {
			possibleMoves = new byte[8][2];
			for(byte i = 0; i < 8; i++) {
				possibleMoves[count++] = new byte[]{(byte) (indexY + kingMovesY[i]), (byte) (indexX + kingMovesX[i])};
			}
			return possibleMoves;
		} else {
			for(byte i = 0; i < 8; i++) {
				if(isSquareSafe((byte) (indexY + kingMovesY[i]), (byte) (indexX + kingMovesX[i]), board[indexY][indexX].color) &&
				   indexY + kingMovesY[i] >= 0 &&
				   indexY + kingMovesY[i] < 8 &&
				   indexX + kingMovesX[i] >= 0 &&
				   indexX + kingMovesX[i] < 8 &&
				   (board[indexY + kingMovesY[i]][indexX + kingMovesX[i]].type == Type.EMPTY || 
				   board[indexY + kingMovesY[i]][indexX + kingMovesX[i]].color != board[indexY][indexX].color)) {
					possibleMoves[count++] = new byte[]{(byte) (indexY + kingMovesY[i]), (byte) (indexX + kingMovesX[i])};
				}
			}
			// Castling logic
			if(!kingsMoved[board[indexY][indexX].color ? 1 : 0]) {
				for(byte i = -1; i < 2; i += 2) {
					if(!rooksMoved[board[indexY][indexX].color ? 1 : 0][(i+1)/2]) {
						if(isSquareSafe(indexY, (byte) (indexX + i), board[indexY][indexX].color) &&
						   isSquareSafe(indexY, (byte) (indexX + 2*i), board[indexY][indexX].color) &&
						   board[indexY][indexX + i].type == Type.EMPTY && board[indexY][indexX + 2*i].type == Type.EMPTY) {
							possibleMoves[count++] = new byte[]{indexY, (byte) (indexX + 2*i)};
						}
					}
				}
			}
		}
		return resizeArray(possibleMoves, count);
	}
}
