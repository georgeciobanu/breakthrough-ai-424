package breakthrough;

import java.util.AbstractList;
import java.util.Calendar;
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
 * A random BreakThrough player.
 */
public class UCTPlayer extends Player {

	final int UNASSIGNED_MOVE = -999;

	final float WIN = 1, LOSE = -1;

	public int myColor = -1, opColor = -1, forward = -10;

	private Random rand = new Random();
	private BTNeuralNetPlayer netPlayer = new BTNeuralNetPlayer(); 

	/** Provide a default public constructor */
	public UCTPlayer() {
		super("icioba1");
		visitedTable = new Hashtable<Integer, MyNode>();
	}

	public void gameStarted(String message) {
		myColor = this.getColor();
		forward = (myColor == EnhancedBTBoard.WHITE ? 1 : -1);
		opColor = (myColor == EnhancedBTBoard.WHITE) ? EnhancedBTBoard.BLACK
				: EnhancedBTBoard.WHITE;
	}

	Hashtable<Integer, MyNode> visitedTable = null;

	BTFixedPlayer otherPlayer = new BTFixedPlayer();

	
	public Move chooseMove(Board theboard) {
		visitedTable.clear();
		// Cast the arguments to the objects we want to work with
		BTBoard btBoard = (BTBoard) theboard;

		EnhancedBTBoard board = new EnhancedBTBoard(btBoard);

		MyNode root = new MyNode(board);

		BTMove best = null;
		int furthest = 9999;
		
		root.createChildren();
		
		for (int i = 0; i <root.getChildCount(); i++){
			int destColor = root.getBoard().getPieceAt( ((MyNode)root.getChildAt(i)).getMove().dest);
			int destPos = ((MyNode)root.getChildAt(i)).getMove().dest;
			
			if (destColor == opColor){ //If it is a capture
				if (board.getRow(destPos) < furthest){ // 
					furthest = board.getRow(destPos);
					best = ((MyNode)root.getChildAt(i)).getMove();
				}
			}			
			
			
			if (best != null)
				if (Math.random() > 0.1)
					return best;			
		}
		
		long simulations = 0;
		//root.setVisits(simulations);
		simulations = UCTSimulateNMilis(root, 4500);
		//System.out.println("Simulations: " + simulations);	
		
		return UCTSelect(root).getMove();
	}

	private long UCTSimulateNMilis(MyNode node, long milis) {
		Calendar cal = Calendar.getInstance();
		long start = cal.getTimeInMillis();
		long sims = 0;
		while (true){			
			sims++;
			node.setVisits(sims);
			UCTSearch(node);
			
			if (Calendar.getInstance().getTimeInMillis() - start >= milis)
				break;
		}
		return sims;
	}
	

	private void UCTSearch(MyNode root) {

		MyNode node = root, selectedChild = null;

		while (visitedTable.containsKey(node.hashCode())) {
			int winner = node.getBoard().getWinner();

			if (winner != EnhancedBTBoard.NOBODY) { // Node is leaf				
				break;
			
			} else {
				if (node.getChildCount() == 0) {
					node.createChildren();
				}
				selectedChild = UCTSelect(node);
				node = selectedChild;

			} // if node is leaf
		}// end while

		visitedTable.put(node.hashCode(), node);
		int outcome = RandomSimulation(node);

		while (node.getParent() != null) {
			node.addVisit();
			if (outcome == 1)
				if (node.getMove().player == myColor)
					node.addWin(); // If WON

			outcome = 1 - outcome;
			node = (MyNode) node.getParent();
		}
	}

	private MyNode UCTSelect(MyNode root) {
		MyNode node, choice = null;
		double maxUCB = Double.NEGATIVE_INFINITY, minUCB = Double.POSITIVE_INFINITY;

		
			for (int i = 0; i < root.getChildCount(); i++) {
				node = (MyNode) root.getChildAt(i);
				// TODO: this should return a random node, or make a smarter
				// choice
				// be it local or global
				if (node.firstVisit())
					return node;

				double currentUCB = node.getUCBValue();
				if (currentUCB > maxUCB) {
					choice = node;
					maxUCB = currentUCB;
				}
			}
			return choice;
	}

	private int RandomSimulation(MyNode node) {
		EnhancedBTBoard board = new EnhancedBTBoard(node.getBoard());

		int turn = myColor;
		int winner = board.getWinner();

		while (board.getWinner() == EnhancedBTBoard.NOBODY) {
			// generate moves
						
			board.move(netPlayer.chooseMove(board));

			winner = board.getWinner();

			if (winner != EnhancedBTBoard.NOBODY) {
				break;
			}
		}

		if (winner == myColor)
			return 1;
		else
			return 0;
	}

} // End class
