### csds600-f23-6

# P4Agents: Automated Resource Collection

##### The P4Agent Program implements an agent(Peasant) to collect resources and deposit. The primary objective of this exercise to write a forward state space planner in strips action to collect the gold and wood from the gold mine and forest, and deposit in townhall in SEPIA using the A * algorithm.

### Code Structure: 

**PlannerAgent.java:**

- It contains the A * algorithm to find the plan that is required to reach goal state from initial state. The method AstarSearch returns a stack of actions that lead to the goal.
- The PlannerAgent class provides an implementation of a planning agent for the SEPIA environment. This agent uses A* search to find  the most optimal actions (plan) to achieve a specified goal, such as gathering a certain amount of wood or gold.

1. **`initialStep`**: This method invokes the initial step, it triggers A * Search to generate a plan and saves the plan to a text file. It then executes the plan in the PEAgent
2. **`middleStep`**: It invoked on the every subsequent game step after the initial step.
3. **`AStarSearch`**:  Implements the A* search algorithm. It searches for the best plan to achieve the goal, returning the plan as a stack of actions.
4. **`savePlan`**: Saves the generated plan to a text file. Each action is written using its `toString()` method.  
<br> 

**PEAgent.java:**

- PEAgent, which stands for Plan Execution Agent, responsible for executing the the plans provided by PlannerAgent. It contains the method createSepiaAction that takes in a StripsAction and returns a corresponding SEPIA Action.

1. **`initialStep`**: It identifies and maps the townhall and peasant units.
2.  **`middleStep`**: Executes the tasks present in the plan. It also checks if a task was completed in the previous turn before moving to the next task.
3.  **`createSepiaAction`**: Converts a Strips action into its corresponding SEPIA action.
4.  **`terminalStep`**: Executes when all tasks have been completed,and terminates the program
<br> 

**GameState.java:**

The `GameState` class is used to represent any particular state in the current problem. It also keeps track of A* specific information such as parent details and cost to reach current state. The class methods and classes used include


1. **Constructor1 - `GameState(StateView state, int playernum, int requiredGold, int requiredWood, boolean buildPeasants)`**:
    Constructs a `GameState` object from a stateview object. This constructor is used to construct initial state of the problem.
2. **Constructor2 - `GameState parent,double cost, Peasant peasant,int goldDeposited,int woodDeposited, HashMap<Integer, Resource> resources, StripsAction action)`**:
    Constructs a `GameState` object from a another parent state.
3. **Class - `Peasant`**:
    This class is used to model our peasant unit which has all the properties that are required for this problem.
4. **Class - `Resource`**:
    This class is used to model resource unit i.e gold-mine or tree.
5. **`isGoal()`**: 
    Checks if the current game state meets the goal conditions.
6. **`generateChildren()`**:
    Generates a list of all possible succesor game states of the current GameState Object.
7. **`heuristic() `**
    Returns a value that is a estimate on the remaining cost to reach goal state from current state.
8. **`hashCode() `**
    Used to compute hashcode of the current GameState based on feature like amount of resources being carried by peasant, peasant's position and resources at townhall.
<br>

**StripsAction.java** 

The `StripsAction` interface is used to define the common structure for all the possible STRIPSActions

1. **`preconditionsMet(GameState state)`**:
   Evaluates whether the game state meets the necessary preconditions for the specific StripsAction.

2. **`apply(GameState state)`**:
   Applies the action instance to the given GameState producing a new GameState in the process.

3. **`createSepiaAction()`**:
   Converts STRIPS actions into SEPIA actions for SEPIA to execute.
<br>


**Harvest.java** 

The Harvest class implements the StripsAction interface to define the harvesting action by a peasant unit within a game. It handles the specifics of resource gathering and manipulates the game state accordingly. The class methods include:

1. **`Constructor - Harvest(Peasant peasant, int id, Position position, boolean isGold)`**:
   Constructs a Harvest object which is instantiated when a peasant is ready to harvest a resource.
2. **`preconditionsMet(GameState state)`**:
   Evaluates whether the game state meets the necessary preconditions for the harvest action to be executed.
3. **`apply(GameState state)`**:
   Executes the harvest action, updating the game state with the new resource count, modifying the associated action cost, etc.
4. **`createSepiaAction()`**:
   Converts STRIPS actions into SEPIA actions for execution. This method is invoked by PEAgent after the STRIPS plan has been generated.
5. **`toString()`**:
   Returns a string representation of the harvest action detailing the resource type, peasant ID, and the location of the resource.
<br> 

**Deposit.java**

The Deposit class, implements the StripsAction interface and is used for defining the deposit action within a game, where a peasant is responsible for depositing collected resources at the townhall. Below are the key methods of the Deposit class:
1.  **`Constructor - Deposit(Peasant peasant, Position t)`**: Constructs a Deposit object which is instantiated when a peasant is ready to deposit a resource.
2.  **`preconditionsMet(GameState state)`**:
   This method validates the preconditions for the deposit action. It ensures that the peasant is carrying a resource and is positioned next to the townhall before executing Deposit action.
3. **`apply(GameState state)`**:
   On successful precondition checks, this method is called to execute the deposit action. It updates the game state to reflect that the peasant has deposited the carried resource, which involves setting the peasant's resource-carrying status to none and possibly updating the townhall's resource storage.
4. **`createSepiaAction()`**:
   Converts the deposit STRIPS action into a SEPIA action, for execution. This method is invoked by PEAgent after the STRIPS plan has been generated.
5. **`toString()`**:
   Provides a string representation of the deposit action, detailing the peasant ID, and the location of the townhall.
<br> 

**MoveTo.java**

The MoveTo class is another implementation of the StripsAction interface within the STRIPS planning framework. It defines the moveTo action for a peasant to navigate to a specified location in the gameState. Below is a summary of its methods and their functionalities:

1. **`Constructor - MoveTo(Position position, Peasant peasant)`**:
   Creates a new MoveTo action instance which sets up the required parameters for moving a peasant to a target position.
2. **`preconditionsMet(GameState state)`**:
   This method checks if the preconditions are met for the execution of the MoveTo action. This usually includes checks like whether the path is clear and the peasant is not already at the specified position.
3. **`apply(GameState state)`**:
   Upon satisfying the preconditions, this method is called to apply the MoveTo action. It updates the game state to update the peasant's location and adjusts the overall action cost based on the movement.
4. **`createSepiaAction()`**:
   This method is used when the STRIPS planning algorithm has determined a plan, and the STRIPS actions need to be translated into Sepia actions so that the game engine can execute them. Here, a MoveTo action is created.
5. **`toString()`**:
   Generates a String representation of the MoveTo action, with the peasant ID, peasant's target position and other relevant details.

