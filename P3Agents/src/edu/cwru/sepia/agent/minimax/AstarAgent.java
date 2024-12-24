package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.*;
import edu.cwru.sepia.util.*;
import java.io.*;
import java.util.*;


public class AstarAgent {

	
    private int xExtent, yExtent;
    public AstarAgent(int xExtent, int yExtent) {
    	
        this.xExtent = xExtent;
        this.yExtent = yExtent;
    }


    class MapLocation
    	{
    		public int x, y;
    		public MapLocation cameFrom;
    		public float cost;
    		public float heuristicCost;
        	public float f;
        	public MapLocation(int x, int y, MapLocation cameFrom, float cost)
        	{
        		this.x = x;
        		this.y = y;
        		this.cameFrom=cameFrom;
        		this.cost=cost;
        	}
    }


    public Stack<MapLocation> findPath(Set<ResourceNode.ResourceView> obstacles, PlayableUnit mover, PlayableUnit enemy) {

        MapLocation startLoc = new MapLocation(mover.getX(), mover.getY(), null, 0);
        MapLocation goalLoc = new MapLocation(enemy.getX(), enemy.getY(), null, 0);

        Set<MapLocation> resourceLocations = new HashSet<MapLocation>();
        for (ResourceNode.ResourceView resource : obstacles) {
            resourceLocations.add(new MapLocation(resource.getXPosition(), resource.getYPosition(), null, 0));
        }

        return AstarSearch(startLoc, goalLoc, xExtent, yExtent, null, resourceLocations);
    }







    /**
     * This is the method you will implement for the assignment. Your implementation
     * will use the A* algorithm to compute the optimum path from the start position to
     * a position adjacent to the goal position.
     *
     * Therefore your you need to find some possible adjacent steps which are in range
     * and are not trees or the enemy footman.
     * Hint: Set<MapLocation> resourceLocations contains the locations of trees
     *
     * You will return a Stack of positions with the top of the stack being the first space to move to
     * and the bottom of the stack being the last space to move to. If there is no path to the townhall
     * then return null from the method and the agent will print a message and do nothing.
     * The code to execute the plan is provided for you in the middleStep method.
     *
     * As an example consider the following simple map
     *
     * F - - - -
     * x x x - x
     * H - - - -
     *
     * F is the footman
     * H is the townhall
     * x's are occupied spaces
     *
     * xExtent would be 5 for this map with valid X coordinates in the range of [0, 4]
     * x=0 is the left most column and x=4 is the right most column
     *
     * yExtent would be 3 for this map with valid Y coordinates in the range of [0, 2]
     * y=0 is the top most row and y=2 is the bottom most row
     *
     * resourceLocations would be {(0,1), (1,1), (2,1), (4,1)}
     *
     * The path would be
     *
     * (1,0)
     * (2,0)
     * (3,1)
     * (2,2)
     * (1,2)
     *
     * Notice how the initial footman position and the townhall position are not included in the path stack
     *
     * @param start Starting position of the footman
     * @param goal MapLocation of the townhall
     * @param xExtent Width of the map
     * @param yExtent Height of the map
     * @param resourceLocations Set of positions occupied by resources
     * @return Stack of positions with top of stack being first move in plan
     */

