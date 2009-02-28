package breakthrough;

import java.util.Random;

import com.sun.org.apache.xpath.internal.compiler.OpCodes;

import boardgame.Board;
import boardgame.Move;
import boardgame.Player;

import breakthrough.BTFixedPlayer;

/**
 *A random BreakThrough player.
 */
public class BTBreadthFirstPlayer extends Player {

	/** Provide a default public constructor */
	public BTBreadthFirstPlayer() { 
		super("icioba1");
		

	}

	BTFixedPlayer otherPlayer = new BTFixedPlayer();
	

	private int [][] getPieces(BTBoard board, int color ){
		int[][] myPieces = new int[2*BTBoard.SIZE][2];
		
		int next = 0;
		for( int i = 0; i < BTBoard.SIZE; i++ )
			for( int j = 0; j < BTBoard.SIZE; j++ )
				if( board.getPieceAt(i,j) == color ) {
					try{
					myPieces[next][0]= i;
					myPieces[next][1]=j;
					next++;
					}catch (Exception e)
					{
						e.printStackTrace();
					}
				}
		return myPieces;
	}
	
	/** Implement a very stupid way of picking moves */
	public Move chooseMove(Board theboard) {
		int myColor = this.getColor();
		int opColor = 0;
		if (myColor == BTBoard.WHITE)
			opColor = BTBoard.BLACK;
		else opColor = BTBoard.WHITE;
		
		// Cast the arguments to the objects we want to work with    	
		BTBoard board = (BTBoard) theboard;				

		// Find all my pieces
		//TODO:This can be easily optimized to be saved on the board
		

		BTMove nextMove = gen_move(board, myColor, myColor, 0);

		return nextMove;
	}

	private BTMove gen_move(BTBoard board, int myColor, int moveColor, int depth)
	{
		final int forward = (myColor == BTBoard.WHITE ? BTBoard.SIZE : -BTBoard.SIZE );
		int opColor = (myColor == BTBoard.WHITE) ? BTBoard.BLACK : BTBoard.WHITE;

		int[][] myPieces = getPieces(board, myColor);
		
		BTMove bestMove = new BTMove(-1,-1,-1);
		int val = 0;
		
		if (depth < 4){
			if (moveColor == myColor){
				for (int i = 0; i < myPieces.length; i++) {
					for (int dir = -1; dir <= 1; dir++){
						int iDest = myPieces[i][0] + forward;
						int jDest = myPieces[i][1] + dir;
						
						if (board.isLegal(myColor, board.getCoord(myPieces[i][0], myPieces[i][1]), board.getCoord(iDest, jDest))){
						
							//Check if move is legal
							BTMove m = new BTMove( myColor, 
									board.getCoord(myPieces[i][0],myPieces[i][1]), 
									board.getCoord(iDest,jDest) );
							
							BTBoard newBoard = new BTBoard(board);
														
							newBoard.move(m);
							
							val = eval_board(newBoard);
							
							switch (val){
								case 1: bestMove = m;
								case -1: continue;
								default: bestMove = gen_move(newBoard, myColor, opColor, depth+1);							
							}																												
						}
					}					
				}				
				return bestMove;
			}
			else
			{
				//play the opponent
				BTBoard newBoard = new BTBoard(board);
				
				BTFixedPlayer opponent = new BTFixedPlayer();
				Move m = opponent.chooseMove(newBoard);
				
				return gen_move(board, myColor, myColor, depth+1);
			}
		}
		else {
			return bestMove;
		}
	}

	private int eval_board(BTBoard board)
	{
		return 0;
	}

} // End class
