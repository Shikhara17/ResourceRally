package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.DirectedAction;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.agent.minimax.AstarAgent.MapLocation;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.util.Direction;

import java.util.*;

/**
 * This class stores all of the information the agent
 * needs to know about the state of the game. For example this
 * might include things like footmen HP and positions.
 *
 * Add any information or methods you would like to this class,
 * but do not delete or change the signatures of the provided methods.
 */
public class GameState {

    /**
     * You will implement this constructor. It will
     * extract all of the needed state information from the built in
     * SEPIA state view.
     *
     * You may find the following state methods useful:
     *
     * state.getXExtent() and state.getYExtent(): get the map dimensions
     * state.getAllResourceIDs(): returns the IDs of all of the obstacles in the map
     * state.getResourceNode(int resourceID): Return a ResourceView for the given ID
     *
     * For a given ResourceView you can query the position using
     * resource.getXPosition() and resource.getYPosition()
     * 
     * You can get a list of all the units belonging to a player with the following command:
     * state.getUnitIds(int playerNum): gives a list of all unit IDs beloning to the player.
     * You control player 0, the enemy controls player 1.
     * 
     * In order to see information about a specific unit, you must first get the UnitView
     * corresponding to that unit.
     * state.getUnit(int id): gives the UnitView for a specific unit
     * 
     * With a UnitView you can find information about a given unit
     * unitView.getXPosition() and unitView.getYPosition(): get the current location of this unit
     * unitView.getHP(): get the current health of this unit
     * 
     * SEPIA stores information about unit types inside TemplateView objects.
     * For a given unit type you will need to find statistics from its Template View.
     * unitView.getTemplateView().getRange(): This gives you the attack range
     * unitView.getTemplateView().getBasicAttack(): The amount of damage this unit type deals
     * unitView.getTemplateView().getBaseHealth(): The initial amount of health of this unit type
     *
     * @param state Current state of the episode
     */

    //Movers represent the current player depending on turn i.e if turn =0 then movers represent footman unit and if turn=1 movers=archers
	//Movers always are the moving units in current turn.
    public List<PlayableUnit> movers;
    //if archers are movers footmen are opponents, viceversa
    public List<PlayableUnit> opponents;
    public int xExtent;
    public int yExtent;
    public Set<ResourceNode.ResourceView> obstacles;
    public AstarAgent AstarAgent;
    //used to identify player's turn i.e archer or footmen.
    public int turnNum=0;
    public int utility;
    //used to store only valid directions from available directions i.e non diagonal movements.
    public List<Direction> validDirections;

    //Constructor that generates a GameState from the given StateView object
    public GameState(State.StateView state) {
        movers = new ArrayList<>();
        opponents = new ArrayList<>();
        //used to extract units i.e footman  and archers from the StateView Object.
        extractUnitInformation(state);
        xExtent = state.getXExtent();
        yExtent = state.getYExtent();
        obstacles = new HashSet<>(state.getAllResourceNodes());
        AstarAgent = new AstarAgent(xExtent, yExtent);
        //adding only valid directions which is used later on.
        validDirections = Arrays.asList(Direction.NORTH, Direction.EAST, Direction.WEST, Direction.SOUTH);      
    }

    //Another Constructor that initializes current GameState from another GameState Object.
    public GameState(GameState previousGameState) {
        xExtent = previousGameState.xExtent;
        yExtent = previousGameState.yExtent;
        obstacles = new HashSet<>(previousGameState.obstacles);
        AstarAgent = new AstarAgent(xExtent, yExtent);
        movers = new ArrayList<>();
        opponents = new ArrayList<>();

        //switching current GameState's turn depending on previous state because we use this constructor only to generate child game state from parent state.
        //this is necessary as for even turn footman is mover and for odd turns archer becomes mover.
        turnNum=previousGameState.turnNum==0? 1:0;

        //For every other turn movers and opponents are reversed as it is a turn taking game.
        for (PlayableUnit f : previousGameState.opponents) {
            movers.add(new PlayableUnit(f));          
        }
        for (PlayableUnit a : previousGameState.movers) {
            opponents.add(new PlayableUnit(a));
        }
        
        validDirections = Arrays.asList(Direction.NORTH, Direction.EAST, Direction.WEST, Direction.SOUTH);
    }

