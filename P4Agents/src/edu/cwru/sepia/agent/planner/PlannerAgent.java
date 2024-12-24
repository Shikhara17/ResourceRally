package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

import java.io.*;
import java.util.*;

/**
 * Created by Devin on 3/15/15.
 */
public class PlannerAgent extends Agent {

    final int requiredWood;
    final int requiredGold;
    final boolean buildPeasants;

    // Your PEAgent implementation. This prevents you from having to parse the text file representation of your plan.
    PEAgent peAgent;

    public PlannerAgent(int playernum, String[] params) {
        super(playernum);

        if(params.length < 3) {
            System.err.println("You must specify the required wood and gold amounts and whether peasants should be built");
        }

        requiredWood = Integer.parseInt(params[0]);
        requiredGold = Integer.parseInt(params[1]);
        buildPeasants = Boolean.parseBoolean(params[2]);


        System.out.println("required wood: " + requiredWood + " required gold: " + requiredGold + " build Peasants: " + buildPeasants);
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView stateView, History.HistoryView historyView) {

        Stack<StripsAction> plan = AstarSearch(new GameState(stateView, playernum, requiredGold, requiredWood, buildPeasants));

        if(plan == null) {
            System.err.println("No plan was found");
            System.exit(1);
            return null;
        }
        
        System.out.println("AstarSearch Plan: " + plan.size());
        // write the plan to a text file
        savePlan(plan);


        // Instantiates the PEAgent with the specified plan.
        peAgent = new PEAgent(playernum, plan);

        return peAgent.initialStep(stateView, historyView);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {
        if(peAgent == null) {
            System.err.println("Planning failed. No PEAgent initialized.");
            return null;
        }

        return peAgent.middleStep(stateView, historyView);
    }

    @Override
    public void terminalStep(State.StateView stateView, History.HistoryView historyView) {

    }

    @Override
    public void savePlayerData(OutputStream outputStream) {

    }

    @Override
    public void loadPlayerData(InputStream inputStream) {

    }

    /**
     * Perform an A* search of the game graph. This should return your plan as a stack of actions. This is essentially
     * the same as your first assignment. The implementations should be very similar. The difference being that your
     * nodes are now GameState objects not MapLocation objects.
     *
     * @param startState The state which is being planned from
     * @return The plan or null if no plan is found.
     */
    private Stack<StripsAction> AstarSearch(GameState startState){
        
        // A Closed list which keeps track of game states already explored.
        Hashtable<Integer, GameState> closedList = new Hashtable<Integer, GameState>(); 
        
        // A Open list contains game states need to be explored, prioritized by their cost.
        PriorityQueue<GameState> openList = new PriorityQueue<>(GameState::compareTo);
        openList.add(startState);
        
        // Looping in a while until all the game states are explored in the openList.
        while (openList.size() > 0) {
        	
        	// poll - remove and get the game state.
            GameState currentGameState = openList.poll();
            
            // check if the current game state is one among the goal states.
            if (currentGameState.isGoal()) {
            	
                //Reconstruct the path of actions that leads to goal state.
                return configureActions(currentGameState);
            }
            
            // add the current state to the closed list if it is not present in the the list, we check this by 
            // calculating the hashcode of the current game state.
            closedList.putIfAbsent(currentGameState.hashCode(), currentGameState);
            
            //generate child states of the current game state.
            List<GameState> childrenOfCurrentGameState = currentGameState.generateChildren();

            for (GameState nextChildState : childrenOfCurrentGameState) {
                
            	// Check if a similar state exists in the closed list by searching with the current child state object hashcode
            	GameState similarExistingState = closedList.get(nextChildState.hashCode());
            	
            	// If a similar state is found in the closed list and its cost is greater than the current child state,
                // update the cost and parent of the state
                if (similarExistingState != null ) {
                    
                	if (similarExistingState.getCost() > nextChildState.getCost()) {
                		similarExistingState.setCost(nextChildState.getCost());
                		similarExistingState.parent = nextChildState.parent;
                    }
                    
                }
                
                // If the state is not in the closed list, add it to the open list.
                else {
                	openList.add(nextChildState);
                } 
            }
        }
        
        // If no path is found, return an empty stack of actions.
        System.out.println("No path found");
        return new Stack<>();
    }
    
   /**
    * This method would backtrack the actions from the goal state and returns the stack of strips actions that 
    * needs to be performed in order to reach the goal state from the initial state
    * @param state The on of the possible goal states  
    * @return A stack of actions
    */
    private Stack<StripsAction> configureActions(GameState state){
    	
    	Stack<StripsAction> actions = new Stack<StripsAction>();
    	
    	GameState parent = state;
    	while (parent.cameFromAction != null) {
    		
    		actions.add(parent.cameFromAction);
    		
    		parent=parent.parent;
    	}  	

    	return actions;
    }

    /**
     * This has been provided for you. Each strips action is converted to a string with the toString method. This means
     * each class implementing the StripsAction interface should override toString. Your strips actions should have a
     * form matching your included Strips definition writeup. That is <action name>(<param1>, ...). So for instance the
     * move action might have the form of Move(peasantID, X, Y) and when grounded and written to the file
     * Move(1, 10, 15).
     *
     * @param plan Stack of Strips Actions that are written to the text file.
     */
    private void savePlan(Stack<StripsAction> plan) {
        if (plan == null) {
            System.err.println("Cannot save null plan");
            return;
        }

        File outputDir = new File("saves");
        outputDir.mkdirs();

        File outputFile = new File(outputDir, "plan.txt");

        PrintWriter outputWriter = null;
        try {
            outputFile.createNewFile();

            outputWriter = new PrintWriter(outputFile.getAbsolutePath());

            Stack<StripsAction> tempPlan = (Stack<StripsAction>) plan.clone();
            while(!tempPlan.isEmpty()) {
                outputWriter.println(tempPlan.pop().toString());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputWriter != null)
                outputWriter.close();
        }
    }
}