enum Type {
	EMPTY,
	PAWN,
	KNIGHT,
	BISHOP,
	ROOK,
	QUEEN,
	KING
}
public class Piece {
	Type type;
	boolean color;
	public Piece(Type type, boolean color) {
		this.type = type;
		this.color = color;
	}
	
	public Piece makeCopy() {
		return new Piece(this.type, this.color);
	}
}
