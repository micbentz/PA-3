import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by Michael on 4/6/17.
 *
 * <code>linkstate</code> is an implementation of Dijkstra's algorithm.
 * Given knowledge of the entire network, linkstate will determine the shortest path
 * from source to destination.
 */
class linkstate {
	private static List<String> nodeList;					// Holds the contents from network.txt for each node
	private static PriorityQueue<Node> nodePriorityQueue;	// Min-Heap to get the node with minimum cost
	private static List<Node> network;						// All nodes in the network
	private static String dashes = "";
	private static String commaAdjustedTab = "";			// Space offset for the header between N' and D(n)
	private static String traversedNodeString = "";			// Space offset for the step between N' and D(n)

	public static void main(String[] args){
		try{
			// Read in the file
			File inputFile = new File(args[0]);
			// Parse through the file and get the node contents
			readFile(inputFile);
			// Create the network
			createNetwork();
			// Find the shortest path
			findShortestPath();
		}catch(IllegalArgumentException e){
			System.out.println("Illegal arguments used.");
			e.printStackTrace();
		}catch(FileNotFoundException e){
			System.out.println("File not found.");
		}
	}

	/*
	 * Read through the file and get the contents for each node
	 */
	private static void readFile(File inputFile) throws FileNotFoundException{
		Scanner fileScanner = new Scanner(inputFile);
		nodeList = new ArrayList<>();

		// Read in the contents of each node (identifier to neighboring nodes)
		while(fileScanner.hasNextLine()){
			// Each line in the text file represents a node
			String nodeContent = fileScanner.nextLine();
			// Remove the "."
			nodeContent = nodeContent.substring(0,nodeContent.length()-1);
			// If EOF is reached then terminate
			if (nodeContent.equals("EOF.")) break;
			nodeList.add(nodeContent);
		}

	}

	/*
	 * Iterate through the contents of each node and create the network and attach the neighbors
	 */
	private static void createNetwork(){
		network = new ArrayList<>();
		nodePriorityQueue = new PriorityQueue<>();

		// Create all the nodes in the network
		for(int nodeNum = 1; nodeNum < nodeList.size(); nodeNum++){
			// Create node with identifier
			Node node = new Node(nodeNum);
			// Add the node to the network
			network.add(node);
			// Add the node to the priority queue
			nodePriorityQueue.add(node);
		}

		// Go through and attach neighbors
		for(int nodeNum = 1; nodeNum < nodeList.size(); nodeNum++){
			// Get the contents of the current node
			String nodeContents = nodeList.get(nodeNum -1);
			// Get the values of the cost to each neighbor
			String[] tokens = nodeContents.split(",");
			// Get the node from the network
			Node node = network.get(nodeNum - 1);

			int count = 0;
			// Iterate through the path costs of each node in the network
			for(String cost: tokens){
				// If the cost is 0 then it is the source node
				// If the cost is N (infinity) then that node is not a neighbor
				// Continue until a neighbor is reached
				if(cost.equals("0") || cost.equals("N")){
					// Do Nothing
				}else{
					// Get the node from the network
					Node neighbor = network.get(count);
					// Add the neighbor to the source node
					node.addNeighbor(neighbor,Integer.parseInt(cost));
				}
				count++;
			}
		}

		// Get the largest the N' String can be
		int maxStringSize = 0;
		for(Node n: network){
			// Account for single and double digits
			maxStringSize += (n.getIdentifier() / 10 == 0) ? 1: 2;
		}

		// Get the maximum amount of commas possible
		int maxCommaCount = network.size() - 1;
		// Get the maximum string size of N'
		int maxNetworkStringSize = maxStringSize + maxCommaCount;

		// Create a string with the maximum size of N'
		for(int i = 0; i <= maxNetworkStringSize; i++){
			traversedNodeString += " ";
		}

		// Create spacing equal to maximum N'
		for(int i = 0; i <= maxNetworkStringSize; i++){
			commaAdjustedTab += " ";
		}
		commaAdjustedTab += "\t";
	}

	// Finds the shortest path;
	private static void findShortestPath(){
		// N'. Contains the traversed nodes
		String traversedNodes = "";
		int stepNum = 0;

		printHeader();

		// Continue until the priority queue is empty
		while(!nodePriorityQueue.isEmpty()){

			// Get the node with the minimum D(v)
			Node source = nodePriorityQueue.remove();

			// Mark the node as traversed and add to N'
			source.traverse();

			// Add the necessary comma
			String comma = (stepNum != network.size()) && (stepNum!= 0) ?  "," : "";
			traversedNodes += comma + source.getIdentifier();

			// Get the neighbors of the node
			List<Node> neighbors = source.getNeighborList();
			for(Node neighbor: neighbors){
				// D(V) = min(D(v), D(w) + c(w,v)
				// The new distance is the minimum between the least cost path of the neighbor(v) AND
				// The sum of the least cost path of the source(w) and it's cost to the neighbor(v)
				int newDistance = source.getLeastCostPath() + source.getCostToNeighbor(neighbor);
				if (newDistance < neighbor.getLeastCostPath()){
					neighbor.updateLeastCostPath(newDistance);
					neighbor.setPreviousNode(source);
				}

				// Removing and adding the updates the indexing of the priority queue
				if (!neighbor.isTraversed()) {
					nodePriorityQueue.remove(neighbor);
					nodePriorityQueue.add(neighbor);
				}

				// If the neighbor was previously traversed then remove it
				if (neighbor.isTraversed()){
					nodePriorityQueue.remove(neighbor);
				}
			}

			// Insert the traversed nodes into a new string with the same size of the maximum N'
			// This is necessary to keep the spacing the same between N' and D(n)
			String updatedTraversedNodes = traversedNodes + traversedNodeString.substring(traversedNodes.length(),traversedNodeString.length());


			// Print the step taken
			printStep(stepNum,updatedTraversedNodes);
			stepNum++;
		}
	}

