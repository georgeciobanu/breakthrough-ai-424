package breakthrough;

import java.util.AbstractList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import com.sun.org.apache.xpath.internal.compiler.OpCodes;

import boardgame.Board;
import boardgame.Move;
import boardgame.Player;

import breakthrough.BTFixedPlayer;

/**
 *A random BreakThrough player.
 */
public class UCTPlayer extends Player {

	final int UNASSIGNED_MOVE = -999;
	final float WIN = 1, LOSE = -1;
	public int myColor = -1, opColor = -1, forward = -10;
	private Random rand = new Random();
	
	
	/** Provide a default public constructor */
	public UCTPlayer() { 
		super("icioba1");
		visitedTable = new Hashtable<Integer, MyNode>();		
	}
	
	public void gameStarted(String message)
	{
		myColor = this.getColor();
		forward = (myColor == EnhancedBTBoard.WHITE ? 1 : -1);
		opColor = (myColor == EnhancedBTBoard.WHITE) ? EnhancedBTBoard.BLACK : EnhancedBTBoard.WHITE;
	}
	
	Hashtable<Integer, MyNode> visitedTable = null;	

	BTFixedPlayer otherPlayer = new BTFixedPlayer();
	
	
	/** Implement a very stupid way of picking moves */
	public Move chooseMove(Board theboard) {		
		
		// Cast the arguments to the objects we want to work with    	
		BTBoard btBoard = (BTBoard) theboard;
		
		EnhancedBTBoard board = new EnhancedBTBoard(btBoard); 

		MyNode root = new MyNode(board);				
		
		long simulations = 600;
		root.setVisits(simulations);
		UCTSimulateNTimes(root, simulations);
			
		
		return UCTSelect(root).getMove();				
	}
	
	private void UCTSimulateNTimes(MyNode node, long count){
		for (long i = 0; i < count; i++){
			UCTSearch(node);
		}
	}
	
	private void UCTSearch(MyNode root){
				
		MyNode node = root, selectedChild = null;
		try{
		while(visitedTable.containsKey(node.hashCode())){
			int winner = node.getBoard().getWinner(); 
			
			if (winner != EnhancedBTBoard.NOBODY){ //Node is leaf
				if (myColor == winner){
					node.addWin();					
					return;					
				}
		    }
			else {
				if (node.getChildCount() == 0){
					node.createChildren();
				}
				selectedChild = UCTSelect(node);
				node = selectedChild;
				
			} //if node is leaf
		}//end while
		} catch(Exception e){
			e.printStackTrace();
		}
		
		visitedTable.put(node.hashCode(), node);
		int outcome = RandomSimulation(node);
		
		while (node.getParent() != null){
			node.addVisit();
			if (outcome == 1)
				node.addWin(); //If WON
			
			outcome = 1-outcome;
			node = (MyNode) node.getParent();
		}			
	}

	private MyNode UCTSelect(MyNode root) {
		MyNode node, choice = null;
		double maxUCB = -1;
		
		for (int i = 0; i < root.getChildCount(); i++) {
			node = (MyNode)root.getChildAt(i);
			//TODO: this should return a random node, or make a smarter choice
			//be it local or global
			if (node.firstVisit())
				return node;
			
			double currentUCB = node.getUCBValue(); 
			if (currentUCB > maxUCB){
				choice = node;			
				maxUCB = currentUCB;
			}
		}
		return choice;
	}

	private int RandomSimulation(MyNode node) {
		EnhancedBTBoard board = new EnhancedBTBoard( node.getBoard());
		
		int turn = myColor;
		int winner = board.getWinner();
		
		while (board.getWinner() == EnhancedBTBoard.NOBODY){
			//generate moves
		 	Vector<BTMove> moves = board.GenerateMoves();
		 	//chose randomly one of them
		 	int selectedMove = rand.nextInt(moves.size());
		 	board.move(moves.get(selectedMove));
		 	
		 	winner = board.getWinner();
		 	
		 	if (winner != EnhancedBTBoard.NOBODY){
		 		break;
		 	}		 			 			 
		}	
		
 		if (winner == myColor)
 			return 1;
 		else return 0;
	}
	


} // End class
