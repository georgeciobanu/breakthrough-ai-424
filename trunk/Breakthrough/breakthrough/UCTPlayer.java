package breakthrough;


import java.util.Calendar;
import java.util.Hashtable;

import boardgame.Board;
import boardgame.Move;
import boardgame.Player;

import breakthrough.BTFixedPlayer;

/**
 * A UCT BreakThrough player. It uses the UCT algorithm and the neural net player to generate
 * random moves, for the Monte Carlo simulations.
 */
public class UCTPlayer extends Player {

	final int UNASSIGNED_MOVE = -999;
	final float WIN = 1, LOSE = -1;

	public int myColor = -1, opColor = -1, forward = -10;	
	private BTNeuralNetPlayer netPlayer = new BTNeuralNetPlayer(); 

	/** Custom public constructor */
	public UCTPlayer() {
		super("icioba1");		
		visitedTable = new Hashtable<Integer, MyNode>();
	}

	/**
	 * Initialize some stuff to not have to do it all the time.
	 */
	
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
		
		//For some reason, UCT does not like to capture. So
		//we have to patch it in that sense.
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
				//Probability 90% that it will capture, if possible
				if (Math.random() > 0.1)
					return best;			
		}
						
		//If not capture happened or was possible, run UCT
		UCTSimulateNMilis(root, 4900);					
		return UCTSelect(root).getMove();
	}

	/**
	 * Run UCT for a certain number of miliseconds
	 * @param node Root of tree
	 * @param milis Milliseconds to run for
	 * 
	 */
	private void UCTSimulateNMilis(MyNode node, long milis) {
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
		node.setVisits(sims);		
	}
	
	/**
	 * Builds and traverses a UCT tree
	 * @param root Root of tree
	 */
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
		//New node encountered
		visitedTable.put(node.hashCode(), node);
		//Run the simulation
		int outcome = RandomSimulation(node);
		//"Backpropagate"
		while (node.getParent() != null) {
			node.addVisit();
			if (outcome == 1)
				if (node.getMove().player == myColor)
					node.addWin(); // If WON

			outcome = 1 - outcome;
			node = (MyNode) node.getParent();
		}
	}

	/**
	 * Selects the node with the best return
	 * @param root Root of tree 
	 * @return Best move
	 */
	private MyNode UCTSelect(MyNode root) {
		MyNode node, choice = null;
		double maxUCB = Double.NEGATIVE_INFINITY, minUCB = Double.POSITIVE_INFINITY;
		
			for (int i = 0; i < root.getChildCount(); i++) {
				node = (MyNode) root.getChildAt(i);
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

	/**
	 * Generate a random simulation
	 * @param node Node to run it from
	 * @return Outcome: 0 = lose, 1 = win
	 */
	private int RandomSimulation(MyNode node) {
		EnhancedBTBoard board = new EnhancedBTBoard(node.getBoard());
		
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
