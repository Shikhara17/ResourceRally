package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState.Peasant;
import edu.cwru.sepia.agent.planner.actions.StripsAction;

/**
 * MoveTo class represents an action in the STRIPS planning framework.
 * Represents a MoveTo action in a game which will be executed by a peasant.
 */
public class MoveTo implements StripsAction {

	private Position position;
	private GameState.Peasant peasant;

    // Initializing minimum distance with a very high value
    double minimumDistance = Double.POSITIVE_INFINITY;


    /**
	 * Initializes a MoveTo action for a peasant to move to a specified position.
	 * The target position is chosen from the adjacent positions of the destination
	 * that is nearest to the current peasant's position.
	 *
	 * @param position The target position which has the resources
	 * @param pe The peasant that will move.
	 */
	public MoveTo(Position position, Peasant peasant) {
		this.position=position;
		this.peasant=peasant;
		double tempDist = 0;

        // Iterating over the adjacent positions of the desired resource/town hall location.
		for (Position adj : this.position.getAdjacentPositions()) {
			
            // Calculating the Euclidean distance from the peasant's current position to the adjacent position of the resource
            tempDist = peasant.position.euclideanDistance(adj);
			
            // Updating the minimum distance and position if a closer adjacent position is found
	        if (minimumDistance > tempDist) {
				minimumDistance = tempDist;
				this.position = adj;
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
		return !state.isGoal() && (!(state.peasant.position.x==position.x && state.peasant.position.y==position.y));
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

        // Create a new Peasant instance with the updated position
	    // while retaining all other properties of the peasant such as ID and resources being carried.
		Peasant newPeasant = new Peasant(peasant.id,position, peasant.isCarrying, peasant.goldCarrying,peasant.woodCarrying);
		
        // Transfer the current resource type being carried by the peasant to the new peasant instance.
        newPeasant.currentResourceType = peasant.currentResourceType;

        // Calculate the cost of moving to the new position. This is done by adding the Euclidean distance
	    // from the peasant's current position to the new position to the current total cost.
		double cost= state.getCost() + newPeasant.position.euclideanDistance(state.peasant.position);

        // Create a new game state that reflects the changes after applying this action.
	    // This includes the updated peasant position and the new total cost.
	    // The rest of the game state remains unchanged (resources and action history).
		GameState nextGameState = new GameState(state, cost,  newPeasant,0,0, state.getAllResources(), this);
		
        // Set the parent state to the current state to maintain the state transition history. 
        return nextGameState;
	}
    
    /**
	 * Creating a compound move action for the peasant.
	 * This method is invoked in the PEAgent class, when it tries to convert STRIPS action to a Sepia action.
	 * 
	 * @return A compound move action containing the movement of the peasant.
	 */
	public Action createSepiaAction(){

        //This action is responsible for moving the peasant to a specific position on the game map.
		return Action.createCompoundMove(peasant.id, position.x, position.y);
	}


    /**
	 * Provides a string representation of the MoveTo action
	 *
	 * @return A string that represents the 'MoveTo' action with the peasant's ID and target position coordinates.
	 */
	public String toString() {

        // Constructing the string that represents the move action with the peasant's ID and the target coordinates.
		String action = "MOVE ( " + peasant.id + ", " +  position.x + ", " + position.y +" )";
        
		System.out.println(action);
		return action;
	}
}