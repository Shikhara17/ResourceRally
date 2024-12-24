package edu.cwru.sepia.agent.planner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState.Peasant;
import edu.cwru.sepia.agent.planner.actions.StripsAction;


/**
 * Represents the deposit action in a STRIPS-like planning system.
 * This action is responsible for depositing collected resources Wood/Gold at the town hall.
 */
public class Deposit implements StripsAction {
	private Position startPosition;
    //Represents the location of Townhall
    private Position townhallPosition;
    // number of peasants
    private int k;
    private int townhallId;
    private GameState state;
    private List<Integer> peasantsActing = null;
    /**
     * Creating a new Deposit action using this constructor.
     * This action is used when a peasant wants to deposit resources at the town hall.
     *
     * @param peasant The peasant that will perform the deposit action.
     * @param t The position of the town hall where the resources will be deposited.
     */
    public Deposit(Position startPosition, Position townhallPosition, int k, int id) {
        this.startPosition = startPosition;
        this.townhallPosition = townhallPosition;
        this.k = k;
        this.townhallId = id;

    }

    /**
     * Checks if the preconditions for the Deposit action are met in the given game state.
     *
     * @param state The current game state to be checked for the preconditions.
     * @return true if both preconditions are satisfied, false otherwise.
     */
    @Override
    public boolean preconditionsMet(GameState state){

        //Returns true if the preconditions are met, i.e., when the peasant is carrying resources and is adjacent to the town hall position.
    	this.state = state;

        ArrayList<Integer> peasantsThatAct = new ArrayList<Integer>();
        int peasantsMeetingPreconditions = 0;
        for (GameState.Peasant peasant : state.getPeasants().values()) {
            if((peasant.position.isAdjacent(startPosition) || peasant.position.equals(startPosition)) && peasant.isCarrying){
            	peasantsMeetingPreconditions++;
                peasantsThatAct.add(peasant.id);
                if(peasantsThatAct.size() == k){
                    this.peasantsActing = peasantsThatAct;
                    break;
                }
            }
        }
        return peasantsMeetingPreconditions >= k;
    }

    /**
     * Applies the deposit action to the current game state.
     * Resources carried by the peasant are deposited at the town hall, and the current resources at the townHall is updated.
     *
     * @param state The current game state before the deposit action.
     * @return A new game state reflecting the results of the deposit action.
     */
    @Override
    public GameState apply(GameState state) {
        
        // Initializing the variables to track the amount of resources to be deposited.
        int woodToBeDeposited = 0;
        int goldToBeDeposited = 0;
        int peasantsMeetingPreconditions = 0;
        HashMap<Integer, GameState.Peasant> newPeasants = state.getPeasants();
        
        //Iterating through all the peasants to create new peasants for the child state
        for (GameState.Peasant peasant : state.getPeasants().values()) {
            if((peasant.position.isAdjacent(startPosition) || peasant.position.equals(startPosition)) && peasant.isCarrying){
            	peasantsMeetingPreconditions++;

                // Creating a new peasant instance with the current ID and position. 
                // As peasant doesn't carry any resources after depositing. So, updating them as 0,0 and isCarrying is set to false.
                GameState.Peasant newPeasant = new Peasant(peasant.id, townhallPosition, false, 0, 0);
                newPeasant.currentResourceType = null;
                newPeasants.put(peasant.id, newPeasant);

                if(peasantsMeetingPreconditions == k){
                	// Retrieving the type of resource which is currently being carried by the peasant.
                    String resourceType = peasant.currentResourceType;
                    
                    // Checking if the peasant is carrying wood or gold, and respectively setting their values to 100.
                    if (resourceType.equals("wood")) {
                        woodToBeDeposited = k*100;
                    } else if (resourceType.equals("gold")) {
                        goldToBeDeposited = k*100;
                    }
                    
                    // Creating a new game where the peasant is updated to no longer be carrying resources,
                    // and the cost of the game state is updated with the amount of resources deposited.
                    GameState nextGameState = new GameState(state, state.getCost(), newPeasants, goldToBeDeposited, woodToBeDeposited, state.getAllResources(), this);
                    return nextGameState;
                }

            }
        }
        return null;
    }

    /**
     * Creates a primitive deposit action for the peasant to deposit the resources.
     * This method is invoked in the PEAgent class, when it tries to convert STRIPS action to a Sepia action.
     *
     * @return A primitive Action object which is configured to perform the deposit action.
     */
    public Action createSepiaAction(int id){

        // Creating a primitive deposit action using the peasant's ID and the direction
        // from the peasant's current position to the town hall position.
    	return Action.createCompoundDeposit(id, townhallId);
    }

    /**
     * Converts the deposit action into a string representation.
     *
     * @return A string that represents the deposit action with the peasant's ID and the town hall's position.
     */
    public String toString() {
    	String action = "MOVE"+ k + townhallPosition + " \nDEPOSIT"+ k + startPosition;;
        System.out.println(action);
        return action;
    }

	@Override
	public List<Integer> getPeasantsActing(){
        return peasantsActing;
    }

    public List<Integer> setPeasantsActing(Integer i){
        List<Integer> peasantsThatAct = getPeasantsActing();
        peasantsThatAct.remove(new Integer(i));
        return peasantsThatAct;
    }
}