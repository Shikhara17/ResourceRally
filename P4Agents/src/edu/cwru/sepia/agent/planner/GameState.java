package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.ResourceNode.*;

import java.util.*;

/**
 * This class is used to represent the state of the game after applying one of the avaiable actions. It will also
 * track the A* specific information such as the parent pointer and the cost and heuristic function. Remember that
 * unlike the path planning A* from the first assignment the cost of an action may be more than 1. Specifically the cost
 * of executing a compound action such as move can be more than 1. You will need to account for this in your heuristic
 * and your cost function.
 *
 * The first instance is constructed from the StateView object (like in PA2). Implement the methods provided and
 * add any other methods and member variables you need.
 *
 * Some useful API calls for the state view are
 *
 * state.getXExtent() and state.getYExtent() to get the map size
 *
 * Note that SEPIA saves the townhall as a unit. Therefore when you create a GameState instance,
 * you must be able to distinguish the townhall from a peasant. This can be done by getting
 * the name of the unit type from that unit's TemplateView:
 * state.getUnit(id).getTemplateView().getName().toLowerCase(): returns "townhall" or "peasant"
 *
 * You will also need to distinguish between gold mines and trees.
 * state.getResourceNode(id).getType(): returns the type of the given resource
 *
 * You can compare these types to values in the ResourceNode.Type enum:
 * ResourceNode.Type.GOLD_MINE and ResourceNode.Type.TREE
 *
 * You can check how much of a resource is remaining with the following:
 * state.getResourceNode(id).getAmountRemaining()
 *
 * I recommend storing the actions that generated the instance of the GameState in this class using whatever
 * class/structure you use to represent actions.
 */
public class GameState implements Comparable<GameState> {

    public State.StateView state;
    //townhall unit
    public static Unit.UnitView townhallStateView;
    
    //amount of gold to be gathered and deposited by peasant
    public int requiredGold;
    //amount of wood to be gathered and deposited by peasant
    public int requiredWood;
    //amount of gold deposited by peasant at townhall
    public int currentGold=0;
    //amount of wood deposited by peasant at townhall
    public int currentWood=0;
    
    //actual cost incurred to reach current state
    public double cost;
    public GameState parent=null;
    public Peasant peasant;
    //StripsAction that is performed in the parent state to generate current state
    public StripsAction cameFromAction = null;
    public HashMap<Integer, Resource> resources = new HashMap<>();



    /**
     * Construct a GameState from a stateview object. This is used to construct the initial search node. All other
     * nodes should be constructed from the another constructor you create or by factory functions that you create.
     *
     * @param state The current stateview at the time the plan is being created
     * @param playernum The player number of agent that is planning
     * @param requiredGold The goal amount of gold (e.g. 200 for the small scenario)
     * @param requiredWood The goal amount of wood (e.g. 200 for the small scenario)
     * @param buildPeasants True if the BuildPeasant action should be considered
     */

    public GameState(State.StateView state, int playernum, int requiredGold, int requiredWood, boolean buildPeasants) {
        
        this.state = state;
        this.cost = 0;
        this.requiredGold = requiredGold;
        this.requiredWood = requiredWood;

        initializeResources();
        initializeUnits(playernum);
    }
    
    /**
     * A method that gets all the resourceIds of resources from current state and initializes the resources object for the current state
     * which includes all the resources present in the map
     */
    private void initializeResources() {
        for (int id : state.getAllResourceIds()) {
            ResourceView resource = state.getResourceNode(id);
            Position position = new Position(resource.getXPosition(), resource.getYPosition());
            this.resources.put(id,
                    new Resource(position, resource.getAmountRemaining(), resource.getType(), id));
        }
    }

    private void initializeUnits(int playernum) {
        for (int id : state.getUnitIds(playernum)) {
            String unitName = state.getUnit(id).getTemplateView().getName().toLowerCase();
            if (unitName.equals("townhall")) {
                townhallStateView = state.getUnit(id);
            } else if (unitName.equals("peasant")) {
                Position peasantPosition = new Position(state.getUnit(id).getXPosition(), state.getUnit(id).getYPosition());
                this.peasant = new Peasant(id, peasantPosition, false, 0, 0);

            }
        }
    }

 
    /**
     * Used to Construct a GameState from a another(parent) GameState object. 
     *
     * @param parent - Another GameState from which child State is created that has most of the properties from parent
     * @param cost - Overall Actual cost incurred on reaching current state from initial state
     * @param peasant - Peasant object that includes all the updated peasant unit properties
     * @param goldDeposited - Amount of gold that is deposited in townhall when compared to previous state 
     * @param woodDeposited - Amount of wood that is deposited in townhall when compared to previous state 
     * @param resources - HashMap of all the resources in the current state
     * @param action - StripsAction that is performed in the parent state to reach the current state
     */