	// Prints the header with all the column information
	private static void printHeader(){
		String header = "Step" + "\t" + "N'" + commaAdjustedTab;
		Node node = null;
		int identifier = 0;
		int tabCount = 0;

		// Prints out the header presented in the format: D(identifier),p(identifier)
		for(int i = 1; i < network.size(); i++){
			node = network.get(i);
			identifier = node.getIdentifier();
			header += "D(" + identifier + "),p(" + identifier + ")" + "\t";
			dashes += "--------";
		}

		// Create dashed line to separate each step
		dashes = header.replaceAll("(?s).", "-");
		for(int i = 0; i < header.length(); i++){
			if (header.charAt(i) == '\t'){
				tabCount++;
			}
		}

		// Account for '\t' character
		for (int i = 0; i < tabCount; i++){
			dashes += "-----";
		}

		System.out.println(dashes);
		System.out.println(header);
	}

	// Prints out the traversed nodes, least cost and previous nodes for each node
	private static void printStep(int stepNum, String traversedNodes){
		String nodeInfo = "";
		String previous = "";
		String cost = "";
		// Adjusted tab to account for the size of the header column: D(#),p(#)
		String adjustedTab = "         " + "\t";

		// Step		N'
		nodeInfo = Integer.toString(stepNum) + "\t" + traversedNodes + "\t";

		// Iterate through the network from 2 - N
		for(int i = 1; i < network.size(); i++){
			// Get the node
			Node n = network.get(i);
			// Only concat info IFF the node hasn't been traversed
			cost = (n.isTraversed()) ? "" :
					(n.getLeastCostPath() == Integer.MAX_VALUE) ? "\u221e" : Integer.toString(n.getLeastCostPath());
			previous = (n.isTraversed()) ? "" :
					(n.getPrevious() == null) ? "" : "," + Integer.toString(n.previous.getIdentifier());
			nodeInfo += (n.isTraversed()) ?
					adjustedTab : cost + previous + adjustedTab ;
		}

		System.out.println(dashes);
		System.out.println(nodeInfo);
	}


	/*
	 * The Node class represents a node in a network.
	 * Every node has an Identifier #.
	 * IF it has neighbors then the node also has: least cost path, previous node
	 * and a list of neighbors
	 */
	private static class Node implements Comparable<Node>{
		private Integer identifier;							// Identifier of the node, i.e. unique num value
		private HashMap<Node,Integer> neighborCostMap;		// Mapping that provides the cost of an edge between neighbors
		private List<Node> neighborList;					// List of neighbors
		private Integer leastCostPath;						// D(v) the least cost to the source node
		private Node previous;								// The previous neighbor to which it has a minimal D(v)
		private boolean traversed = false;

		Node(int identifier){
			this.identifier = identifier;
			// Set the least cost path of the root to 0
			if (identifier == 1){
				this.leastCostPath = 0;
			}else{
				this.leastCostPath = Integer.MAX_VALUE;
			}
			this.neighborCostMap = new HashMap<>();
			this.neighborList = new ArrayList<>();
		}

		/**
		 * Returns the unique identfier of the node. Used for
		 * printing purposes
		 * @return unique int identifier
		 */
		int getIdentifier(){
			return this.identifier;
		}

		/**
		 * Gets the identifier associated with the neighboring Node
		 * @param neighbor the node to get the identifier of
		 * @return the identifier of the path to the associated Node
		 */
		int getCostToNeighbor(Node neighbor){
			return neighborCostMap.get(neighbor);
		}

		/**
		 * Returns list of neighbors. Used when iterating through
		 * the neighbors of the node
		 * @return the list of neighbors of the node
		 */
		List<Node> getNeighborList(){
			return this.neighborList;
		}

		/**
		 * Adds a neighbor to the node with the associated cost of the path
		 * @param neighbor The neighbor to be added to the node
		 * @param cost	The cost of the path from the node to the neighbor
		 */
		void addNeighbor(Node neighbor, int cost){
			neighborCostMap.put(neighbor, cost);
			neighborList.add(neighbor);
		}

		/**
		 * Updates the D(v) with the given cost
		 * @param cost the new D(v) of the node
		 */
		void updateLeastCostPath(int cost){
			this.leastCostPath = cost;
		}

		/**
		 * Returns the D(v). Used when determining the next
		 * node to traverse
		 * @return
		 */
		Integer getLeastCostPath(){
			return this.leastCostPath;
		}

		/**
		 * Returns the previous node. Used for printing purposes
		 * @return the previous node
		 */
		Node getPrevious(){
			return this.previous;
		}

		/**
		 * Sets the previous node which provided a minimal D(v)
		 * @param node the previous node
		 */
		void setPreviousNode(Node node){
			this.previous = node;
		}

		/**
		 * Sets the node as traversed
		 */
		void traverse(){
			this.traversed = true;
		}

		/**
		 * Checks if the node has already been traversed
		 * @return true if node is traverse, else false
		 */
		boolean isTraversed(){
			return this.traversed;
		}

		@Override
		public int compareTo(Node neighbor) {
			// compareTo should return < 0 if this is supposed to be
			// less than other, > 0 if this is supposed to be greater than
			// other and 0 if they are supposed to be equal

			// Compare the least cost path
			int leastCostPath = getLeastCostPath().compareTo(neighbor.leastCostPath);
			if (leastCostPath != 0) return leastCostPath;

			// If D(v) is equal then compare the identfiers
			return this.identifier.compareTo(neighbor.getIdentifier());
		}
	}
}