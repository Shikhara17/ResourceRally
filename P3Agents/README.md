csds600-f23-6

## P3Agents: Playing Against an Opponent

The P3Agent Program implements an intelligent agent to play a strategic two-player game. The primary objective is to create an agent that can effectively control Footmen to eliminate enemy Archers in various game scenarios. The agent will utilize the Minimax algorithm enhanced with Alpha-Beta pruning to navigate the game tree.

### Code Strucuture:

- `MinimaxAlphaBeta` : The main class where the alpha-beta search algorithm resides.
- `GameState` : A Helper class that handles the game state. It's used to calculate utilities, derive child states and generate possible actions for agent.
- `AstarAgent` : This class is used to find optimal path from footman to opponent.
- `PlayableUnit` : This class is used to model playable units in game such as footmen and archers.

### `MinimaxAlphaBeta.java` Functionality's:

1. **alphaBetaSearch** : This method is the entry point for the alpha-beta search algorithm. It explores possible children and game states up to a certain depth and uses alpha-beta pruning to ignore suboptimal nodes.
2. **MaxValue and MinValue** : These methods recursively calculates the maximum and minimum utility from the current game state, executing the optimality for both the maximizing and minimizing player respectively.
3. **orderChildrenWithHeuristics** : This method sorts the children nodes based on heuristics, Due to the extensive size of the game tree,the agent will use heuristics to prioritize the expansion of the most promising nodes which helps in improving the efficiency of the alpha-beta pruning by examining maximum/minimum nodes first.

### `GameState.java` Functionality's:

1. **GameState(State.StateView stateView):**
   The `GameState` constructor initializes key components for managing the state of a game. This comstructor initializes the movers and opponents lists, extracts unit information from the stateView object, Sets xExtent and yExtent to represent the dimensions of the game map. It also creates a set of obstacles based on the resource nodes from the stateView. It Initializes an AstarAgent and defines a list of valid directions to use in the game.
2. **public GameState(GameState previousGameState)**
   The `GameState` constructor in the code initializes the current GameState based from another GameState object. This constructor keeps track of the player's turns by incrementing it by one. This constructor has the logic of taking turns between players,i.e., movers and opponents.
