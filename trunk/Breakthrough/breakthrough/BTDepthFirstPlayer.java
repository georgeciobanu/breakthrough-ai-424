package breakthrough;

import java.util.Hashtable;
import java.util.Random;

import com.sun.org.apache.xpath.internal.compiler.OpCodes;

import boardgame.Board;
import boardgame.Move;
import boardgame.Player;

import breakthrough.BTFixedPlayer;

/**
 *A random BreakThrough player.
 */
public class BTDepthFirstPlayer extends Player {

	final int UNASSIGNED_MOVE = -999;
	final float WIN = 1, LOSE = -1;
	
	
	/** Provide a default public constructor */
	public BTDepthFirstPlayer() { 
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
		
		// Cast the arguments to the objects we want to work with    	
		BTBoard board = (BTBoard) theboard;				

		// Find all my pieces
		//TODO:This can be easily optimized to be saved on the board
		
		
		BTMove nextMove = new BTMove(UNASSIGNED_MOVE, -1, -1);
		gen_move(board, myColor, myColor, 0, nextMove);

		return nextMove;
	}

	private float gen_move(BTBoard board, int myColor, int moveColor, int depth, BTMove bestMove)
	{
		int forward = (myColor == BTBoard.WHITE ? 1 : -1);
		int opColor = (myColor == BTBoard.WHITE) ? BTBoard.BLACK : BTBoard.WHITE;

		int[][] myPieces = getPieces(board, myColor);
		Hashtable<String,String> mine = new Hashtable<String, String>();
				
		float val = -999;
		float maxVal = -999;
		
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
														
							if (val == WIN){
									if (depth == 0){								
										bestMove.fromString(m.toPrettyString());
									}
								}
							else if (val == LOSE){
									if (bestMove.player == UNASSIGNED_MOVE && depth == 0)
										bestMove.fromString( m.toString() );																	
							}else{
								
									val = gen_move(newBoard, myColor, opColor, depth+1, bestMove);
									
									if ((val > maxVal || bestMove.player == UNASSIGNED_MOVE) && depth == 0)									
										bestMove.fromString( m.toTransportable() );																	
								}																																			
						}						
					}
					if (val == WIN) //No need to look for other moves if we found a winning position
						break;
				}
				if (bestMove.toPrettyString().equals("WHITE F1 -> E2") && depth == 0 && !board.isLegal(bestMove) )
					System.out.println("Warning");
				return val;
			}
			else
			{
				val = eval_board(board); 
				if ( val == WIN || val == LOSE)
					return val;
				
				//play the opponent
				BTBoard newBoard = new BTBoard(board);												
				BTFixedPlayer opponent = new BTFixedPlayer();
				Move m = null;
				try{
					m = opponent.chooseMove(newBoard);
				} catch (IllegalArgumentException e){
					//something went really wrong
					e.printStackTrace();
					return 1; // Is this a good idea?
				}				
				newBoard.move(m);				
				return gen_move(newBoard, myColor, myColor, depth+1, bestMove);
			}
		}
		else {
			return eval_board(board);
		}
	}

	private int eval_board(BTBoard board)
	{
		//if winning return 1
		//if losing return -1
		//otherwise evaluate board and return the value
		return 0;
	}

} // End class
