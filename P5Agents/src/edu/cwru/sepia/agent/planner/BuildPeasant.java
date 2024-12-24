package edu.cwru.sepia.agent.planner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.actions.StripsAction;

public class BuildPeasant implements StripsAction {
	
	//used to store list of peasants that act in this STRIPSAction
	private List<Integer> peasantsActing = new ArrayList<>();
	
	//constructor to initialize buildPeasantAction
    public BuildPeasant(){
        ArrayList<Integer> peasantsThatAct = new ArrayList<Integer>();
        //townhall would the the acting peasant as the action is performed by it
        peasantsThatAct.add(GameState.townhallStateView.getID());
        this.peasantsActing = peasantsThatAct;
    }
    /**
     * Checks if the preconditions for the BuildPeasant action are met within the given game state.
     * The preconditions are met if current state's townhall has atleast 1 food and 400 gold
     *
     * @param state The current game state to be checked against the preconditions.
     * @return true if all preconditions are met, false otherwise.
     */
	@Override
	public boolean preconditionsMet(GameState state) {
		
		//precondition is satisfied if atleast one food is available and 400 gold is available
		return state.foodAvailable > 0 &&
                state.currentGold >= 400;
	}

	/**
     * Applies the BuildPeasant action to the current game state.
     * New Peasant is created at the townhall 
     *
     * @param state The current game state before the BuildPeasant action.
     * @return A new game state reflecting the results of the BuildPeasant action.
     */
	@Override
	public GameState apply(GameState state) {
		// TODO Auto-generated method stub
		
		//get the all the peasants from current state
        HashMap<Integer, GameState.Peasant> newPeasants = state.getPeasants();
        //Create a new Peasant near the townhall to add it to the list of peasants
        
        GameState.Peasant newPeasant = new GameState.Peasant(state.numberOfPeasants + 1,
        													new Position(GameState.townhallStateView.getXPosition(),
        																GameState.townhallStateView.getYPosition()),
        													false, 0,0);

        newPeasant.currentResourceType = null;

        //adding newly created peasant to list of peasants
        newPeasants.put(state.numberOfPeasants + 1, newPeasant);
        
        double cost = state.cost;
        
        // Creating a new game where the newly built peasant is added and number of peasants for that state is increased by 1
       
		GameState nextGameState = new GameState(state, cost, newPeasants, 0, 0, state.getAllResources(), this);

        nextGameState.numberOfPeasants = state.numberOfPeasants + 1;
        //As building peasant consumes one food and 400 gold respective resources are reduced in child state
        nextGameState.foodAvailable = (state.foodAvailable - 1);
        nextGameState.currentGold-=400;
        return nextGameState;
	}

	/**
     * Creating a primitive Produce action for the current state to build a peasant. 
     * This method is invoked in the PEAgent class, when it tries to convert STRIPS action to a Sepia action.
     *
     * @return Action An instance of an Action object that contains the details of the gather command.
     */
	@Override
	public Action createSepiaAction(int id) {
		return Action.createPrimitiveProduction(GameState.townhallStateView.getID(), PEAgent.peasantTemplateId);
	}

	public List<Integer> getPeasantsActing(){
        return peasantsActing;
    }
	/**
     * Converts this BuildPeasant action into a String format
     *
     * @return A String that represents the BuildPeasant action.
     */
    public String toString() {



     // Constructing the action String with relevant details
       String action = "BUILD PEASANT";

        // Constructing the action String with relevant details        
        System.out.println(action);
        return action;
    }

    public List<Integer> setPeasantsActing(Integer i){
        List<Integer> peasantsThatAct = getPeasantsActing();
        peasantsThatAct.remove(new Integer(i));
        return peasantsThatAct;
    }

}
