package student;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

import game.ScramState;
import game.HuntState;
import game.Edge;
import game.Explorer;
import game.Node;
import game.NodeStatus;

public class Indiana extends Explorer {

	private long startTime= 0;    // start time in milliseconds

	/** Get to the orb in as few steps as possible. Once you get there, 
	 * you must return from the function in order to pick
	 * it up. If you continue to move after finding the orb rather 
	 * than returning, it will not count.
	 * If you return from this function while not standing on top of the orb, 
	 * it will count as a failure.
	 * 
	 * There is no limit to how many steps you can take, but you will receive
	 * a score bonus multiplier for finding the orb in fewer steps.
	 * 
	 * At every step, you know only your current tile's ID and the ID of all 
	 * open neighbor tiles, as well as the distance to the orb at each of these tiles
	 * (ignoring walls and obstacles). 
	 * 
	 * In order to get information about the current state, use functions
	 * currentLocation(), neighbors(), and distanceToOrb() in HuntState.
	 * You know you are standing on the orb when distanceToOrb() is 0.
	 * 
	 * Use function moveTo(long id) in HuntState to move to a neighboring 
	 * tile by its ID. Doing this will change state to reflect your new position.
	 * 
	 * A suggested first implementation that will always find the orb, but likely won't
	 * receive a large bonus multiplier, is a depth-first search. Some
	 * modification is necessary to make the search better, in general.*/
	@Override public void huntOrb(HuntState state) {
		//TODO 1: Get the orb
		HashSet<Long> visited = new HashSet<Long>();
		dfs(state, visited);
	}


	/** Visit current location, if orb is in this location, return. If not, list 
	 * all its neighbors and sort based on Manhattan Distance. Recursively
	 * call dfs on all neighbors. If orb is not found on one path, come back to 
	 * current location and start the dfs on next neighbor.
	 */
	public void dfs(HuntState state, HashSet<Long> visited) {
		if (state.distanceToOrb() == 0) return;
		long cl = state.currentLocation();
		visited.add(cl);
		ArrayList<NodeStatus> nbs = (ArrayList<NodeStatus>) state.neighbors();
		nbs.sort(null);
		for (NodeStatus ns : nbs) {
			Long nodeid = ns.getId();
			if (!visited.contains(nodeid)) {
				state.moveTo(nodeid);
				dfs(state, visited);
				if (state.distanceToOrb() == 0) return;
				state.moveTo(cl);
			}
		}
	}




	/** 
	 * Get out the cavern before the ceiling collapses, trying to collect as much
	 * gold as possible along the way. Your solution must ALWAYS get out before time runs
	 * out, and this should be prioritized above collecting gold.
	 * 
	 * You now have access to the entire underlying graph, which can be accessed through ScramState.
	 * currentNode() and getExit() will return Node objects of interest, and getNodes()
	 * will return a collection of all nodes on the graph. 
	 * 
	 * Note that the cavern will collapse in the number of steps given by getStepsRemaining(),
	 * and for each step this number is decremented by the weight of the edge taken. You can use
	 * getStepsRemaining() to get the time still remaining, pickUpGold() to pick up any gold
	 * on your current tile (this will fail if no such gold exists), and moveTo() to move
	 * to a destination node adjacent to your current node.
	 * 
	 * You must return from this function while standing at the exit. Failing to do so before time
	 * runs out or returning from the wrong location will be considered a failed run.
	 * 
	 * You will always have enough time to escape using the shortest path from the starting
	 * position to the exit, although this will not collect much gold. For this reason, using 
	 * Dijkstra's to plot the shortest path to the exit is a good starting solution    */
	@Override public void scram(ScramState state) {
		//TODO 2: Get out of the cavern before it collapses, picking up gold along the way
		
		//Take steps towards the exit while collecting gold. Only tiles from with the shortest 
		//path to exit has length within the steps remaining will be considered. Choose the next
		//step by prioritizing tile with gold over no gold, unvisited over visited, visited less
		//times over visited more times, non-previous node over previous node.
		
		HashMap map = Paths.shortestPath(state.getExit());
		HashMap<Node, Integer> allvisited = new HashMap<Node, Integer>();
		allvisited.put(state.currentNode(), 1);
		Node previous = null;
		if (state.currentNode().getTile().gold() > 0) state.grabGold();
		//In every iteration, take the best next step
		while (state.currentNode() != state.getExit()) {
			Heap<Node> gold = new Heap<Node>();
			ArrayList<Node> unvisited = new ArrayList<Node>();
			Heap<Node> visited = new Heap<Node>();
			Node cn = state.currentNode();
			Node bn = null;
			for (Node n : cn.getNeighbors()) {
				List<Node> path = Paths.constructPath(n, map);
				Edge e = n.getEdge(state.currentNode());
				//Can get out from n at least by taking the shortest path
				if (Paths.pathDistance(path) + e.length() <= state.stepsLeft()) {
					//Sort n to 3 categories: gold - has gold; unvisited - no gold but
					//haven't been visited; visited - visited but not previous node.
					if (n.getTile().gold() > 0) gold.add(n, ((n.getTile().gold())*(-1)));
					else {
						if (allvisited.get(n) == null) unvisited.add(n);
						else {
							if (!n.equals(previous)) {
								visited.add(n, allvisited.get(n));
							}
						}
					}
				}
			}

			//choose the next step by the following priorities: 
			//has gold > no gold but not visited > visited but not previous node > previous node.
			//If the a set is empty, move to the set of the next priority.
			if (gold.size() > 0) bn = gold.poll();
			else if (unvisited.size() > 0) bn = unvisited.get(0);
			else if (visited.size() > 0) bn = visited.poll();
			else bn = previous;

			state.moveTo(bn);
			if (state.currentNode().getTile().gold() > 0) state.grabGold();
			if (allvisited.get(bn) != null) {
				Integer priority = allvisited.get(bn) +1;
				allvisited.replace(bn, priority);
			} else allvisited.put(bn, 1);
			previous = cn;
		}
		return;
	}




}
