import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import java.awt.Font;

public class ChessWindow {
	Board game;
	byte currentClickY;
	byte currentClickX;
	Color white;
	Color black;
	Color highlightedWhite;
	Color highlightedBlack;
	Color possibleWhite;
	Color possibleBlack;
	Color[][] squareColors;
	Image[] pieceImages;
	byte[][] possibleMoves;
	byte[] pieceClicked;
	boolean promotion;
	byte[] promotionMove;
	byte[] lastMove;
	byte gameMoveReturn;
	boolean playerColor;
	Network network;
	byte[][] computerMove = new byte[2][2];
	
	private JFrame frame;
	private JPanel square_1;
	private JPanel square_2;
	
	public Image getImageFromPiece(Piece piece) {
		boolean color = !piece.color;
		Image output;
		switch(piece.type) {
			case PAWN:
				output = pieceImages[0 + (color ? 0 : 6)];
				return output.getScaledInstance(80, 80,  java.awt.Image.SCALE_SMOOTH); 
			case ROOK:
				output = pieceImages[1 + (color ? 0 : 6)];
				return output.getScaledInstance(80, 80,  java.awt.Image.SCALE_SMOOTH); 
			case KNIGHT:
				output = pieceImages[2 + (color ? 0 : 6)];
				return output.getScaledInstance(80, 80,  java.awt.Image.SCALE_SMOOTH); 
			case BISHOP:
				output = pieceImages[3 + (color ? 0 : 6)];
				return output.getScaledInstance(80, 80,  java.awt.Image.SCALE_SMOOTH); 
			case QUEEN:
				output = pieceImages[4 + (color ? 0 : 6)];
				return output.getScaledInstance(80, 80,  java.awt.Image.SCALE_SMOOTH); 
			case KING:
				output = pieceImages[5 + (color ? 0 : 6)];
				return output.getScaledInstance(80, 80,  java.awt.Image.SCALE_SMOOTH); 
			default:
				return null;
		}
	}
	
	public void drawBoard(boolean color) {
		for(byte y = 0; y < 8; y++) {
    		for(byte x = 0; x < 8; x ++) {
    			byte usingX = (byte) (color ? (7 - x) : x);
        		byte usingY = (byte) (color ? (7 - y) : y);
				JPanel square = new JPanel();
				if(lastMove[0] == -1) {
					square.setBackground(squareColors[usingY][usingX]);
				} else {
					if(((lastMove[0] == usingY && lastMove[1] == usingX) || 
					   (lastMove[2] == usingY && lastMove[3] == usingX)) && 
					   !(squareColors[usingY][usingX] == possibleBlack || 
					   squareColors[usingY][usingX] == possibleWhite)) {
						square.setBackground((y+x)%2 == 0 ? highlightedWhite : highlightedBlack);
					} else {
						square.setBackground(squareColors[usingY][usingX]);
					}
				}
        		square.setBounds(x*100, y*100, 100, 100);
                frame.getContentPane().add(square);
    		}
		}
    }
	