    public GameState(GameState parent,double cost, Peasant peasant,int goldDeposited,int woodDeposited, HashMap<Integer, Resource> resources, StripsAction action) {

        this.parent = parent;
        this.cost = cost;
        this.peasant = peasant;
        this.resources = new HashMap<>(resources);
        this.cameFromAction = action;
        this.requiredGold = parent.requiredGold;
        this.requiredWood = parent.requiredWood;
        this.currentGold = parent.currentGold + goldDeposited;
        this.currentWood = parent.currentWood + woodDeposited;
        this.state = parent.state;
    }


    /**
     * Class that models a Peasant unit which basically has all the required properties/fields/
     */
    public static class Peasant {
    	
    	public int id;
    	
    	//denotes the type of resource that peasant currently holds i.e wood/gold
    	public String currentResourceType;
    	//coordinates of peasant
    	public Position position;
    	//determines if the peasant is carrying anything or is empty handed
    	public boolean isCarrying;
    	//amount of gold being carried by peasant
    	public int goldCarrying;
    	//amount of wood being carried by peasant
    	public int woodCarrying;


        public Peasant(int id, Position position, boolean isCarrying, int goldCarrying, int woodCarrying) {
            this.id = id;
            this.position = position;
            this.isCarrying = isCarrying;
            this.goldCarrying = goldCarrying;
            this.woodCarrying = woodCarrying;
            this.currentResourceType = null;
        }
    }

    /**
     * Class that models a Resource unit which basically has all the required properties/fields/
     */
    public static class Resource {
        public int resourceId;
        //used to quantify the amount of wood/gold remaining in the resource
        public int quantity;
        //location of resource
        public Position resourcePosition;
        //type of the resource i.e tree or gold mine
        public Type resourceType;


        public Resource(Position resourcePosition, int quantity, Type resourceType, int resourceId) {
            this.resourceId = resourceId;
            this.quantity = quantity;
            this.resourcePosition = resourcePosition;
            this.resourceType = resourceType;
        }
        
        //used to clone current instance if resource that calls this method
        public Resource resourceClone() {
            return new Resource(resourcePosition, quantity, resourceType, resourceId);
        }

    }

    //helper method to clone all resources rather than sharing the same object for multiple states
    public HashMap<Integer, Resource> getAllResources() {
        HashMap<Integer, Resource> hashMap = new HashMap<>();
        for (Map.Entry<Integer, Resource> entry : this.resources.entrySet()) {
            hashMap.put(entry.getKey(), entry.getValue().resourceClone());
        }
        return hashMap;
    }


    /**
     * Unlike in the first A* assignment there are many possible goal states. As long as the wood and gold requirements
     * are met the peasants can be at any location and the capacities of the resource locations can be anything. Use
     * this function to check if the goal conditions are met and return true if they are.
     *
     * @return true if the goal conditions are met in this instance of game state.
     */
    public boolean isGoal() {
    	
    	//states where the actual required gold and wood is less than the gold and wood that the townhall holds are goal states.
        return requiredGold <= currentGold && requiredWood <= currentWood;
    }

    /**
     * The branching factor of this search graph are much higher than the planning. Generate all of the possible
     * successor states and their associated actions in this method.
     *
     * @return A list of the possible successor states and their associated actions
     */