    /**
     *This method is used to extract footmen and archer units by considering StateView Object
     *
     * @param state Current state of the episode
     */
    private void extractUnitInformation(State.StateView state) {
    	
    	movers = extractUnitsFromUnitIds(state.getUnitIds(0),state);
        opponents = extractUnitsFromUnitIds(state.getUnitIds(1),state);	
        
    }

    //used in "extractUnitInformation" to fetch units from unitIds using unitView.
    private List<PlayableUnit> extractUnitsFromUnitIds(List<Integer> unitIds,State.StateView state) {
        List<PlayableUnit> units = new ArrayList<>();
        for (Integer unitId : unitIds) {
        	Unit.UnitView unitView = state.getUnit(unitId);
            units.add(new PlayableUnit(unitView));
        }
        return units;
    }



    /**
     * You will implement this function.
     *
     * You should use weighted linear combination of features.
     * The features may be primitives from the state (such as hp of a unit)
     * or they may be higher level summaries of information from the state such
     * as distance to a specific location. Come up with whatever features you think
     * are useful and weight them appropriately.
     *
     * It is recommended that you start simple until you have your algorithm working. Then watch
     * your agent play and try to add features that correct mistakes it makes. However, remember that
     * your features should be as fast as possible to compute. If the features are slow then you will be
     * able to do less plys in a turn.
     *
     * Add a good comment about what is in your utility and why you chose those features.
     *
     * @return The weighted linear combination of the features
     */
    public double getUtility() {

    	// considered footmen hp , attackers hp and totalDistance to opponents as utility features.
        //even planned on adding range as a utility feature but in certain stages instead of blocking archer it attacks which creates a possibility for archer to escape and survive longer
    	int footmensTotalHealth =0;
    	int archersTotalHealth=0;
    	int totalDistanceToOpponents=0;
    	
        /**
         *	As utility is only considered in the perspective of the footmen and as movers and opponents keep on changing
         *  based on turnNumber we calculate utility so that it is evaluated in the perspective of footmen.
         */
    	if(turnNum%2==0) {
    		
    		 footmensTotalHealth = movers.stream().mapToInt(PlayableUnit::getHp).sum();
             archersTotalHealth = opponents.stream().mapToInt(PlayableUnit::getHp).sum();
             totalDistanceToOpponents = calculateTotalDistance(movers,opponents);
    	}
    	else {
    		
    		 footmensTotalHealth = opponents.stream().mapToInt(PlayableUnit::getHp).sum();
             archersTotalHealth = movers.stream().mapToInt(PlayableUnit::getHp).sum();
             totalDistanceToOpponents = calculateTotalDistance(opponents,movers);
    	}
        
        //only reward footmen for maintaining high hp, and penalizing based on distance to opponent and opponents hp
        utility = (5 * footmensTotalHealth) + (-10 * archersTotalHealth) + (-15 * totalDistanceToOpponents)  ;
        return (double) utility;
    }   
 
    /**
     *This method is used to calculate the cumulative distance between each footman and the closest archer to the footman.
     *
     * @param footmen List of available footmen
     * @param archers List of available archers
     */
    private int calculateTotalDistance(List<PlayableUnit> footmen, List<PlayableUnit> archers) {
        int totalDistance = 0;
        
        for (PlayableUnit unit : footmen) {   
            //fetching the closest archer to the current footman.  	
            PlayableUnit closestArcher = getClosestUnit(unit,archers);
            
            //using A* as a heuristic feature only for map with obstacles because for map without obstacles path is same as Manhattan distance.
            if (obstacles.size()>0 && !isOpponentInRange(unit, closestArcher)) {
            	
                //only used when footman is not adjacent to archer as A* returns path to adjacent block of goal.
                Stack<MapLocation> aStarGoalPath = AstarAgent.findPath(obstacles, unit, closestArcher);
                totalDistance += aStarGoalPath.firstElement().f;
            }
            else {
            	
                //calculate total distance using Manhattan distance when map doesn't have obstacles.
            	totalDistance+=distanceToEnemy(unit,closestArcher);
            }
        }

        return totalDistance;
    }
    
