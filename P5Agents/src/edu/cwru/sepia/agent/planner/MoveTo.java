package edu.cwru.sepia.agent.planner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.cwru.sepia.action.Action;

import edu.cwru.sepia.agent.planner.GameState.Peasant;
import edu.cwru.sepia.agent.planner.GameState.Resource;
import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.state.State;

/**
 * MoveTo class represents an action in the STRIPS planning framework.
 * Represents a MoveTo action in a game which will be executed by a peasant.
 */
public class MoveTo implements StripsAction {

    // Initializing minimum distance with a very high value
	double[] minimumDistance = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
	//determines location to be moved to
    private Position resourcePos;
    // number of peasants
    private int k;

    private GameState state;
	//list of all peasants acting in this state
    private List<Integer> peasantsActing = null;
	//list of destinations of each peasant
    private Position[] destinations = null;
    private Peasant[] peasants = null;
    private int precon=0;



    /**
	 * Initializes a MoveTo action for a peasant to move to a specified position.
	 * The target position is chosen from the adjacent positions of the destination
	 * that is nearest to the current peasant's position.
	 *
	 * @param position The target position which has the resources
	 * @param pe The peasant that will move.
	 */
	public MoveTo(Position resourcePos, HashMap<Integer, Peasant> peasants, int k) {
		this.peasants= new Peasant[peasants.values().size()];
		
        this.resourcePos = resourcePos;
        for(int i=1; i<=peasants.values().size(); i++) {
        	
        	
        	this.peasants[i-1] = peasants.get(i);	
        	
        	
        }
        this.k = k;
        double tempDist = 0;
        
        destinations = new Position[peasants.values().size()];
        for(int i=0; i<k; i++) {
    		for (Position adj : this.resourcePos.getAdjacentPositions()) {
    			for(int j=i; j>=0; j--) {
    				if(!adj.equals(destinations[j])) {
    					 tempDist = this.peasants[i].position.euclideanDistance(adj);
    						
    			            // Updating the minimum distance and position if a closer adjacent position is found
    				        if (minimumDistance[i] > tempDist) {
    							minimumDistance[i] = tempDist;
    							destinations[i]= adj;
    							
    							
    						}
    					
    				}
    			}
    		}
    	}
	}


    /**
	 * Checks if the preconditions for the MoveTo action are met in the current game state.
	 *
	 * @param state The current state of the game.
	 * @return true if the preconditions are met, false otherwise.
	 */
	@Override
	public boolean preconditionsMet(GameState state) {

        // Returns true if the game's goal has not been reached and the peasant is not already at the target position.
//		return !state.isGoal() && (!(state.peasant.position.x==position.x && state.peasant.position.y==position.y));
		  this.state = state;
		  int num=0;
		  int peasantsMeetingPreconditions = 0;
	        ArrayList<Integer> peasantsThatAct = new ArrayList<Integer>();
	        for (GameState.Peasant peasant : state.getPeasants().values()) {
	        	
	        	
	            if(!state.isGoal() && !(peasant.position.equals(destinations[num]) && !peasant.position.isAdjacent(resourcePos))){
	            	peasantsMeetingPreconditions++;
	                peasantsThatAct.add(peasant.id);
	                if(peasantsThatAct.size() == k){
	                    this.peasantsActing = peasantsThatAct;
	                    break;
	                }
	            }
	            num++;
	        }

	        System.out.println("deposit preconditions met or not: " + (peasantsMeetingPreconditions >=k));
	        precon=peasantsMeetingPreconditions;
	        
	        return peasantsMeetingPreconditions >= k;
	}
    
    /**
	 * Applies the MoveTo action to the current game state to produce a new game state.
	 * This involves creating a new peasant with the updated position and recalculating the cost
	 * based on the distance moved. The resulting state is a successor state after the action has been carried out.
	 *
	 * @param state The current game state on which the action will be applied.
	 * @return A new game state that results from applying the MoveTo action.
	 */
	@Override
	public GameState apply(GameState state) {
		
        HashMap<Integer, GameState.Resource> newResources = new HashMap<>(state.getResources());
        HashMap<Integer, GameState.Peasant> newPeasants = state.getPeasants();
        double maxDist = Math.min(Math.min(minimumDistance[0], minimumDistance[1]), minimumDistance[2]);
        int cost = (int) (state.getCost() + maxDist);
        int num=0;
        for (Integer i : peasantsActing) {
                GameState.Peasant newPeasant = new GameState.Peasant(peasants[i-1].id,destinations[num], peasants[i-1].isCarrying, peasants[i-1].goldCarrying, peasants[i-1].woodCarrying );
                
                newPeasant.currentResourceType=peasants[i-1].currentResourceType;
                if(newPeasant.isCarrying) {
                	System.out.println(newPeasant.currentResourceType + newPeasant.goldCarrying + newPeasant.woodCarrying + newPeasant.isCarrying);
                }
                if(newPeasant.currentResourceType==null) {
                	newPeasant.goldCarrying=0;
                	newPeasant.woodCarrying=0;
                	newPeasant.isCarrying = false;
                }

                newPeasants.put(peasants[i-1].id, newPeasant);


                if (precon == k) {                    
                    GameState nextGameState = new GameState(state, cost, newPeasants,0,0, state.getAllResources(), this);
                    nextGameState.parent = state;
                    return nextGameState;
                }
                num++;
            
        }
        return null;

        
	}
    
    /**
	 * Creating a compound move action for the peasant.
	 * This method is invoked in the PEAgent class, when it tries to convert STRIPS action to a Sepia action.
	 * 
	 * @return A compound move action containing the movement of the peasant.
	 */
	public Action createSepiaAction(int id ){
		
		
        //This action is responsible for moving the peasant to a specific position on the game map.
		return Action.createCompoundMove(id, resourcePos.x, resourcePos.y);
	}


    /**
	 * Provides a string representation of the MoveTo action
	 *
	 * @return A string that represents the 'MoveTo' action with the peasant's ID and target position coordinates.
	 */
	public String toString() {

        // Constructing the string that represents the move action with the peasant's ID and the target coordinates.
		String action = "MOVE" + k;
        

		return action;
	}


	@Override
	 public List<Integer> getPeasantsActing(){
        return peasantsActing;
    }

    public List<Integer> setPeasantsActing(Integer i){
        List<Integer> peasantsThatAct = getPeasantsActing();
        peasantsThatAct.remove(new Integer(i));
        System.out.println("peasantsThatAct" + peasantsThatAct);
        peasantsActing=peasantsThatAct;
        return peasantsThatAct;
    }


	

}