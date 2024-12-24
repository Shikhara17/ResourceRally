package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState.Peasant;
import edu.cwru.sepia.agent.planner.GameState.Resource;
import edu.cwru.sepia.agent.planner.actions.StripsAction;

import java.util.HashMap;


/**
 * Harvest class represents an action in the STRIPS planning framework.
 * Represents a harvest action in a game which will be executed by a peasant
 */
public class Harvest implements StripsAction {

    private Peasant peasant;
    private Position resourcePosition; 
    private boolean isGold; //Flag to determine if the resource targeted by a Peasant is a goldMine or Tree
    private int resourceId;
    

     /**
     * Constructor to initialize a Harvest object which represents a harvesting action by a peasant.
     *
     * @param peasant, Peasant object that will perform the harvesting action.
     * @param id, Id of the resource to be harvested
     * @param position, Position object indicating where the resource to be harvested is located.
     * @param isGold, A boolean that is true if the resource to be harvested is gold, false if it is any other type.
     */
    public Harvest( Peasant peasant, int id,  Position position, boolean isGold){

        this.peasant = peasant;
        this.resourcePosition = position;
        this.isGold =isGold;
        this.resourceId = id;
    }


     /**
     * Checks if the preconditions for the Harvest action are met within the given game state.
     * The preconditions are met if the peasant is not currently carrying anything,
     * the resource at the specified position has a quantity of at least 100 units,
     * and the peasant is adjacent to the resource position.
     *
     * @param state The current game state to be checked against the preconditions.
     * @return true if all preconditions are met, false otherwise.
     */
    public boolean preconditionsMet(GameState state) {
        return !state.peasant.isCarrying &&
                state.getAllResources().get(resourceId).quantity>= 100 &&
                state.peasant.position.isAdjacent(resourcePosition);
    }


     /**
     * Applies the harvest action to the given game state, updating the cost, resources, and peasant state.
     * The type of resource harvested is determined by the 'isGold' flag, and it's either 'gold' or 'wood'.
     *
     * @param state The current game state where the harvest action has to be applied.
     * @return nextGameState The new game state after the harvest action has been applied, reflecting the updated cost,
     *                   resources, and peasant state.
     */
    public GameState apply(GameState state) {

        // Calculating the new cost after applying the harvest action, we assume that the cost of the harvest action is 100.
        double cost = state.getCost() + 100;

        // Determining the type of resource that can be harvested and the respective quantity available
        String resourceType = isGold ? "gold" : "wood";
        int goldHarvested = isGold ? 100 : 0;
        int woodHarvested = isGold ? 0 : 100;

        // Creating a new peasant instance with updated details of the quantity of the resources harvested by the peasant, 
        // and isCarrying field is set to true.
        Peasant newPeasant = new Peasant(peasant.id, peasant.position, true, goldHarvested, woodHarvested);
        newPeasant.currentResourceType=resourceType;

        // Retrieving and updating the resources in the game state to reflect the harvested amount
        HashMap<Integer, Resource> newResources = new HashMap<>(state.getAllResources());
        Resource resourceToDeplete = newResources.get(resourceId);
        resourceToDeplete.quantity =resourceToDeplete.quantity - 100;
        
        // Create a new game state with the changes after the harvest action
        GameState nextGameState = new GameState(state, cost,newPeasant,0,0,newResources, this);
        return nextGameState;
    }


    /**
     * Creating a primitive gather action for the current peasant. 
     * This method is invoked in the PEAgent class, when it tries to convert STRIPS action to a Sepia action.
     *
     * @return Action An instance of an Action object that contains the details of the gather command.
     */
    public Action createSepiaAction(){

        // Creating and returning a primitive gather action after determining the direction in which the action should be executed, 
    	// taking into account the current position of the peasant relative to the resource's position.
        return Action.createPrimitiveGather(peasant.id	,peasant.position.getDirection(resourcePosition));
    }


    /**
     * Converts this harvest action into a String format
     *
     * @return A String that represents the harvest action with resource type, peasant ID, and resource position.
     */
    public String toString() {

        // Determining the type of resource for the String representation
        String resource = isGold? "GOLD":"WOOD";

        // Constructing the action String with relevant details
        String action = "HARVEST"+ resource + " ( " + peasant.id + ", " +  resourcePosition.x + ", " + resourcePosition.y +" )";
        
        System.out.println(action);
        return action;
    }
}