	public void drawEndScreen(String endMessage) {
		frame.getContentPane().removeAll();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBackground(Color.DARK_GRAY);
        frame.getContentPane().setLayout(null);
        frame.setResizable(false);
		
        JPanel square = new JPanel();
		square.setBackground(new Color(255, 255, 255));
		square.setBounds(100, 100, 605, 100);
        frame.getContentPane().add(square);
        
        JLabel lblNewLabel = new JLabel(endMessage);
        lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 40));
        square.add(lblNewLabel);
        square.add(lblNewLabel);
        
        square_1 = new JPanel();
        square_1.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		drawHomeScreen();
        	}
        });
		square_1.setBackground(new Color(255, 255, 255));
		square_1.setBounds(250, 400, 300, 100);
        frame.getContentPane().add(square_1);
        
        lblNewLabel = new JLabel("Home Screen");
        lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 40));
        square_1.add(lblNewLabel);
        
        square = new JPanel();
		square.setBackground(new Color(50, 50, 50));
		square.setBounds(0, 0, 805, 829);
        frame.getContentPane().add(square);

        frame.setVisible(true);
	}
	
	public void drawHomeScreen() {
		game = new Board();
		gameMoveReturn = 1;
		currentClickY = 0;
		currentClickX = 0;
		white = new Color(213, 196, 161);
		black = new Color(87, 65, 47);
		highlightedWhite = new Color(225, 225, 100);
		highlightedBlack = new Color(200, 200, 75);
		possibleWhite = new Color(225, 150, 150);
		possibleBlack = new Color(200, 125, 125);
		possibleMoves = null;
		pieceClicked = null;
		promotion = false;
		promotionMove = null;
		lastMove = new byte[]{-1, -1, -1, -1};
		
		squareColors = new Color[][]{
			 {white, black, white, black, white, black, white, black},
			 {black, white, black, white, black, white, black, white},
			 {white, black, white, black, white, black, white, black},
			 {black, white, black, white, black, white, black, white},
			 {white, black, white, black, white, black, white, black},
			 {black, white, black, white, black, white, black, white},
			 {white, black, white, black, white, black, white, black},
			 {black, white, black, white, black, white, black, white}};
		
		frame.getContentPane().removeAll();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBackground(Color.DARK_GRAY);
        frame.getContentPane().setLayout(null);
        frame.setResizable(false);
		
        JPanel square = new JPanel();
		square.setBackground(new Color(255, 255, 255));
		square.setBounds(0, 0, 805, 200);
        frame.getContentPane().add(square);
        
        JLabel lblNewLabel = new JLabel("  Chess!");
        lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 100));
        square.add(lblNewLabel);
        lblNewLabel = new JLabel("\n- By Drew");
        lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 10));
        square.add(lblNewLabel);
        
        square_1 = new JPanel();
        square_1.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		draw(false);
        	}
        });
		square_1.setBackground(new Color(255, 255, 255));
		square_1.setBounds(250, 400, 300, 100);
        frame.getContentPane().add(square_1);
        
        lblNewLabel = new JLabel("Play!");
        lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 70));
        square_1.add(lblNewLabel);
        
        square_2 = new JPanel();
        square_2.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		Random random = new Random();
        		drawComputer(random.nextBoolean());
        	}
        });
        square_2.setBackground(new Color(255, 255, 255));
        square_2.setBounds(250, 600, 300, 100);
        frame.getContentPane().add(square_2);
        
        lblNewLabel = new JLabel("Computer!");
        lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 65));
        square_2.add(lblNewLabel);
        
        square = new JPanel();
		square.setBackground(new Color(50, 50, 50));
		square.setBounds(0, 0, 805, 829);
        frame.getContentPane().add(square);

        frame.setVisible(true);
	}
	
	public void drawComputer(boolean color) {
		network = Network.loadFromFile(getPathToFile());
		frame.getContentPane().removeAll();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBackground(Color.LIGHT_GRAY);
        frame.getContentPane().setLayout(null);
        frame.setResizable(false);
        System.out.println("The network evaluates this position as: " + network.feedForward(network.getInputsFromPosition(game.board, game.fiftyMoveRule, game.turn ? 1 : -1, game.enPassants, game.rooksMoved, game.kingsMoved))[0] + ".");
        this.playerColor = color;
        
        if(promotion) {
        	int xPos = (promotionMove[1] == 0 ? (7-promotionMove[1])*100 : promotionMove[1]*100) + 12;
        	int yPos = 0;
        	Type[] promotionPieces = new Type[]{Type.QUEEN, Type.ROOK, Type.BISHOP, Type.KNIGHT};
            for(byte y = 0; y < 4; y++) {
            	JLabel label = new JLabel("");
    			label.setIcon(new ImageIcon(getImageFromPiece(new Piece(promotionPieces[y], game.board[pieceClicked[0]][pieceClicked[1]].color))));
    			label.setBounds(xPos, y*100, 100, 100);
    	        frame.getContentPane().add(label);
            }
        	JPanel square = new JPanel();
    		square.setBackground(new Color(255, 255, 255));
    		square.setBounds(xPos-12, yPos, 100, 400);
            frame.getContentPane().add(square);
        }
        
        for(byte y = 0; y < 8; y++) {
        	for(byte x = 0; x < 8; x++) {
        		byte usingX = (byte) (color ? (7 - x) : x);
        		byte usingY = (byte) (color ? (7 - y) : y);
        		if(game.board[usingY][usingX].type != Type.EMPTY) {
        			JLabel label = new JLabel("");
        			label.setIcon(new ImageIcon(getImageFromPiece(game.board[usingY][usingX])));
        			label.setBounds(x*100 + 12, y*100, 100, 100);
        	        frame.getContentPane().add(label);
        		}
        	}
        }
		
		drawBoard(color);
		JButton button = new JButton("");
		button.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent arg0) {
        		Point clickLoc = MouseInfo.getPointerInfo().getLocation();
        		int clickY = ((clickLoc.y - 29) - (clickLoc.y - 29)%100 - 100);
        		int clickX = ((clickLoc.x - 5) - (clickLoc.x - 5)%100 - 100);
        		doClickComputer(color ? (700 - clickY) : clickY, color ? (700 - clickX) : clickX, color);
        	}
        });
		button.setOpaque(false);
		button.setContentAreaFilled(false);
		button.setBorderPainted(false);
		button.setBounds(0, 0, 800, 800);
        frame.getContentPane().add(button);
        frame.setVisible(true);
        
        if(game.turn != color) {
			byte[] bestMove = this.network.getBestMove(game);
			gameMoveReturn = game.doMove(bestMove[0], bestMove[1], bestMove[2], bestMove[3], bestMove[4]);
			squareColors[computerMove[0][0]][computerMove[0][1]] = (computerMove[0][0]+computerMove[0][1])%2 == 0 ? white : black;
			squareColors[computerMove[1][0]][computerMove[1][1]] = (computerMove[1][0]+computerMove[1][1])%2 == 0 ? white : black;
			computerMove[0] = new byte[]{bestMove[0], bestMove[1]};
			computerMove[1] = new byte[]{bestMove[2], bestMove[3]};
			squareColors[bestMove[0]][bestMove[1]] = (bestMove[0]+bestMove[1])%2 == 0 ? highlightedWhite : highlightedBlack;
			squareColors[bestMove[2]][bestMove[3]] = (bestMove[2]+bestMove[3])%2 == 0 ? highlightedWhite : highlightedBlack;
			switch(gameMoveReturn) {
				case 1:
					draw(game.turn);
					break;
				case 2:
					drawEndScreen("Draw by insufficient material.");
					break;
				case 3:
					drawEndScreen("Draw by repetition.");
					break;
				case 4:
					drawEndScreen("Draw by fifty move rule.");
					break;
				case 5:
					drawEndScreen("Draw by stalemate.");
					break;
				case 6:
					drawEndScreen("Black wins by checkmate!");
					break;
				case 7:
					drawEndScreen("White wins by checkmate!");
					break;
			}
			drawComputer(game.turn);
        }
	}
	
	public void draw(boolean color) {
		frame.getContentPane().removeAll();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBackground(Color.LIGHT_GRAY);
        frame.getContentPane().setLayout(null);
        frame.setResizable(false);
  
        if(promotion) {
        	int xPos = promotionMove[0] == 0 ? (promotionMove[1]*100 + 12) : ((7-promotionMove[1])*100 + 12);
        	int yPos = 0;
        	Type[] promotionPieces = new Type[]{Type.QUEEN, Type.ROOK, Type.BISHOP, Type.KNIGHT};
            for(byte y = 0; y < 4; y++) {
            	JLabel label = new JLabel("");
    			label.setIcon(new ImageIcon(getImageFromPiece(new Piece(promotionPieces[y], game.board[pieceClicked[0]][pieceClicked[1]].color))));
    			label.setBounds(xPos, y*100 + yPos, 100, 100);
    	        frame.getContentPane().add(label);
            }
        	JPanel square = new JPanel();
    		square.setBackground(new Color(255, 255, 255));
    		square.setBounds(xPos-12, yPos, 100, 400);
            frame.getContentPane().add(square);
        }
        
        for(byte y = 0; y < 8; y++) {
        	for(byte x = 0; x < 8; x++) {
        		byte usingX = (byte) (color ? (7 - x) : x);
        		byte usingY = (byte) (color ? (7 - y) : y);
        		if(game.board[usingY][usingX].type != Type.EMPTY) {
        			JLabel label = new JLabel("");
        			label.setIcon(new ImageIcon(getImageFromPiece(game.board[usingY][usingX])));
        			label.setBounds(x*100 + 12, y*100, 100, 100);
        	        frame.getContentPane().add(label);
        		}
        	}
        }
		
		drawBoard(color);
		JButton button = new JButton("");
		button.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent arg0) {
        		Point clickLoc = MouseInfo.getPointerInfo().getLocation();
        		int clickY = ((clickLoc.y - 29) - (clickLoc.y - 29)%100 - 100);
        		int clickX = ((clickLoc.x - 5) - (clickLoc.x - 5)%100 - 100);
        		doClick(color ? (700 - clickY) : clickY, color ? (700 - clickX) : clickX);
        	}
        });
		button.setOpaque(false);
		button.setContentAreaFilled(false);
		button.setBorderPainted(false);
		button.setBounds(0, 0, 800, 800);
        frame.getContentPane().add(button);
        frame.setVisible(true);
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ChessWindow window = new ChessWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public String getPathToFile() {
		return ChessWindow.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
	}
	
	public ChessWindow() {
		initialize();
	}
	
	private void initialize() {
		game = new Board();
		currentClickY = 0;
		currentClickX = 0;
		white = new Color(213, 196, 161);
		black = new Color(87, 65, 47);
		highlightedWhite = new Color(225, 225, 100);
		highlightedBlack = new Color(200, 200, 75);
		possibleWhite = new Color(225, 150, 150);
		possibleBlack = new Color(200, 125, 125);
		squareColors = new Color[8][8];
		pieceImages = new Image[12];
		possibleMoves = null;
		pieceClicked = null;
		promotion = false;
		promotionMove = null;
		lastMove = new byte[]{-1, -1, -1, -1};
		
		frame = new JFrame("Chess");
		frame.setBounds(100, 100, 815, 829);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBackground(Color.LIGHT_GRAY);
        frame.getContentPane().setLayout(null);
        frame.setResizable(false);

        
        pieceImages[0] = new ImageIcon(this.getClass().getResource("/WhitePawn.png")).getImage();
        pieceImages[1] = new ImageIcon(this.getClass().getResource("/WhiteRook.png")).getImage();
        pieceImages[2] = new ImageIcon(this.getClass().getResource("/WhiteKnight.png")).getImage();
        pieceImages[3] = new ImageIcon(this.getClass().getResource("/WhiteBishop.png")).getImage();
        pieceImages[4] = new ImageIcon(this.getClass().getResource("/WhiteQueen.png")).getImage();
        pieceImages[5] = new ImageIcon(this.getClass().getResource("/WhiteKing.png")).getImage();
        pieceImages[6] = new ImageIcon(this.getClass().getResource("/BlackPawn.png")).getImage();
        pieceImages[7] = new ImageIcon(this.getClass().getResource("/BlackRook.png")).getImage();
        pieceImages[8] = new ImageIcon(this.getClass().getResource("/BlackKnight.png")).getImage();
        pieceImages[9] = new ImageIcon(this.getClass().getResource("/BlackBishop.png")).getImage();
        pieceImages[10] = new ImageIcon(this.getClass().getResource("/BlackQueen.png")).getImage();
        pieceImages[11] = new ImageIcon(this.getClass().getResource("/BlackKing.png")).getImage();
        
        
        squareColors = new Color[][]{{white, black, white, black, white, black, white, black},
        							 {black, white, black, white, black, white, black, white},
        							 {white, black, white, black, white, black, white, black},
        							 {black, white, black, white, black, white, black, white},
        							 {white, black, white, black, white, black, white, black},
        							 {black, white, black, white, black, white, black, white},
        							 {white, black, white, black, white, black, white, black},
        							 {black, white, black, white, black, white, black, white}};
        
        drawHomeScreen();
	}
	
	public String positionToString(byte[] position) {
		char[] letters = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
		return letters[position[1]] + String.valueOf(7-position[0] + 1);
	}
	// Checks to see if an array of moves contains a square
	public boolean arrayContains(byte[][] array, byte[] value) {
		if(array != null) {
			for(byte i = 0; i < array.length; i++) {
				if(array[i][0] == value[0] && array[i][1] == value[1]) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void doClickComputer(int indexY, int indexX, boolean playerColor) {
		if(playerColor == game.turn) {
			byte[] click = new byte[]{(byte) (indexY/100), (byte) (indexX/100)};
			byte clickY = (byte) (indexY/100);
			byte clickX = (byte) (indexX/100);
			if(!promotion) {
				if(possibleMoves != null) {
					for(byte i = 0; i < possibleMoves.length; i++) {
						squareColors[possibleMoves[i][0]][possibleMoves[i][1]] = (possibleMoves[i][0]+possibleMoves[i][1])%2 == 0 ? white : black;
					}
				}
				if(pieceClicked != null) {
					squareColors[pieceClicked[0]][pieceClicked[1]] = (pieceClicked[0]+pieceClicked[1])%2 == 0 ? white : black;
				}
		
				if(arrayContains(possibleMoves, click)) {
					if(game.board[pieceClicked[0]][pieceClicked[1]].type == Type.PAWN && (game.board[pieceClicked[0]][pieceClicked[1]].color ? 7 : 0) == clickY) {
						promotionMove = new byte[]{clickY, clickX};
						promotion = true;
					} else {
						lastMove[0] = pieceClicked[0];
						lastMove[1] = pieceClicked[1];
						lastMove[2] = clickY;
						lastMove[3] = clickX;
						gameMoveReturn = game.doMove(pieceClicked[0], pieceClicked[1], clickY, clickX, (byte) 0);
						possibleMoves = null;
						pieceClicked = null;
					}
				} else if(game.board[clickY][indexX/100].type != Type.EMPTY) {
					possibleMoves = game.getPossibleMovesFromCoords(clickY, clickX);
					pieceClicked = new byte[]{clickY, clickX};
					if(possibleMoves!=null) {
						for(byte i = 0; i < possibleMoves.length; i++) {
							squareColors[possibleMoves[i][0]][possibleMoves[i][1]] = (possibleMoves[i][0]+possibleMoves[i][1])%2 == 0 ? possibleWhite : possibleBlack;
						}
					}
					squareColors[clickY][clickX] = (clickY+clickX)%2 == 0 ? highlightedWhite : highlightedBlack;
				} else {
					if(possibleMoves != null) {
						for(byte i = 0; i < possibleMoves.length; i++) {
							squareColors[possibleMoves[i][0]][possibleMoves[i][1]] = (possibleMoves[i][0]+possibleMoves[i][1])%2 == 0 ? white : black;
						}
					}
					if(pieceClicked != null) {
						squareColors[pieceClicked[0]][pieceClicked[1]] = (pieceClicked[0]+pieceClicked[1])%2 == 0 ? white : black;
					}
					possibleMoves = null;
					pieceClicked = null;
				}
			} else {
				if(clickX == promotionMove[1]) {
					for(byte y = 0; y < 4; y++) {
						if(clickY == (promotionMove[0] == 0 ? y : (7-y))) {
	            			promotion = false;
	            			gameMoveReturn = game.doMove(pieceClicked[0], pieceClicked[1], promotionMove[0], promotionMove[1], y);
	            			promotionMove = null;
	            			possibleMoves = null;
	    					pieceClicked = null;
	            			break;
	            		}
	            	}
				}
			}
			
			switch(gameMoveReturn) {
				case 1:
					drawComputer(this.playerColor);
					break;
				case 2:
					drawEndScreen("Draw by insufficient material.");
					break;
				case 3:
					drawEndScreen("Draw by repetition.");
					break;
				case 4:
					drawEndScreen("Draw by fifty move rule.");
					break;
				case 5:
					drawEndScreen("Draw by stalemate.");
					break;
				case 6:
					drawEndScreen("Black wins by checkmate!");
					break;
				case 7:
					drawEndScreen("White wins by checkmate!");
					break;
			}
			
		}
	}
	
	public void doClick(int indexY, int indexX) {
		byte[] click = new byte[]{(byte) (indexY/100), (byte) (indexX/100)};
		byte clickY = (byte) (indexY/100);
		byte clickX = (byte) (indexX/100);
		if(!promotion) {
			if(possibleMoves != null) {
				for(byte i = 0; i < possibleMoves.length; i++) {
					squareColors[possibleMoves[i][0]][possibleMoves[i][1]] = (possibleMoves[i][0]+possibleMoves[i][1])%2 == 0 ? white : black;
				}
			}
			if(pieceClicked != null) {
				squareColors[pieceClicked[0]][pieceClicked[1]] = (pieceClicked[0]+pieceClicked[1])%2 == 0 ? white : black;
			}
	
			if(arrayContains(possibleMoves, click)) {
				if(game.board[pieceClicked[0]][pieceClicked[1]].type == Type.PAWN && (game.board[pieceClicked[0]][pieceClicked[1]].color ? 7 : 0) == clickY) {
					promotionMove = new byte[]{clickY, clickX};
					promotion = true;
				} else {
					lastMove[0] = pieceClicked[0];
					lastMove[1] = pieceClicked[1];
					lastMove[2] = clickY;
					lastMove[3] = clickX;
					gameMoveReturn = game.doMove(pieceClicked[0], pieceClicked[1], clickY, clickX, (byte) 0);
					possibleMoves = null;
					pieceClicked = null;
				}
				draw(game.turn);
			} else if(game.board[clickY][indexX/100].type != Type.EMPTY) {
				possibleMoves = game.getPossibleMovesFromCoords(clickY, clickX);
				pieceClicked = new byte[]{clickY, clickX};
				if(possibleMoves!=null) {
					for(byte i = 0; i < possibleMoves.length; i++) {
						squareColors[possibleMoves[i][0]][possibleMoves[i][1]] = (possibleMoves[i][0]+possibleMoves[i][1])%2 == 0 ? possibleWhite : possibleBlack;
					}
				}
				squareColors[clickY][clickX] = (clickY+clickX)%2 == 0 ? highlightedWhite : highlightedBlack;
				draw(game.turn);
			} else {
				if(possibleMoves != null) {
					for(byte i = 0; i < possibleMoves.length; i++) {
						squareColors[possibleMoves[i][0]][possibleMoves[i][1]] = (possibleMoves[i][0]+possibleMoves[i][1])%2 == 0 ? white : black;
					}
				}
				if(pieceClicked != null) {
					squareColors[pieceClicked[0]][pieceClicked[1]] = (pieceClicked[0]+pieceClicked[1])%2 == 0 ? white : black;
				}
				possibleMoves = null;
				pieceClicked = null;
				draw(game.turn);
			}
		} else {
			if(clickX == promotionMove[1]) {
				for(byte y = 0; y < 4; y++) {
					if(clickY == (promotionMove[0] == 0 ? y : (7-y))) {
            			promotion = false;
            			gameMoveReturn = game.doMove(pieceClicked[0], pieceClicked[1], promotionMove[0], promotionMove[1], y);
            			promotionMove = null;
            			possibleMoves = null;
    					pieceClicked = null;
            			draw(game.turn);
            			break;
            		}
            	}
			}
		}
		
		switch(gameMoveReturn) {
			case 1:
				draw(game.turn);
				break;
			case 2:
				drawEndScreen("Draw by insufficient material.");
				break;
			case 3:
				drawEndScreen("Draw by repetition.");
				break;
			case 4:
				drawEndScreen("Draw by fifty move rule.");
				break;
			case 5:
				drawEndScreen("Draw by stalemate.");
				break;
			case 6:
				drawEndScreen("Black wins by checkmate!");
				break;
			case 7:
				drawEndScreen("White wins by checkmate!");
				break;
		}
		
	}
}
