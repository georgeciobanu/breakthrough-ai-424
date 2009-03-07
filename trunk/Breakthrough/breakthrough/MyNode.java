package breakthrough;

import java.security.InvalidParameterException;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

public class MyNode extends DefaultMutableTreeNode {

	private long visits = 0, wins = 0;
	public final static double UCBConstant = 1;
	private BTMove move = null;
		
	
	public MyNode (EnhancedBTBoard board){
		if (board != null)
			this.setUserObject(board);
		else throw new NullPointerException("Please pass an existing board.");		
	}
	
	public EnhancedBTBoard getBoard()
	{
		return (EnhancedBTBoard) this.userObject;
	}

	public void createChildren() {
		//go over all possible moves (!) in a specific order
		//and for each move generate a board, create a node around it and
		//add it to the list of children of this node		
		Vector<BTMove> moves = this.getBoard().GenerateMoves();
		
		for (BTMove move : moves) {
			EnhancedBTBoard board = new EnhancedBTBoard(this.getBoard());
			board.move(move);			
			MyNode node = new MyNode(board);
			node.setMove(move);
			this.add(node);			
		}
	}

	public void addVisit() {
		visits++;
		
	}

	public void addWin() {
		wins++;
		
	}
	
	public double getUCBValue(){
		double TotalVisits = ((MyNode)getParent()).getVisits();
		if (visits == 0)
			return -1;
		return wins/TotalVisits + UCBConstant * Math.sqrt( 2*Math.log(TotalVisits) / visits);
	}
	
	public boolean firstVisit(){
		return visits < 1;
	}
	public long getVisits()
	{
		return visits;
	}

	public BTMove getMove() {
		return move;
	}

	public void setMove(BTMove move) {
		this.move = move;
	}
	
	public void setVisits(long visits){
		this.visits = visits;
	}
	

}