    private int distanceToEnemy(PlayableUnit unit1, PlayableUnit unit2) {
        return Math.abs(unit1.getX() - unit2.getX()) + Math.abs(unit1.getY() - unit2.getY());
    }

    /**
     * You will implement this function.
     *
     * This will return a list of GameStateChild objects. You will generate all of the possible
     * actions in a step and then determine the resulting game state from that action. These are your GameStateChildren.
     * 
     * It may be useful to be able to create a SEPIA Action. In this assignment you will
     * deal with movement and attacking actions. There are static methods inside the Action
     * class that allow you to create basic actions:
     * Action.createPrimitiveAttack(int attackerID, int targetID): returns an Action where
     * the attacker unit attacks the target unit.
     * Action.createPrimitiveMove(int unitID, Direction dir): returns an Action where the unit
     * moves one space in the specified direction.
     *
     * You may find it useful to iterate over all the different directions in SEPIA. This can
     * be done with the following loop:
     * for(Direction direction : Directions.values())
     *
     * To get the resulting position from a move in that direction you can do the following
     * x += direction.xComponent()
     * y += direction.yComponent()
     * 
     * If you wish to explicitly use a Direction you can use the Direction enum, for example
     * Direction.NORTH or Direction.NORTHEAST.
     * 
     * You can check many of the properties of an Action directly:
     * action.getType(): returns the ActionType of the action
     * action.getUnitID(): returns the ID of the unit performing the Action
     * 
     * ActionType is an enum containing different types of actions. The methods given above
     * create actions of type ActionType.PRIMITIVEATTACK and ActionType.PRIMITIVEMOVE.
     * 
     * For attack actions, you can check the unit that is being attacked. To do this, you
     * must cast the Action as a TargetedAction:
     * ((TargetedAction)action).getTargetID(): returns the ID of the unit being attacked
     * 
     * @return All possible actions and their associated resulting game state
     */
    public List<GameStateChild> getChildren() {
        List<GameStateChild> validChildren = new ArrayList<>();

        //get the first mover's id     
        int mover1Id = movers.get(0).getId();

        //fetches all the available actions for mover 1 (here mover can be either footman or archer depending on turn)
        List<Action> mover1Actions = getActions(movers.get(0), opponents);

        int mover2Id = -1;
        List<Action> mover2Actions = Collections.emptyList();

        //fetching mover2's actions only if it's alive
        if (movers.size() > 1) {
        	mover2Id = movers.get(1).getId();
        	mover2Actions = getActions(movers.get(1), opponents);
        }

       //iterating through the mover1 and mover 2 actions using nested loops
        for (Action mover1Action : mover1Actions) {
            if (mover2Id != -1) { 
                for (Action mover2Action : mover2Actions) {

                    //generate child states based on mover 1 and 2 actions.
                	validChildren.add(createChildState(mover1Id, mover1Action, mover2Id, mover2Action));
                }
            } else {

                //generate child state based on mover 1 actions as its the only surviving mover unit.
            	validChildren.add(createChildState(mover1Id, mover1Action,-1,null));
            }
        }

        return validChildren;
    }


