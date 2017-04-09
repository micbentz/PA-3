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
	static File inputFile;
	static List<String> nodeList;
	static PriorityQueue<Node> nodePriorityQueue;
	static List<Node> network;
	static String dashes = "";
	static String adjustedNodeSizeTab = "";

	public static void main(String[] args){
		try{
			// Read in the file
			inputFile = new File(args[0]);
			// Parse through the file and create the network
			readFile(inputFile);

//			int nodeNum = 1;
//			for(String s: nodeList){
//				System.out.println(nodeNum++ + " " + s);
//			}

			// Get the adjusted spaces needed
			for(int i = 0; i < nodeList.size(); i++){
				adjustedNodeSizeTab += " ";
			}

			createNetwork();

//			printHeader();

			findShortestPath();

		}catch(IllegalArgumentException e){
			System.out.println("Illegal arguments used.");
			e.printStackTrace();
		}catch(FileNotFoundException e){
			System.out.println("File not found.");
		}
	}

	private static void readFile(File inputFile) throws FileNotFoundException{
		Scanner fileScanner = new Scanner(inputFile);
		nodeList = new ArrayList<>();
		network = new ArrayList<>();
		nodePriorityQueue = new PriorityQueue<>();

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

	private static void createNetwork(){
		// Create all the nodes in the network
		for(int nodeNum = 1; nodeNum < nodeList.size(); nodeNum++){
			Node node = new Node(nodeNum);
			network.add(node);
			nodePriorityQueue.add(node);
		}

		String tokens[] = null;
		Node node = null;
		// Go through and attach neighbors
		for(int nodeNum = 1; nodeNum < nodeList.size(); nodeNum++){
			// Get the contents of the current node
			String nodeContents = nodeList.get(nodeNum -1);
			// Get the values of the cost to each neighbor
			tokens = nodeContents.split(",");
			// Get the node from the network
			node = network.get(nodeNum - 1);

			int count = 0;
			// Iterate through the costs of each node in the network
			// and determine if the node is a direct neighbor of the node
			// and add the node's neighbor
			for(String cost: tokens){
				// If the cost is 0 then it is the source node
				// If the cost is N then that node is not a neighbor
				// Continue until a neighbor is reached
				if(cost.equals("0") || cost.equals("N")){
					// Do Nothing
				}else{
					Node neighbor = network.get(count);
					node.addNeighbor(neighbor,Integer.parseInt(cost));
				}
				count++;
			}
		}
	}

	private static void findShortestPath(){
		int stepNum = 0;
		printHeader();

		String traversedNodes = "";


		while(!nodePriorityQueue.isEmpty()){

			Node source = nodePriorityQueue.remove();
//			System.out.println("SOURCE> " + source.toString());
			source.remove();
//			network.remove(source);
			traversedNodes += source.getIdentifier();

			List<Node> neighbors = source.getNeighborList();
			// For all neighbors

			for(Node neighbor: neighbors){

				// D(V) = min(D(v), D(w) + c(w,v)
				// The new distance is the minimum between the least cost path of the neighbor(v) AND
				// The sum of the least cost path of the source(w) and it's cost to the neighbor(v)
				int newDistance = source.getLeastCostPath() + source.getCostToNeighbor(neighbor);
				if (newDistance < neighbor.getLeastCostPath()){
//				if (newDistance < source.getCostToNeighbor(neighbor)){
					neighbor.updateLeastCostPath(newDistance);
					neighbor.setPreviousNode(source);
				}
				// Need to remove and add neighbor to update the indexing of the priority queue
				nodePriorityQueue.remove(neighbor);
				nodePriorityQueue.add(neighbor);


				if (neighbor.isRemoved()){
					nodePriorityQueue.remove(neighbor);
				}
//				// Check if the node has already been traversed
//				if (!network.contains(neighbor)){
//					nodePriorityQueue.remove(neighbor);
//				}
//				System.out.println("NEIGHBOR> " + neighbor.toString());


			}
			printStep(stepNum,traversedNodes);
			stepNum++;
		}
	}

	// Prints the header with all the column information
	private static void printHeader(){
		String header = "Step" + "\t" + "N'" + "\t";
		Node node = null;
		int identifier = 0;
		int tabCount = 0;

		for(int i = 1; i < network.size(); i++){
			node = network.get(i);
			identifier = node.getIdentifier();
			header += "D(" + identifier + "),p(" + identifier + ")" + "\t";
			dashes += "--------";
		}

		dashes = header.replaceAll("(?s).", "-");
		for(int i = 0; i < header.length(); i++){
			if (header.charAt(i) == '\t'){
				tabCount++;
			}
		}

		for (int i = 0; i < tabCount; i++){
			dashes += "-----";
		}

		System.out.println(dashes);
		System.out.println(header);
	}


	private static void printStep(int count, String traversedNodes){
		String nodeInfo = "";
		String previous = "";
		String cost = "";
		String adjustedTab = "         " + "\t";

		nodeInfo += Integer.toString(count) + "\t" + traversedNodes + "\t";

		for(int i = 1; i < network.size(); i++){
			Node n = network.get(i);
			cost = (n.isRemoved()) ? "" :
					(n.getLeastCostPath() == Integer.MAX_VALUE) ? "\u221e" : Integer.toString(n.getLeastCostPath());
			previous = (n.isRemoved()) ? "" :
					(n.getPrevious() == null) ? "" : "," + Integer.toString(n.previous.getIdentifier());
			nodeInfo += (n.isRemoved()) ? adjustedTab : cost + previous + adjustedTab;
		}

		System.out.println(dashes);
		System.out.println(nodeInfo);
	}


	private static class Node implements Comparable<Node>{
		private Integer identifier;
		private Node previous;
		private Integer leastCostPath;
		private HashMap<Node,Integer> neighborCostMap;
		private List<Node> neighborList;
		private String neighbors;
		private boolean removed = false;

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
			this.neighbors = "NEIGHBORS OF: " + identifier + "\n" ;
		}

		/**
		 * Adds a neighbor to the node with the associated cost of the path
		 * @param neighbor The neighbor to be added to the node
		 * @param cost	The cost of the path from the node to the neighbor
		 */
		void addNeighbor(Node neighbor, int cost){
			neighborCostMap.put(neighbor, cost);
			neighborList.add(neighbor);
			neighbors += "Node: " + neighbor.getIdentifier() + " Cost: " + neighborCostMap.get(neighbor) + "\n";
		}

		/**
		 * Gets the identifier associated with the neighboring Node
		 * @param neighbor the node to get the identifier of
		 * @return the identifier of the path to the associated Node
		 */
		int getCostToNeighbor(Node neighbor){
			return neighborCostMap.get(neighbor);
		}


		void updateLeastCostPath(int cost){
			this.leastCostPath = cost;
			neighbors += "Least cost: " + leastCostPath;
		}

		void setPreviousNode(Node node){
			this.previous = node;
			neighbors += " Previous node: " + previous.getIdentifier() + "\n";
		}

		List<Node> getNeighborList(){
			return this.neighborList;
		}

		// Returns the identifier of the path
		int getIdentifier(){
			return this.identifier;
		}

		Integer getLeastCostPath(){
			return this.leastCostPath;
		}

		Node getPrevious(){
			return this.previous;
		}

		void remove(){
			this.removed = true;
		}

		boolean isRemoved(){
			return this.removed;
		}

		@Override
		public String toString(){
			return this.neighbors;
		}

		@Override
		public int compareTo(Node neighbor) {
			// compareTo should return < 0 if this is supposed to be
			// less than other, > 0 if this is supposed to be greater than
			// other and 0 if they are supposed to be equal
//			int edgeCost = Integer.compare(this.ne)
//
			int leastCostPath = getLeastCostPath().compareTo(neighbor.leastCostPath);
			if (leastCostPath != 0) return leastCostPath;
//
//
			int identifier = this.identifier.compareTo(neighbor.getIdentifier());

//			System.out.println("IDENTIFIER COMPARISON: " + identifier);
			return this.identifier.compareTo(neighbor.getIdentifier());

//			return this.leastCostPath.compareTo(neighbor.getLeastCostPath());
		}
	}
}