    private Stack<MapLocation> AstarSearch(MapLocation start, MapLocation goal, int xExtent, int yExtent, MapLocation enemyFootmanLoc, Set<MapLocation> resourceLocations)
    {

        //Comparator for MapLocation objects based on its fvalues
        Comparator<MapLocation> mapLocationComparator = Comparator.comparingDouble(mapLocation -> mapLocation.f);

        //Initializing a priorityQueue using a mapLocationComparator defined above.
        PriorityQueue<MapLocation> openSet = new PriorityQueue<>(mapLocationComparator);

        //Initializing a HashSet
        Set<MapLocation> closedSet = new HashSet<>();
    	
        //Calculating the heuristic values for the root node and assigning them to a MapLocation object - start
        start.cost = 0;
        start.heuristicCost = calculateChebyshevDistance(start, goal);
        start.f = start.cost + start.heuristicCost;

        //Adding the starting node to the openSet
        openSet.add(start);

        //Iterating in the openSet until all the nodes have been explored.
        while (!openSet.isEmpty()) {

            //Retrieve the node with the smallest f value from the openSet.
            MapLocation currentNode = openSet.poll();

            //Verifying if we have reached the goal node
            if (currentNode.x==goal.x && currentNode.y==goal.y) {

                //returning the path
                return configurePath(currentNode.cameFrom);
            }

            //Adding the currentNode to the closedSet as it has been visited
            closedSet.add(currentNode);

            //Retrieving the available valid neighbor nodes for the currentNode
            Stack<MapLocation> neighborNodes = expandNextAvailableValidSteps(currentNode,goal,xExtent, yExtent, resourceLocations);

            //Iterating through the available neighborNodes
            for (MapLocation neighborNode : neighborNodes) {

                //Initialized Iterators for closedSet and openSet
            	Iterator<MapLocation> closedSetIterator = closedSet.iterator();
            	Iterator<MapLocation> openSetIterator = openSet.iterator();
                
            	boolean inOpenSet=false;
            	boolean inClosedSet=false;
            	
            	while (closedSetIterator.hasNext()){
    	            MapLocation closedSetNode = closedSetIterator.next();

    	            //Checking if the closedSet contains the current neighborNode
    	            if((closedSetNode.x == neighborNode.x && closedSetNode.y == neighborNode.y)) {
    	            	inClosedSet=true;
    	            	break;
    	            }         
            	}

            	//Skipping this neighbor node if it already exists in the closedSet
            	if (inClosedSet) {
            		continue;
            	}

                //Checking if the current neighbor node position is occupied by a footman
            	if (enemyFootmanLoc!=null) {
            		
            		if (enemyFootmanLoc.x==neighborNode.x && enemyFootmanLoc.y==neighborNode.y) {
            			continue;
            		}
            	}          	
            	
            	while (openSetIterator.hasNext()){
    	            MapLocation openSetNode = openSetIterator.next();

    	            //Checking if the openSet contains the current neighbor node and comparing the costs between the neighborNode and the
                    //node existing in the openSet
    	            if (openSetNode.x == neighborNode.x && openSetNode.y == neighborNode.y && openSetNode.cost >= neighborNode.cost) {
    	            	inOpenSet=true;

    	            	//removing from the openSet
    	            	openSet.remove(openSetNode);

    	            	//Adding the new neighbor node as it has the lesser cost/fvalue
    	            	openSet.add(neighborNode);
    	            	break;
    	            }
    	        }

                //Adding this neighbor node if it doesn't exist in the openSet
            	if (!inOpenSet) {
            		openSet.add(neighborNode);
            	} 

            }
        }
        

        // Invoking System.ext() if no path can be found.(As per problem statement)
        System.out.println("No available path.");
      

        return new Stack<MapLocation>();
    }
    private Stack<MapLocation> expandNextAvailableValidSteps(MapLocation currentLocation, MapLocation goal,int xExtent, int yExtent, Set<MapLocation> resourceLocations) {
  	  
    	//Initializing Stack<MapLocations> to store and return valid neighbors
    	Stack<MapLocation> nextPossibleSteps = new Stack<MapLocation>();	
    	
    	//A 2d array which corresponds to all the moves that are possible in any location.
    	int[][] directions = {

		        {-1, 0}, {1, 0}, {0, -1}, {0, 1}   //Moving up,down,left,right


		    };   	
    	
    	//Loop through each direction, to create corresponding neighbor Nodes 
    	for (int[] direction:directions) {    		
    		
    		//Represents the x,y coordinates of the next step.
    		int nextX = currentLocation.x + direction[0];
    		int nextY = currentLocation.y + direction[1];
    		
    		//Checking whether the nextPossible location falls within the boundary of the given map
    		if (nextX >= 0 && nextX < xExtent && nextY >= 0 && nextY < yExtent) {   			
    			
    			//Populates the actual cost for the next step by adding 1 to actual path cost of its parent i.e currentLocation
    			float actualCostForNextStep = currentLocation.cost + 1;
    			MapLocation nextPossibleStep = new MapLocation(nextX, nextY, currentLocation, actualCostForNextStep);
    			
    			//Defined a boolean to store information on whether the next step is blocked by tree or not 
    			boolean isNextStepBlockedByTree = false;
    			
    			//Initialized iterator for iterating through all resourceLocations
    			Iterator<MapLocation> setIterator = resourceLocations.iterator();
    			
    			//Iterating through resourceLocations to check if nextPossibleStep is occupied by tree.
    	        while (setIterator.hasNext()){
    	            
    	        	MapLocation tree = setIterator.next();   	            
    	            
    	        	//If there exists a tree such that its coordinates are equal to nextPossibleStep's coordinates then we set 'isNextStepBlockedByTree' to true 	
    	        	if (tree.x == nextPossibleStep.x && tree.y == nextPossibleStep.y) {
    	            	isNextStepBlockedByTree=true;
    	            	break;
    	            }
    	        }
    			
    	        //If the current neighbor is a valid neighbor we calculate its heuristic and add it to list of valid neighbors.
    	        if (!isNextStepBlockedByTree) {
    	        	
    	        	//Calculate the heuristic value of corresponding neighbor node
    	        	nextPossibleStep.heuristicCost = calculateChebyshevDistance(nextPossibleStep,goal);
    	        	
    	        	//As we know total cost = actual cost + heuristic
    	        	nextPossibleStep.f = nextPossibleStep.cost + nextPossibleStep.heuristicCost;
    	        	nextPossibleSteps.add(nextPossibleStep);
    	        }			
    		}
    		
    	}
    	
    	// Returning all the possible valid(within boundary, Not occupied by tree) neighbors.
    	return nextPossibleSteps;
    }
    
    /**
     * This method would extract/backtrack the path from a given destination node and returns Stack of 
     * positions that needs to be followed in order to move from initial node to destination node. 
     * 
     * 
     * @param destination MapLocation of the townhall.
     * @return Stack of positions with top of stack being first move in plan
     */ 
    private Stack<MapLocation> configurePath(MapLocation destination){
    	
    	//Initialize a new Stack<MapLocation> to track path.
    	Stack<MapLocation> path = new Stack<MapLocation>();
    	
    	//Iterate through all the corresponding parent nodes and adding them to path until we reach initial state
    	while (destination != null) {
    		
    		path.add(destination);
    		
    		//replacing current object with its parent.
    		destination=destination.cameFrom;
    	}  	
    	//Remove current/initial position of footman from stack
    	path.pop();
    	
    	//Return stack of positions where first position is the next location that agent can move to
    	return path;
    }
    
    
    /**
     * This function calculates the chebyshev distance between two points
     * by returning the maximum value among the absolute difference between x and y coordinates of 2 locations
     *  
     * @param current - Map Location Object that represents current node
     * @param goal - Map Location Object that represents Goal/Townhall 
     * @return - Chebyshev distance between current and goal which acts as a heuristic value 
     */
    private float calculateChebyshevDistance(MapLocation currentNode, MapLocation goal) {
    	
    	// Calculating chebyshev distance between currentNode and goal.
    	return Math.max(Math.abs(goal.x - currentNode.x), Math.abs(goal.y - currentNode.y));
    }   
}