    /**
     *This method is used to generate next child states by calling "performActions" method,
     *that performs actions on current state and returns next GameStateChild.
     * @param id1 , id of the mover1
     * @param action1 , one among the available actions of mover1
     * @param id2 , id of the mover2 -1 if mover 2 doesn't exist
     * @param action1 , one among the available actions of mover2, null if mover 2 doesn't exist
     * @return GameStateChild, represent next state.
     */
    private GameStateChild createChildState(int id1, Action action1, int id2, Action action2) {
    	
        //create a HashMap to store multiple actions(one for mover1 and one for mover2) that has to be performed on current state.
        Map<Integer, Action> actionMap = new HashMap<>();
        actionMap.put(id1, action1);
        if (id2 != -1) {
        	actionMap.put(id2, action2);
        }
        GameState childState = new GameState(this);

        //perform available actions on the current GameState to generate the next state.
        childState.performActions(actionMap);
        return new GameStateChild(actionMap, childState);
    }
    
     /**
     *This method is used apply actions on the current GameState.
     * @param actions, hashMap of actions that has to be applied to current state
     */
    public void performActions(Map<Integer, Action> actions) {
    	
        actions.forEach((unitId, action) -> {

            //if action is of type PRIMITIVEATTACK we lower target units hp and update.
            if (action.getType() == ActionType.PRIMITIVEATTACK) {
            	
            	int hpAfterAttack=0;
                TargetedAction attackAction = (TargetedAction) action;
                int targetId = attackAction.getTargetId();
                PlayableUnit attacker = fetchUnitById(unitId);
                PlayableUnit target = fetchUnitById(targetId);
                hpAfterAttack = target.getHp() - attacker.getDamage();
                target.setHp(hpAfterAttack);
            } 

             //if action type is PRIMITIVEMOVE we update the coordinates of respective mover.
            else if (action.getType() == ActionType.PRIMITIVEMOVE) {
            	
            	int nextX=0;
            	int nextY=0;
                DirectedAction moveAction = (DirectedAction) action;
                Direction direction = moveAction.getDirection();
                PlayableUnit unit = fetchUnitById(unitId);
                nextX = unit.getX() + direction.xComponent();
                nextY = unit.getY() + direction.yComponent();
                unit.setXY(nextX,nextY);
            }
        });
    }
    
     /**
     *This method is used to get a unit (footman or archer) from given unitId
     * @param id, ID of the unit in game.
     */
    private PlayableUnit fetchUnitById(int id) {
        for (PlayableUnit gameUnit : movers) {
            if (gameUnit.getId() == id) {
                return gameUnit;
            }
        }
        for (PlayableUnit gameUnit : opponents) {
            if (gameUnit.getId() == id) {
                return gameUnit;
            }
        }
        return null;
    }
 

    /**
     *This method is used fetch all the available actions for the corresponding mover by considering list of opponents.
     *It is called in initial stage of "getChildren" method.
     *@param mover, corresponding players who's actions we are planning to generate
     *@param opponentsList, list of opponent players.
     *@return actions, List of possible actions for the given mover.
     */
    private List<Action> getActions(PlayableUnit mover, List<PlayableUnit> opponentsList) {
    	
        List<PlayableUnit> allUnits = new ArrayList<>(movers);
        allUnits.addAll(opponents);
        List<Action> actions = new ArrayList<>();

        //get the closest enemy unit for the current mover.
        PlayableUnit closestEnemy = getClosestUnit(mover,opponentsList);
        
        //generates an attack action the opponent is it's in the range of current mover.
        if (isOpponentInRange(mover, closestEnemy)) {
            actions.add(Action.createPrimitiveAttack(mover.getId(), closestEnemy.getId()));
        }

        // A* is used to find next possible step on in maps where obstacles are present and A* is only run if the mover is footman.
        //If A* is also used for archer by removing second statement in "if" codes works faster for even depths greater than 10.
        if (obstacles.size() > 0 && turnNum%2==0) {
			Stack<MapLocation> optimalPath = AstarAgent.findPath(obstacles, mover, closestEnemy);
			if (optimalPath != null && optimalPath.size() > 0) {
				MapLocation nextCoordinate = optimalPath.pop();

                //convert coordinate into corresponding direction and PRIMITIVEMOVE action is generated.
				actions.add(Action.createPrimitiveMove(mover.getId(), getDirectionWithCoordinates(mover, nextCoordinate)));
			}
		}
        else {

            // for map without obstacles or if its archer's turn then rather than depending on A* we generate all actions.
        	for (Direction direction : validDirections) {
                int newX = mover.getX() + direction.xComponent();
                int newY = mover.getY() + direction.yComponent();


                //checking if the next move is valid or not.
                if (isValidMove(newX, newY, allUnits, obstacles)) {
                    actions.add(Action.createPrimitiveMove(mover.getId(), direction));
                }
            }
        }
        
        return actions;
    }

