package com.uom.georgevio;

import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.graphstream.graph.Edge;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.omg.CORBA.Current;

import com.fazecast.jSerialComm.SerialPort;

public class ClientHelper {

	private String ipServer = null; /* hardwire the sink's IP if not found */
	
	SerialPort motePort = null; /* will be set by the Client class */
	private static final long appTimeStarted = System.currentTimeMillis(); /* Time this Application first started */
	private long timeReprobeEdgesCounter = System.currentTimeMillis(); /* ??? */
	
	GraphStyling graphstyling = new GraphStyling();

	private Graph graph = graphstyling.returnGraph();
	
	SpanningTree spanningtree = new SpanningTree(graph); /* graph exists now */
	
	ClusterMonitor clustermonitor = new ClusterMonitor();
	
	Send2Serial send2serial; 
	
	DecimalFormat df = new DecimalFormat();
		
	public ClientHelper() { /* constructor */

	}
	
/***************************************************************************/
	public void removeGraph() {
		graphstyling.removeView();
	}
/***************************************************************************/	
	public void setMotePort(SerialPort motePort) { /* port will be set by the Client class */
		this.motePort = motePort;
		debug("ClientHelper class: Serial port set from Client class...");
		send2serial = new Send2Serial(motePort); /* serial port is set now */
	}
/***************************************************************************/
	public void sendMsg2Serial(String message) {
		send2serial.sendSpecificMessage(message);
	}
/***************************************************************************/	
	public boolean getInDegrees(int roundsCounter) {

		Stream<Node> nodesStr = graph.nodes();
		Iterable<Node> nodes = nodesStr::iterator; 
		
		for (Node node : nodes) {
			/* Orphan node, probe it again. This will happen rarely */
			if(node.getInDegree()== 0 && !(node.toString()).equals(ipServer)){
				debug("R:"+roundsCounter+" Sneacky node with no incoming edge is "+IPlastHex(node.toString()));
				String message = "SP:"+node.toString()+"\n";
				send2serial.sendSpecificMessage(message);
				return true; /* found a sneaky hiding node and probed it */
			}//if
		}//while
		return false; /* no one has zero incoming degree, If you are missing nodes, try next to probe for neighbors */
	}
/***************************************************************************/		
	public void probeForHiddenEdges(int roundsCounter){
		int nodesNum = graph.getNodeCount();
		int edgesNum = graph.getEdgeCount();
		
		/* Initial delay ??? mins, every ??? mins, hence, after the network has "settled" down */
		if (System.currentTimeMillis()  > appTimeStarted + Main.keepAliveNodeBound && 
				System.currentTimeMillis() > timeReprobeEdgesCounter+ Main.keepAliveNodeBound / 2 ) {

			timeReprobeEdgesCounter = System.currentTimeMillis();
			
			if(nodesNum != edgesNum+1) { /* If nodes<>edges+1, there is missing information */
				debug("R:"+roundsCounter+" ALERT: nodes "+nodesNum+" <> (edges+1) "+(edgesNum+1));
				
				/* if there is no node with zero inDegree, last resort, probe for neighbors */
				if(!getInDegrees(roundsCounter)) { 
	
					Stream<Node> nodesStr = graph.nodes();
					Iterable<Node> nodes = nodesStr::iterator; 
					send2serial.probeNeighbors(nodes);					
				}

			} else { /* Check the Spanning Tree JUST IN CASE: There could be loops! */
				//TODO: Make the spanning tree run less often (timer?)
				List<Edge> edgesSpan = spanningtree.spanTreeKruskal();
				if(!edgesSpan.isEmpty()) {
					for (Edge edge : edgesSpan) {
						Main.debug("Loose node(s) found! Taking actions...\n");
						/* Edge will be removed, node will be forced to tell the truth */
						removeEdgeifExists(edge);
					}
				}
			}
		}
	}
/***************************************************************************/	
	public void printEdgesInfo(int roundsCounter){
		int nodesNum = graph.getNodeCount();
		int edgesNum = graph.getEdgeCount();
		int inDegree = 0;
		int outDegree = 0;
		int totalOutDegrees = 0;
		int UDPRecv = 0;
		df.setMaximumFractionDigits(2);
		
		/* If it is smaller than a given threshold, the node is still alive.
		 * Else, the node is long gone, remove it.
		 */
		long keepAliveTimer;
		
		/* Print every six rounds, AFTER a "grace" period. At the beginning
		 * the network is still converging.
		 */	
		if(roundsCounter%6 == 0 && appTimeStarted > 2*Main.keepAliveNodeBound) { 
			
			debug("R:"+roundsCounter+" -------Nodes - I/O Edges---------------");

			/* Also can get data from Stream<Nodes> */
			List<Node> nodes = graph.nodes()
					.filter(node -> node.getId() != ipServer) /* Sink should not be probed */
				    .collect(Collectors.toList());

			for (Node node : nodes) {

				inDegree = node.getInDegree();
				outDegree = node.getOutDegree();
				totalOutDegrees+=outDegree;
				int shortNodeName = IPlastHex(node.toString());
				keepAliveTimer = System.currentTimeMillis() - (long) node.getAttribute("keepAliveTimer");;
				float secs = keepAliveTimer / 1000F; /* seconds */
				try {
					UDPRecv = (int) node.getAttribute("UDPRecv");
				}catch (NullPointerException e) {
					debug("Node "+IPlastHex(node.toString())+", UDPRecv doesnot exist");
				}
				debug(" Node: "+shortNodeName+"\tInEdges: "+inDegree+
							"\tOutEdges: "+outDegree+",\tLast seen: "+
							df.format(secs)+"sec ago. UDPRecv "+UDPRecv); 
								
				if (keepAliveTimer < Main.keepAliveNodeBound) { 
					/* Node reappears, or appears for first time */
					if((boolean) node.getAttribute("parentOnly")) {							 
						graphstyling.nodeColorAlien(node);
					} else {
						graphstyling.nodeStyle(node);
					}
				}	
				
				else if (keepAliveTimer > Main.keepAliveNodeBound &&  
					keepAliveTimer < Main.grayZoneNodeBound ) { 
					debug("Node "+shortNodeName+" is very quiet lately...");
					
					if((boolean) node.getAttribute("parentOnly")) {							 
						graphstyling.nodeColorAlien(node);
					} else {
						graphstyling.nodeColorGrey(node);
					}
				}				
				
				else if (keepAliveTimer > Main.keepAliveNodeBound ) { 
					if((boolean) node.getAttribute("parentOnly")) {							 
						//TODO: Do something about nodes as parents only
					} else {
						debug("Node "+shortNodeName+" is long gone... Removing it");
						graph.removeNode(node);
					}						
				}
			
			}
			debug("------Current Nodes:"+nodesNum+"------Current Edges:"+edgesNum+"-------------\n\n");

			Main.nodesOutput((Integer.toString(nodesNum)));
			Main.edgesOutput((Integer.toString(edgesNum)));
			Main.totalOutDegreesOutput((Integer.toString(totalOutDegrees)));
		}   
	}
/***************************************************************************/		
	public void runKMeans(int clusters) {

		List<Node> nodes = graph.nodes()
				.filter(node -> node.getId() != ipServer) 
			    .collect(Collectors.toList());
		//if( System.currentTimeMillis() > appTimeStarted + Main.keepAliveNodeBound ) {
			debug("Time to start kmeans...............................");
			try {
				clustermonitor.kMeans(clusters, nodes);
			} catch (Exception e) {
				debug(e.toString());
			}
		//}
	}	
/***************************************************************************/	
	/* Print all nodes in the graph */
	public void printAllNodes() {
		int nodesCount=0;
		
		Stream<Node> nodesStr = graph.nodes();
		Iterable<Node> nodes = nodesStr::iterator; 
		
		debug("---------Graph Nodes--------");
		for (Node node : nodes) {
			nodesCount++;
			debug(nodesCount+"\t"+node.toString()+"\t"+IPlastHex(node.toString()));
		}
		debug("---------------------------\n");
	}
/***************************************************************************/	
	public void setSink(String ipServer) { /* Should only be used ONCE */
		this.ipServer = ipServer;
		if (graph.getNode(ipServer)==null) {/* node does not exist, needs to be added */
			graph.addNode(ipServer);		
			graphstyling.sinkColor(ipServer);
		}
	}
/***************************************************************************/	
	
	
	
	
	
	
	
	
	
	
/*	
keepAliveTimer causes problems.
Solution 1: printEdgesInfo
for node :nodes
If node.parentonly == false
do KeepaliveTimer ....

Solution 2
 set KeepAliveTimer to a time (what? Small or big?)
	
*/	
	
	
	/* If a node is reported ONLY as a parent, it could be an intruder (alien).
	 * Mark it as a node, but dont set the boolean parameter "responding"
	 */	
	public boolean onlyAddNodeifNotExist(String nodeId) {	
		boolean answer = false;

		
		if (graph.getNode(nodeId)==null) {/* node does not exist, needs to be added */
			debug("Only adding (parent) node: "+IPlastHex(nodeId));
			
			graph.addNode(nodeId);
			graphstyling.nodeColorAlien(graph.getNode(nodeId));
			
			/* Node has been reported as a parent from another node.
			 * If the node later on responds as a child, this variable
			 * will be false */
			graph.getNode(nodeId).setAttribute("parentOnly", true);
			
			/* color each node for the graph, accordingly */ 
			if(nodeId.equals(ipServer))
				graphstyling.sinkColor(nodeId);			
			else { /* for all nodes != sink */

				graphstyling.nodeColorAlien(graph.getNode(nodeId));
				graph.getNode(nodeId).setAttribute("timeSeenCounter",0);
				graph.getNode(nodeId).setAttribute("avgTimeSeen",0.0);
				graph.getNode(nodeId).setAttribute("keepAliveTimer",System.currentTimeMillis());	
			}
			
			answer = true; 
		} else if(!nodeId.equals(ipServer)) { /*  for all nodes EXCEPT the sink */
			
			/* node already exists, no need to be added, DONT reset its timer, 
			 * it was just mentioned, there was not direct contact
			 */
			//debug("Node: "+IPlastHex(graph.getNode(nodeId).toString())+ "appears as neighbor");	
		}		
		return answer;
	}
/***************************************************************************/	
	/* If a node does not exist, add it to the graph. If the node exists,
	 * make sure that the keepAliveTimer is reset. iF the timer remains
	 * unchanged, the node is not in the graph anymore.
	 */
	public boolean checkNode(String nodeId) {

		boolean answer = false; 
		int timeSeenCounter = 0;
		double updatedAvgSeen = 0;
		long currentTime = System.currentTimeMillis();
		
		if (graph.getNode(nodeId)==null) {/* node does not exist, needs to be added */
			graph.addNode(nodeId);
			debug("New node added: "+graph.getNode(nodeId));
			
			/* color each node for the graph, accordingly */ 
			if(nodeId.equals(ipServer))
				graphstyling.sinkColor(nodeId); //TODO: redundant code, above is enough?
			else { /* for all nodes != sink */

				graph.getNode(nodeId).setAttribute("parentOnly", false);
				graphstyling.nodeStyle(nodeId);

				if(graph.getNode(nodeId).getAttribute("UDPRecv")==null)
					graph.getNode(nodeId).setAttribute("UDPRecv",0);

				graph.getNode(nodeId).setAttribute("timeSeenCounter",1);
				graph.getNode(nodeId).setAttribute("avgTimeSeen",0.0);
				graph.getNode(nodeId).setAttribute("keepAliveTimer",currentTime);
			}
			answer = true; 
			
		} else if(!nodeId.equals(ipServer)) { /*  for all nodes EXCEPT the sink */
			
			/* node already exists, no need to be added, just reset its timer */
			graph.getNode(nodeId).setAttribute("keepAliveTimer",currentTime);
			
			/* Node has just responded as a child, hence it is not only parent */
			graph.getNode(nodeId).setAttribute("parentOnly", false);
			
			double curAvgTimeSeen = (double) graph.getNode(nodeId).getAttribute("avgTimeSeen");
						
			timeSeenCounter = (int) graph.getNode(nodeId).getAttribute("timeSeenCounter");
			updatedAvgSeen = ( curAvgTimeSeen+currentTime ) /++timeSeenCounter;
			graph.getNode(nodeId).setAttribute("timeSeenCounter",timeSeenCounter);
			graph.getNode(nodeId).setAttribute("avgTimeSeen",updatedAvgSeen);
			
			if(graph.getNode(nodeId).getAttribute("UDPRecv")==null)
				graph.getNode(nodeId).setAttribute("UDPRecv",0);
		}		
		return answer;
	}
/***************************************************************************/	
	/* if an edge does not exist, add it to the graph AND remove the old incoming one if any*/
	public void checkEdge(String ip1, String ip2) {

		String curEdge = ip1+"-"+ip2;
		if (graph.getEdge(curEdge)==null) {
			if((graph.getNode(ip2).getInDegree()>0)) { /* node has an old parent */
	
				List<Edge> edges = graph.edges()
					    .filter(edge -> edge.getNode1() == graph.getNode(ip2))
					    .collect(Collectors.toList());
				
				for(Edge edge : edges){
					Node oldParent = edge.getSourceNode();
					debug("Removing an old edge: "+oldParent.toString()+"-->"+ip2);
					removeEdgeifExists(oldParent.toString(),ip2);
				} 
			}
			graph.addEdge(curEdge,ip1,ip2, true);/* true means directional */

			//TODO: Is it needed here???
			//printEdgesInfo();
		}	
	}
/***************************************************************************/
	public void addRecvdPacket(String nodeId) {
		try{
			Node node = graph.getNode(nodeId);
			int UDPRecv = (int) node.getAttribute("UDPRecv");
			UDPRecv+=1;
			node.setAttribute("UDPRecv", UDPRecv);
		}catch (Exception e) {
			debug("Node "+IPlastHex(nodeId)+": "+e.toString());
		}
	}
/***************************************************************************/	
	/* Convert the last part of IPv6 to DEC for short print */
	public int IPlastHex(String IPv6) {
		int decValue = 0;
		int index = IPv6.lastIndexOf(":");
		String lastPart = IPv6.substring(index+3,index+5);// last hex number (e.g. 0e0e)
		try{
			decValue = Integer.valueOf(lastPart,16);
		}catch(NumberFormatException e) {
			debug(e.toString());
		}
		return decValue;
	} 
/***************************************************************************/
	/* Remove and old edge (Normally, the node changed parent) */
	public void removeEdgeifExists(String from, String to) {
		try {
			graph.removeEdge(from, to);
		}catch(ElementNotFoundException e) {
			debug(e.toString());
		}
	}
/***************************************************************************/
	/* Remove and old edge (Normally, the node changed parent) */
	public void removeEdgeifExists(Node from, Node to) { /* polymorphism */
		try {
			graph.removeEdge(from, to);
		}catch(ElementNotFoundException e) {
			debug(e.toString());
		}
	}
/***************************************************************************/
	/* Remove and old edge (Normally, the node changed parent) */
	public void removeEdgeifExists(Edge edge) { /* polymorphism */
		try {
			graph.removeEdge(edge);
		}catch(ElementNotFoundException e) {
			debug(e.toString());
		}
	}
/***************************************************************************/	
	/* sometimes an IP "0000" comes along, but we need to compare it
	 * with the SINK's IP, hence AFTER the sink IP is first set
	 */
	public boolean legitIncomIP(String incomingIP) {
    	/* if chars 1-4 in str1 match chars 1-4 in str2 */
    	return incomingIP.regionMatches(1, ipServer, 1, 4);
    }
/***************************************************************************/
	public void printEdgesOnly(int roundsCounter, long timeStart){ //TODO: Redundand method ??? Below method is enough?
		int nodesNum = graph.getNodeCount();
		int edgesNum = graph.getEdgeCount();
		
		/* print every fifteen rounds, after initial delay */
		if(roundsCounter%15 == 0 && timeStart > Main.keepAliveNodeBound) { 

			Stream<Edge> edgesStr = graph.edges();
			Iterable<Edge> edges = edgesStr::iterator; 
			
			debug("R:"+roundsCounter+"-------Edges: "+graph.getEdgeCount()+"---------------");
			for (Edge edge : edges) {
				Node n0 = edge.getNode0();
				Node n1 = edge.getNode1();	
				debug("\tEDGE:\t"+IPlastHex(n0.toString())+"--->"+IPlastHex(n1.toString()));
			}
			debug("-----Current Nodes:"+nodesNum+"------Current Edges:"+edgesNum+"-------------\n\n");
		}
	}	
/***************************************************************************/	
	/* Convert a string to a byte IPv6. Currently it does not work? */
	public  byte[] string2IP(String ipString) throws Exception{
		byte[] bytes = null;
		try {
			InetAddress a = InetAddress.getByName(ipString);
		    bytes = a.getAddress();
		} catch(Exception e) {
			debug(e.toString()); //getMessage has validation error
		}
		return bytes;
	}
/***************************************************************************/    

/***************************************************************************/  
	private void debug(String message){
		Main.debug((message));
	}
}
