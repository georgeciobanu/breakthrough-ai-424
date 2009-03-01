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

	final int UNASSIGNED_MOVE = -999;
	
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
		
		// Cast the arguments to the objects we want to work with    	
		BTBoard board = (BTBoard) theboard;				

		// Find all my pieces
		//TODO:This can be easily optimized to be saved on the board
		
		
		BTMove nextMove = new BTMove(UNASSIGNED_MOVE, -1, -1);
		gen_move(board, myColor, myColor, 0, nextMove);

		return nextMove;
	}

	private int gen_move(BTBoard board, int myColor, int moveColor, int depth, BTMove bestMove)
	{
		int forward = (myColor == BTBoard.WHITE ? 1 : -1);
		int opColor = (myColor == BTBoard.WHITE) ? BTBoard.BLACK : BTBoard.WHITE;

		int[][] myPieces = getPieces(board, myColor);
				
		int val = -999;
		int maxVal = -999;
		
		if (depth < 7){
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
								case 1: {
									if (depth == 0){								
										bestMove.fromString(m.toPrettyString());
										if (m.toPrettyString().equals("WHITE F1 -> E2"))
											System.out.println("Serious Warning");
									}
								};break;
								case -1:{
									if (bestMove.player == UNASSIGNED_MOVE && depth == 0)
										bestMove.fromString( m.toString() );
									if (m.toPrettyString().equals("WHITE F1 -> E2"))
										System.out.println("Serious Warning");
								}; break; 
								default: {
									val = gen_move(newBoard, myColor, opColor, depth+1, bestMove);
									
									if ((val > maxVal || bestMove.player == UNASSIGNED_MOVE) && depth == 0)
										if (m.toPrettyString().equals("WHITE F1 -> E2"))
											System.out.println("Serious Warning");
										bestMove.fromString( m.toTransportable() );																	
								}
							}																												
						}						
					}
					if (val == 1) //No need to look for other moves if we found a winning position
						break;
				}
				if (bestMove.toPrettyString().equals("WHITE F1 -> E2") && depth == 0 && !board.isLegal(bestMove) )
					System.out.println("Warning");
				return val;
			}
			else
			{
				val = eval_board(board); 
				if ( val == 1 || val == -1)
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
		return 0;
	}

} // End class