3. **extractUnitInformation(State.StateView stateView**:
   The `extractUnitInformation` is used to retrieve footmen and archer units by using the information provided in the `StateView` object. This internally calls `extractUnitsFromUnitIds` to fetch the next state unit.
4. **extractUnitsFromUnitIds(List<Integer> unitIds,State.StateView stateView)**:
   The `extractUnitsFromUnitIds`, is called within the `extractUnitInformation` method to retrieve units based on their unitIds using the data provided by the `StateView` object. It creates a list of `PlayableUnit` objects, each representing a unit, by fetching unit information from the `StateView` based on the provided unitIds.
5. **getUtility():**
   The `getUtility` method calculates a utility value for the game state, considering the health of footmen and attackers, and the total distance to opponents. It adjusts calculations based on whether it's an even or odd turn, to view the utility of the respective player.
   The utility is higher when footmen have more health, and it decreases with greater distance to opponents and higher opponent health. The formula used to compute utility is: `5 * (Footmen Health) - 10 * (Opponents Health) - 15 * (Total Distance to Opponents)`.
6. **calculateTotalDistance(List<PlayableUnit> footmen, List<PlayableUnit> archers):**
   The `calculateTotalDistance` method is used to calculate the cumulative distance between each footman and their closest archer in a game. The method iterates through each footman in the footmen list and for each footman, it finds the closest archer in the archers list using the `getClosestUnit` method. If there are obstacles on the map, it uses the A* algorithm to find the path from the footman to the closest archer and then the length of the path is added to the *totalDistance. If there are no obstacles on the map or if the footman is adjacent to the archer, it calculates the distance using the Manhattan distance, and this is also added to the \*_totalDistance_. The total cumulative distance between all footmen and their closest archers is returned
7. **distanceToEnemy(PlayableUnit unit1, PlayableUnit unit2):**
   The `distanceToEnemy` method is used to calculate the Manhattan distance which is the sum of the absolute differences between their X-coordinates and Y-coordinates between two game units objects, and returns it.

8. **getChildren():**
   The `getChildren` method is used for creating a list of child game states. It fetches all the available actions for the **first mover** and then checks if the **second mover** is still alive. If the second mover is still alive, this method generates all the possible actions for the second mover too. It iterates through the list of actions of the **first** and the **second** mover. For each combination of actions, it generates a child game state using the createChildState function using `createChildState` method. The resulting child game states represent different possibilities based on the actions of the movers. This method calls the following methods,

   1. **getActions**: Generates a list of possible attack and move actions for a unit.
   2. **isValidMove**: Validates whether a map path is accessible.
   3. **createChildState**: This method generates a Child, where new game states are generated based on possible actions from the current state.

9. **createChildState(int id1, Action action1, int id2, Action action2):**
   The `createChildState` method generates the next child game state by performing actions on the current state. It uses a HashMap to store all the actions for both the **mover one** and **mover two**. A new game state is then created based on the current state, by performing available actions using the `performActions` method on the current state. The method returns a GameStateChild object representing the actions and the resulting child state.
10. **performActions(Map<Integer, Action> actions):**
    The `performActions` method is used to apply actions to the current game state. If the action is of type **PRIMITIVEATTACK**, it decrements the target unit's health after the attack, or if the action is of type **PRIMITIVEMOVE**, it updates the coordinates of the respective mover to reflect their new position based on the chosen direction. This method updates the game state in response to the actions taken by the game's units, including attacks and movements.
11. **fetchUnitById(int id)**:
    The `fetchUnitById` method is designed to get a game unit, be it a footman or an archer, by using its unique ID in the game. It searches for the unit within both the movers and opponents. If the unit with the specified ID is found, it is returned, else, null is returned.
12. **getActions(PlayableUnit mover, List<PlayableUnit> opponentsList):**
    The `getActions` is used to determine and collect all available actions for a specific mover, taking into account a list of opponent units. The method calculates the closest enemy unit using `getClosestUnit` method and generates an attack action if the opponent is within range using an internal state method `createPrimitiveAttack`. For a footman on maps with obstacles, it uses the `A* algorithm` to find the optimal path to the nearest enemy, and in other cases such as if obstacles are absent or if it's the archer's turn, it generates all possible movement actions in all valid directions.
13. **getDirectionWithCoordinates(PlayableUnit unit, MapLocation nextCoord):**
    The `getDirectionWithCoordinates` method is used to convert coordinates provided by the **A \* pathfinding** method into the directional movement. It calculates the differences between the current unit's position and the target coordinates to determine the correct direction. Based on the differences in the X and Y coordinates, it maps the movement direction, such as **North**, **South**, **East**, or **West**. The method returns the corresponding direction to implement a movement towards the target coordinates.
14. **isOpponentInRange(PlayableUnit unit1, PlayableUnit unit2):**
    The method `isOpponentInRange` calculates the distance between two units, the player unit and the opponent unit. It then checks if the distance between these units is less than or equal to the attacking range of the player unit. Returns _true_ if the opponent unit is within the attacking range of the player unit, returns _false_ if its not in the range.
15. **isValidMove(int x, int y, List<PlayableUnit> gameUnits, Set<ResourceNode.ResourceView> obstacles):**
    The method `isValidMove` is used to determine if the next move is valid or not. The validity of the move is based three conditions. Firstly, if the coordinates fall within the map extent, Secondly, if they collide with existing units and Finally, whether they occupy a tree or obstacle on the map. Returns _true_ if the specified move is valid according to the conditions mentioned above, returns _false_ if the conditions donot satisfy.
16. **getClosestUnit(PlayableUnit unit,List<PlayableUnit> opponents):**
    The Java method `getClosestUnit` is used to find the closest opponent in a playable unit by calculating the manhattan distance. The Manhattan distance between two points (x1, y1) and (x2, y2) is calculated as `|x1 - x2| + |y1 - y2|`. The method iterates through the list of opponents, calculates the Manhattan distance between the current unit and opponent unit, to find the closest opponent and returns the closest unit.
