csds600-f23-6

## P2Agents: Pathfinding

Implemented A\* algorithm with which agent can find a path from current location to Townhall.

### Changes made in the existing code

1.  **MapLocation class**: Added two new fields in the existing class i.e heuristicCost,f. Modified the constructor so that it assigns values for cameFrom and cost fields during initialization.
2.  **terminalStep:** System.exit() is invoked in the terminalstep function in order to stop the execution after logging "Destroyed the TownHall" in case of dynamic maps.

### Agent's Functionality:

1.  **AstarSearch():**
    The `AstarSearch` function executes the A\* search algorithm to determine the optimal path from a specific starting point to the goal and then destroy the town hall in a maze. It takes into account potential obstacles, the presence of enemy footmen, and the presence of resource locations during the path calculation. This function uses an **openSet** to store nodes that need to be expanded according to their actual path cost, and a **closedSet** to track the nodes that have been visited. <br/><br/>`AstarSearch` invokes other methods such as `configurePath()`, `calculateChebyshevDistance()`, and `expandNextAvailableValidSteps()` to find the optimal path.

2.  **expandNextAvailableValidSteps():**
    The `expandNextAvailableValidSteps` function expands the current node and generates all of its succesors. In this method we take currentLocation, goal, boundaries of map and set of resource Locations as input. It generates all the neighbors one by one and checks if they fall within the map's defined boundary and they are not already occupied by resources (i.e. trees). Only a valid Stack of neighbors would be returned by the function. This function internally calls the `calculateChebyshevDistance` to assign the heuristic value to each neighbor node.

3.  **calculateChebyshevDistance():**
    This`calculateChebyshevDistance` methods calculates the chebyshev distance between 2 locations which we use as a heuristic value for the current location

    This Method is invoked in the `AstarSearch` and `expandNextAvailableValidSteps` functions to determine the `heuristicCost`.

4.  **configurePath():**
    The `configurePath` function helps in forming a path from initial location to goal by backtracking from the goal node and adding their respective coordinates to the path. The function returns a `Stack<MapLocation>` representing the path from the initial state to the destination state.

5.  **shouldReplanPath():**
    The `shouldReplanPath` function within the A\* search is responsible for determining whether the current path should be replanned or not due to presence of enemyFootman in the path.

    It evaluates the current path, represented as a stack of `MapLocation` objects, to check if the `enemyFootman` is blocking the way. If the enemy is found in the path, the function returns `true`. Otherwise, it returns `false`.

    The `shouldReplanPath` function invoked with in `middleStep` method.