    /**
     *This method converts the coordinates returned by the A* method into respective Direction.
     *@param unit, represents the gameunit
     *@param nextCoord, represents the coordinates returned by the A* method
     */
    private Direction getDirectionWithCoordinates(PlayableUnit unit, MapLocation nextCoord) {
        int xDiff = unit.getX() - nextCoord.x;
        int yDiff = unit.getY() - nextCoord.y;

        switch (xDiff) {
            case 0:
                switch (yDiff) {
                    case 1: return Direction.NORTH;
                    case -1: return Direction.SOUTH;
                    default: return null;
                }
            case 1: return Direction.WEST;
            case -1: return Direction.EAST;
            default: return null;
        }
    }

    /**
     *This method is used calculate the distance between 2 units (one is the player and other is the opponent)
     *It checks if the distance between 2 units is less than the attacking range of the playing unit.
     *@param unit1, corresponding players who's actions we are planning to generate
     *@param unit2, opponent unit to check if it's in the range of unit1.
     */
    private boolean isOpponentInRange(PlayableUnit unit1, PlayableUnit unit2) {
        int distanceX = Math.abs(unit1.getX() - unit2.getX());
        int distanceY = Math.abs(unit1.getY() - unit2.getY());
        return distanceX <= unit1.getRange() && distanceY <= unit1.getRange();
    }

    /**
     *This method is used to determine if the next move is valid or not i.e its coordinates should be lesser than mapExtent, 
     *it should not collide with any existing unit's coordinates and it shouldn't be occupied by a tree/obstacle
     *@param x, x-coordinate of the next move
     *@param y, y-coordinate of the next move
     *@param gameUnits, all the alive game units(including movers and opponents)
     *@param obstacles, List of ResourceViews that represents the tree(obstacles) in the given map
     *@return returns a boolean move representing if the next move is valid or not.
     */
    private boolean isValidMove(int x, int y, List<PlayableUnit> gameUnits, Set<ResourceNode.ResourceView> obstacles) {
        if (x >= 0 && x < xExtent && y >= 0 && y < yExtent) {
            for (PlayableUnit unit : gameUnits) {
                if (unit.getX() == x && unit.getY() == y) {
                    return false;
                }
            }
            for (ResourceNode.ResourceView a : obstacles) {
                if (x == a.getXPosition() && y == a.getYPosition()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     *This method is used to find out the closest opponent to the corresponding playable unit. It takes 2 parameter 
     *first one is the unit that's currently being controlled and second would be the list of opponents among whom we need to find closest one.
     *@param unit, current unit being controlled (mover1 or mover 2)
     *@param opponents, List of opponents in the current turn
     *@return closestUnit, closest opponent unit to the controlled unit.
     */
    private PlayableUnit getClosestUnit(PlayableUnit unit,List<PlayableUnit> opponents) {
        int minDistance = Integer.MAX_VALUE;
        PlayableUnit closestUnit = null;

        for (PlayableUnit opponent : opponents) {
            int distance = Math.abs(unit.getX() - opponent.getX()) + Math.abs(unit.getY() - opponent.getY());
            if (distance < minDistance) {
                minDistance = distance;
                closestUnit = opponent;
            }
        }
        return closestUnit;
    }   
}