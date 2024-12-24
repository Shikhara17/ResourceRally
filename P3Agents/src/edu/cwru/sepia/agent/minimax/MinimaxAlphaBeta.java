package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.*;


public class MinimaxAlphaBeta extends Agent {

    private final int numPlys;
    
    public class StateUtilityPair{
    	
    	private GameStateChild childState;
    	private Double utility;
    	
    	public StateUtilityPair(GameStateChild childState, Double utility) {
    		this.childState=childState;
    		this.utility=utility;
    	}

    }

    public MinimaxAlphaBeta(int playernum, String[] args)
    {
        super(playernum);

        if(args.length < 1)
        {
            System.err.println("You must specify the number of plys");
            System.exit(1);
        }

        numPlys = Integer.parseInt(args[0]);
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView newstate, History.HistoryView statehistory) {
        return middleStep(newstate, statehistory);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView newstate, History.HistoryView statehistory) {
        GameStateChild bestChild = alphaBetaSearch(new GameStateChild(newstate),
                numPlys,
                Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY);

        return bestChild.action;
    }

    @Override
    public void terminalStep(State.StateView newstate, History.HistoryView statehistory) {

    }

    @Override
    public void savePlayerData(OutputStream os) {

    }

    @Override
    public void loadPlayerData(InputStream is) {

    }

    /**
     * You will implement this.
     *
     * This is the main entry point to the alpha beta search. Refer to the slides, assignment description
     * and book for more information.
     *
     * Try to keep the logic in this function as abstract as possible (i.e. move as much SEPIA specific
     * code into other functions and methods)
     *
     * @param node The action and state to search from
     * @param depth The remaining number of plys under this node
     * @param alpha The current best value for the maximizing node from this node to the root
     * @param beta The current best value for the minimizing node from this node to the root
     * @return The best child of this node with updated values
     */
    
    //Main method, alpha-beta search initiated, searching upto a certain depth and alpha-beta pruning is used 
    public GameStateChild alphaBetaSearch(GameStateChild node, int depth, double alpha, double beta) {
    	//The MaxValue function, which returns the child with the highest utility.
        return MaxValue(node, depth, alpha, beta, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY).childState;
    }
    
    
 // Method to find the maximum value from the current state. It is used to simulate the footmans gameplay.
    public StateUtilityPair MaxValue(GameStateChild node, int depth, double alpha, double beta, double currentMax, double currentMin) {
    	//if depth is 0, the search has reached its maximum depth and returns the utility value of the node.
    	if (depth == 0) {
        	return new StateUtilityPair(node, node.state.getUtility());
        }      
    	//Maximum utility value stored
        StateUtilityPair max = new StateUtilityPair(null, Double.NEGATIVE_INFINITY);
        
        //Loop each child of the node.
        for (GameStateChild child : orderChildrenWithHeuristics(node.state.getChildren())) {
            StateUtilityPair temp = MinValue(child, depth - 1, alpha, beta, max.utility, currentMin);
            
         // Update max if the value returned from MinValue is greater.
            if (temp.utility > max.utility) {
                max = new StateUtilityPair(child, temp.utility);
            }

         // Alpha-beta pruning,if max is greater or equal to beta, prune the remaining child nodes.
            if (max.utility >= beta) {
                return max;
            }

            alpha = Math.max(alpha, max.utility);
        }
        return max; //Return the highest utility value.
    }

 // Method to find the minimum value from the current state. It is used when it's the AI's/archer turn to play.
    public StateUtilityPair MinValue(GameStateChild node, int depth, double alpha, double beta, double currentMax, double currentMin) {
       
    	// if depth is 0, the search has reached its maximum depth and returns the utility value of the node.
    	if (depth == 0) {
    		return new StateUtilityPair(node, node.state.getUtility());
    	}
            
    	//Minimum utility value stored
        StateUtilityPair min = new StateUtilityPair(null, Double.POSITIVE_INFINITY);
        
      //Loop each child of the node.
        for (GameStateChild child : orderChildrenWithHeuristics(node.state.getChildren())) {
            StateUtilityPair temp = MaxValue(child, depth - 1, alpha, beta, currentMax, min.utility);
            
         // Update min if the value returned from MaxValue is lesser.
            if (temp.utility < min.utility) {
                min = new StateUtilityPair(child, temp.utility);
            }

         // Alpha-beta pruning,if min is lesser or equal to alpha, prune the remaining child nodes.
            if (min.utility <= alpha) {
                return min;
            }

            beta = Math.min(min.utility, beta);
        }
        return min; // Return the lowest utility value.
    } 

    


    /**
     * You will implement this.
     *
     * Given a list of children you will order them according to heuristics you make up.
     * See the assignment description for suggestions on heuristics to use when sorting.
     * Sort base on utility.
     * Use this function inside of your alphaBetaSearch method.
     *
     * Include a good comment about what your heuristics are and why you chose them.
     *
     * @param children
     * @return The list of children sorted by your heuristic.
     */
    
    // This method orders game state nodes based on their utility value
    // Sorting the children will improve the efficiency of alpha-beta pruning
    public List<GameStateChild> orderChildrenWithHeuristics(List<GameStateChild> children) {
    	
    	//Sort the children list based on the utility of the states.
    	children.sort((child1, child2) -> {
            // Calculate the difference in utility between two children.
            double utilityDifference = child2.state.getUtility() - child1.state.getUtility();
            
            if (utilityDifference > 0) return 1; // child2 is less than child1
            else if (utilityDifference < 0) return -1; // child1 is less than child2
            else return 0; //If the utilities are equal, maintain the current order
        });
    	
    	// Return the sorted list of children
        return children;
    }
    
}