    public List<GameState> generateChildren() {

    	//store all the possible strips actions
        List<StripsAction> possibleActions = new ArrayList<>();
        //store all the generated child nodes/states
        List<GameState> childStates = new ArrayList<>();

        for (Resource resource : getAllResources().values()) {

        	
            StripsAction moveToResource = null;
            StripsAction harvestAtResource = null;
            
            //If the gold deposited/available at townhall is less than gold required to reach goal and if the resource is of type gold mine
            //we generate MoveTo and Harvest Strips Actions.
            if (this.currentGold < requiredGold && resource.resourceType == ResourceNode.Type.GOLD_MINE) {

                moveToResource = new MoveTo(resource.resourcePosition, this.peasant);
                //here the StripsAction that we generate is for harvesting gold. hence the last parameter(isGold) is set to true
                harvestAtResource = new Harvest(peasant, resource.resourceId,resource.resourcePosition, true);

            } 
            
            //If the wood deposited/available at townhall is less than wood required to reach goal and if the resource is of type tree
            //we generate MoveTo and Harvest Strips Actions.
            else if (this.currentWood < requiredWood && resource.resourceType == ResourceNode.Type.TREE) {
                
            	moveToResource = new MoveTo(resource.resourcePosition, this.peasant);            	
            	//here the StripsAction that we generate is for harvesting wood. hence the last parameter(isGold) is set to false
                harvestAtResource = new Harvest(peasant, resource.resourceId, resource.resourcePosition, false);
            }
            
            //add the moveTo and Harvest actions to possible actions only if the current state satifies the actions precondition
            if (moveToResource != null && moveToResource.preconditionsMet(this)) {
                possibleActions.add(moveToResource);
            }
            if (harvestAtResource != null && harvestAtResource.preconditionsMet(this)) {
                possibleActions.add(harvestAtResource);
            }
        }

        //Position Object to represent the townhall coordinates
        Position townhallPosition = new Position(townhallStateView.getXPosition(),townhallStateView.getYPosition());
        
        //As deposit is also a possible action in current state we generate a MoveTo StripsAction and Deposit StripsAction.
        MoveTo movetoTownhall = new MoveTo(townhallPosition, this.peasant);
        
        //only added to valid/possible actions when precondition is met
        if(movetoTownhall.preconditionsMet(this)) {
            possibleActions.add(movetoTownhall);
        }

        Deposit deposit = new Deposit(peasant, townhallPosition);
        if(deposit.preconditionsMet(this)) {
            possibleActions.add(deposit);
        }

        //Looping through all the valid StripsActions in the current state and generating the equivalent child states
        for (StripsAction action : possibleActions) {

            childStates.add(action.apply(this));
        }

        return childStates;


    }


    /**
     * Write your heuristic function here. Remember this must be admissible for the properties of A* to hold. If you
     * can come up with an easy way of computing a consistent heuristic that is even better, but not strictly necessary.
     * <p>
     * Add a description here in your submission explaining your heuristic.
     *
     * @return The value estimated remaining cost to reach a goal state from this state.
     */
    public double heuristic() {
    	//considered amount of gold and wood that is still required to reach goal state and costs incurred for harvesting these resources as heuristic
    	
    	//amount of gold that is still required to reach goal state from current state. 
        int goldRequiredToReachGoal =  requiredGold - currentGold - peasant.goldCarrying;
        //amount of wood that is still required to reach goal state from current state.
        int woodRequiredToReachGoal =  requiredWood - currentWood - peasant.woodCarrying ;
        
        //Calculated number of harvests that have to be done to reach goal state and multiplied them by cost for each harvest action
        //Considered each harvest cost as 100 hence multiplied the result with 100.
        int harvestGoldCost = ((goldRequiredToReachGoal)/100) * 100;
        int harvestWoodCost = ((woodRequiredToReachGoal)/100) * 100;

        return goldRequiredToReachGoal + woodRequiredToReachGoal + harvestGoldCost + harvestWoodCost;
    }

    /**
     * Write the function that computes the current cost to get to this node. This is combined with your heuristic to
     * determine which actions/states are better to explore.
     *
     * @return The current cost to reach this goal
     */
    public double getCost(){
        return cost;
    }
    public void setCost(double cost) {
        this.cost = cost;
    }

    /**
     * This is necessary to use your state in the Java priority queue. See the official priority queue and Comparable
     * interface documentation to learn how this function should work.
     *
     * @param o The other game state to compare
     * @return 1 if this state costs more than the other, 0 if equal, -1 otherwise
     */
    @Override
    public int compareTo(GameState o) {
        return Double.compare(this.getCost() + this.heuristic(), o.getCost() + o.heuristic());
    }

    /**
     * This will be necessary to use the GameState as a key in a Set or Map.
     *
     * @param o The game state to compare
     * @return True if this state equals the other state, false otherwise.
     */
    @Override
    public boolean equals(Object o) {

        return false; //using hashcode instead of equals
    }

    /**
     * This is necessary to use the GameState as a key in a HashSet or HashMap. Remember that if two objects are
     * equal they should hash to the same value.
     *
     * @return An integer hashcode that is equal for equal states.
     */
    @Override
    public int hashCode() {
    	
    	//Considered that 2 states are similar if the resources being carried by peasant, peasant's position and resources at townhall are the same.
        int hash =  Objects.hash(peasant.goldCarrying, peasant.woodCarrying, peasant.position, currentGold, currentWood);
        return hash;
    }
}