package breakthrough;

import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import boardgame.Move;

public class EnhancedBTBoard extends BTBoard {
	Vector<MyPiece> blackPieces;

	Vector<MyPiece> whitePieces;

	public EnhancedBTBoard() {
		super();
		blackPieces = new Vector<MyPiece>(16);
		whitePieces = new Vector<MyPiece>(16);
		initializePieceTables();
	}

	public EnhancedBTBoard(BTBoard board) {
		super(board);
		blackPieces = new Vector<MyPiece>(16);
		whitePieces = new Vector<MyPiece>(16);
		UpdatePieceTables(board);
	}

	public EnhancedBTBoard(EnhancedBTBoard board) {
		super(board);
		blackPieces = new Vector<MyPiece>(board.blackPieces.size());
		whitePieces = new Vector<MyPiece>(board.whitePieces.size());
		
		for (MyPiece piece : board.blackPieces) {
			blackPieces.add(new MyPiece( piece.getPosition()));
		}
		
		for (MyPiece piece : board.whitePieces) {
			whitePieces.add(new MyPiece( piece.getPosition()));
		}
		
	}

	public void initializePieceTables() {
		for (int i = 0; i < 2 * SIZE; i++) {
			MyPiece piece = new MyPiece(i);
			whitePieces.add(piece);
		}

		for (int i = SIZE * SIZE - 2 * SIZE; i < SIZE * SIZE; i++) {
			MyPiece piece = new MyPiece(i);
			blackPieces.add(piece);
		}
	}

	public void UpdatePieceTables(BTBoard board) {
		for (int i = 0; i < BTBoard.SIZE; i++)
			for (int j = 0; j < BTBoard.SIZE; j++)
				if (board.getPieceAt(i, j) == BTBoard.WHITE) {
					MyPiece piece = new MyPiece(board.getCoord(i, j));
					whitePieces.add(piece);
				} else if (board.getPieceAt(i, j) == BTBoard.BLACK) {
					MyPiece piece = new MyPiece(board.getCoord(i, j));
					blackPieces.add(piece);
				}
	}

	/** Execute a move. */
	public void move(Move mm) throws IllegalArgumentException {
		BTMove m = (BTMove) mm;
		move(m.player, m.orig, m.dest);
	}

	/**
	 * 
	 * @return A vector of all the legal moves for the current position. This
	 *         takes into account whose turn it is
	 */
	public Vector<BTMove> GenerateMoves() {
		final int forward = (turn == BTBoard.WHITE ? BTBoard.SIZE : -BTBoard.SIZE );

		Vector<BTMove> moves = new Vector<BTMove>();

		if (turn == EnhancedBTBoard.BLACK) {
			// For each piece 
			for (MyPiece piece : this.blackPieces) {
				int orig = piece.getPosition();
				// For each possible move
				for (int d = -1; d < 2; d++) {
					int dest = orig + forward + d;
					if (this.isLegal(turn, orig, dest))
						moves.add(new BTMove(turn, orig, dest));
				}
			}
		} else // we need to do the same for white pieces
		{
			// For each piece
			for (MyPiece piece : this.whitePieces) {
				int orig = piece.getPosition();
				// For each possible move
				for (int d = -1; d < 2; d++) {
					int dest = orig + forward + d;
					if (this.isLegal(turn, orig, dest))
						moves.add(new BTMove(turn, orig, dest));
				}
			}
		}
		return moves;
	}

	public void move(int player, int orig, int dest)
			throws IllegalArgumentException {
		if (!isLegal(player, orig, dest))
			throw new IllegalArgumentException("Illegal move: "
					+ BTMove.toTransportable(player, orig, dest));
		moveFast(player, orig, dest);
		
		// Update the pieces
		if (player == EnhancedBTBoard.BLACK) {
			for (MyPiece piece : blackPieces) {
				if (piece.getPosition() == orig)
					piece.setPosition(dest);
			}

			for (MyPiece piece : whitePieces) {
				if (piece.getPosition() == dest){
					whitePieces.remove(piece);
					break; //If we don't, we get an exception
				}
			}

		} else {
			for (MyPiece piece : whitePieces) {
				if (piece.getPosition() == orig)
					piece.setPosition(dest);
			}

			for (MyPiece piece : blackPieces) {
				if (piece.getPosition() == dest){
					blackPieces.remove(piece);
					break; //If we don't, we get an exception
				}
			}

		}
		
		

	}
}
