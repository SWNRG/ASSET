package com.uom.georgevio;

import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.graphstream.graph.Edge;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import com.fazecast.jSerialComm.SerialPort;

import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;

public class ClientHelper {

	private String ipServer = null; /* hardwire the sink's IP if not found */
	
	SerialPort motePort = null; /* will be set by the Client class */
	
	private long timeReprobeEdgesCounter = System.currentTimeMillis(); /* ??? */
	
	GraphStyling graphstyling = new GraphStyling();

	private Graph graph = graphstyling.returnGraph();
	
	SpanningTree spanningtree = new SpanningTree(graph); /* graph exists now */
	
	ClusterMonitor clustermonitor = new ClusterMonitor();
	
	Send2Serial send2serial; 
	
	DecimalFormat df = new DecimalFormat();
	
	DataAnalysis datanalys = new DataAnalysis(); //not used
	
	ChebyshevInequality chebIneq = new ChebyshevInequality();
	
	Object2Double object2double = new Object2Double();
	
	/* Just testing RPL without ANY OVERHEAD. KEEP IT FALSE */
	boolean dontDoAnythingTest = false;
	
	//Scene mainScene = Main.getScene();
	
/***************************************************************************/
	public ClientHelper() { /* constructor */
		Main.debugEssentialTitle("Time\t\tNode\tInEdges\tLastSeen(sec)\tUDPRecv\tICMPin\tICMPout"); 
	}
/***************************************************************************/
	public void removeGraph() {
		graphstyling.removeView();
	}
/***************************************************************************/	
	public void closeGraphViewer() { /* trying to stop the graph viewer in main */
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
		if (!dontDoAnythingTest)
			send2serial.sendSpecificMessage(message);
	}
/***************************************************************************/	
	public boolean getInDegrees(int roundsCounter) {
		boolean answer = false;
		Stream<Node> nodesStr = graph.nodes();
		//Iterable<Node> nodes = nodesStr::iterator; 
				
		/* Trying to iterate RANDOMLY */
		Iterator<Node> randomNodes = nodesStr.iterator();
		List<Node> nodeArray = new ArrayList<Node>();
		while (randomNodes.hasNext()) {
			nodeArray.add(randomNodes.next());
		}
		Collections.shuffle((List<?>) nodeArray);
		
		//while (randomNodes.hasNext()) {
			//Node node = randomNodes.next();
		
		for (Node node : nodeArray) {
			/* Orphan node(s), probe it again. This will happen rarely */
			if(node.getInDegree()== 0 && !(node.toString()).equals(ipServer)){
				debug("R:"+roundsCounter+" Sneacky node "+IPlastHex(node.toString())
						+ " with no in->edge, was msg'd ");		
				String message = "SP:"+node.toString()+"\n";
				/*
				send2serial.sendSpecificMessage(message);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} */
				
				/* trying to implement NON-blocking */	
			  new Thread( new Runnable() {
			        public void run()  {
			            try  { Thread.sleep( 800 ); }
			            catch (InterruptedException ie)  {}
			            send2serial.sendSpecificMessage(message);
			        }
			    } ).start();

				answer = true; /* found a sneaky hiding node and probed it */				
			}//if
		}//while
		return answer; /* no one has zero incoming degree, If you are missing nodes, try next to probe for neighbors */
	}
/***************************************************************************/		
	public void probeForHiddenEdges(int roundsCounter){
		int nodesNum = graph.getNodeCount();
		int edgesNum = graph.getEdgeCount();
		
		/* Initial delay ??? mins, every ??? mins, hence, after the network has "settled" down */
		//if (System.currentTimeMillis()  > Main.appTimeStarted && //+ Main.keepAliveNodeBound && 
		//		System.currentTimeMillis() > timeReprobeEdgesCounter+ Main.keepAliveNodeBound &&
		if(	roundsCounter>50 &&	roundsCounter%100 == 0 &&
				System.currentTimeMillis() > 
						timeReprobeEdgesCounter + Main.keepAliveNodeBound ) {
			
			debug("R:"+roundsCounter+" probing for Hidden Edges in clientHelper");
			
			timeReprobeEdgesCounter = System.currentTimeMillis();
			
			if(nodesNum != edgesNum+1) { /* If nodes<>edges+1, there is missing information */
				debug("R:"+roundsCounter+" ALERT: nodes "+nodesNum+" <> (edges+1) "+(edgesNum+1));
				
				/* if there is no node with zero inDegree, last resort, probe for neighbors */
				if(!getInDegrees(roundsCounter)) { 
					
					debug("Tried getInDegrees, now probeNeighbors...");

					Stream<Node> nodesStr = graph.nodes();
					Iterable<Node> nodes = nodesStr::iterator; 
					
					/* The sink seems to receive those, but it does not forward? */
					send2serial.probeNeighbors(nodes);					
				}

			} else { /* Check the Spanning Tree JUST IN CASE: There could be loops! */
				//TODO: Make the spanning tree run less often (timer?)
				List<Edge> edgesSpan = spanningtree.spanTreeKruskal();
				if(!edgesSpan.isEmpty()) {
					for (Edge edge : edgesSpan) {
						Main.debug("Spanning Tree: Loose node(s) found! Taking actions...\n");
						/* Edge will be removed, node will be forced to tell the truth */
						removeEdgeifExists(edge);
					}
				}
			}
		} else {
			//debug("R:"+roundsCounter+" probeForHiddenEdges did not start yet");
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
		int ICMPSent = 0;
		int ICMPRecv = 0;
		df.setMaximumFractionDigits(2); /* printing only 2 decimals */
		
		/* If it is smaller than a given threshold, the node is still alive.
		 * Else, the node is long gone, remove it.
		 */
		long keepAliveTimer;
		
		/* Print every nine rounds, AFTER a "grace" period. At the beginning
		 * the network is still converging.
		 */	
		if(roundsCounter%90 == 0 && Main.appTimeStarted > 4*Main.keepAliveNodeBound) { 			
			debug("R:"+roundsCounter+" -------Nodes - I/O Edges---------------");

			/* Also can get data from Stream<Nodes> */
			List<Node> nodes = graph.nodes()
					.filter(node -> node.getId() != ipServer) /* Sink should not be probed */
				    .collect(Collectors.toList());

			/* this has to be printed only once in the relative file? */
			//Main.debugEssential("Time\tNode\tInEdges\tLastSeen(sec)\tUDPRecv\tICMPin\tICMPout");			
			for (Node node : nodes) {

				inDegree = node.getInDegree();
				outDegree = node.getOutDegree();
				totalOutDegrees+=outDegree;
				int shortNodeName = IPlastHex(node.toString());
				keepAliveTimer = System.currentTimeMillis() - (long) node.getAttribute("keepAliveTimer");;
				//float secs = keepAliveTimer / 1000F; 
				String secs = new DecimalFormat("#,##0.00").format( keepAliveTimer / 1000F); /* seconds */
				try {
					UDPRecv = (int) node.getAttribute("UDPRecv");
					ICMPSent = (int) node.getAttribute("ICMPSent");
					ICMPRecv = (int) node.getAttribute("ICMPRecv");

				}catch (NullPointerException e) {
					debug("Node "+IPlastHex(node.getId())+" "+e.toString());
				}
/******PRINTING INFORMATION ABOUT NODES *********************************************/	
				debug(" Node "+shortNodeName+"\tInEdges "+inDegree+",\tLast seen "+
							//df.format(secs)+
							secs+" sec. UDPRecv "+UDPRecv+
							"\tICMP I/O: "+ICMPRecv+", "+ICMPSent
					 ); 

				/* file for easy extraction of stats only */
				Main.debugEssential(shortNodeName+"\t\t\t"+inDegree+"\t\t\t"+
						secs+"\t\t\t"+UDPRecv+
						"\t\t\t"+ICMPRecv+"\t\t\t"+ICMPSent
						); 

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
						debug("Node "+shortNodeName+" was parent, and gone... Removing it");
						graph.removeNode(node);
					} else {
						debug("Node "+shortNodeName+" is long gone... Removing it");
						graph.removeNode(node);
					}						
				}
			}
			debug("------Nodes:"+nodesNum+"------inComing Edges:"+edgesNum+"-------------\n\n");

			Main.debugEssential(" ");
			
			Main.nodesOutput((Integer.toString(nodesNum)));
			Main.edgesOutput((Integer.toString(edgesNum)));
			Main.totalOutDegreesOutput((Integer.toString(totalOutDegrees)));
		}   
	}
/***************************************************************************/		
	/* Running kMeans algorithm on nodes UDP received by the controller data.
	 * Step 1: If kMeans returns two clusters with confidence, this means that nodes
	 * are under attack since their UDP mean is an outlier. In this case we
	 * don't know how many "neighborhoods" of nodes are attacked.
	 * Step 2: We need to run Kosaraju algorithm to classify strongly connected components,
	 * aka how many different neighborhoods or clusters of nodes we have. 
	 * Step 3: we need to find the "mother* of each subgraph. This mother is the attacker.
	 * Step 4: send this "mother to all nodes as an attacker(s) so nodes can just exclude 
	 * it from being selected as a parent 
	 */
	public void runKMeans(int clusters) {
		
		List<Node> nodes = graph.nodes()
				.filter(node -> node.getId() != ipServer) 
			    .collect(Collectors.toList());

/* bring back here the mother-nodes to color accordingly */
		
		List<Node> motherNodes = null;
		try {
			motherNodes = clustermonitor.kMeans(clusters, nodes);
		} catch (Exception e) {
			debug("ClientHelper motherNodes creation problem:"+e.toString());
		}
		if(motherNodes !=null) {
			try {
				List<Node> attackedNodes = clustermonitor.getClusterWithAttackedNodes();
				for(Node node : attackedNodes) {
					if (!motherNodes.contains(node)) // it doesn't seem to work.. check again
						graphstyling.nodeUnderAttack(node);
					
					//printNodeDetails(node);
					
				}
			} catch (Exception e) {
				debug("ClientHelper attackedNodes problem: "+e.toString());
			}
			
			try {
				for(Node motherNode : motherNodes) {
					//debug("ClientHelper: Attacker Node:"+IPlastHex(motherNode.toString()));
					//printNodeDetails(motherNode);

					for (Node n : nodes) {
						/* they are not the SAME node, they have the same name?? */
						if (motherNode.getId() == n.getId()) {
							//debug("ClientHelper: Mother-coloring node: "+IPlastHex(n.getId()));
							graphstyling.nodeColorMother(n);
						}
					}
				}
			} catch (Exception e) {
				debug("ClientHelper motherNodes dublication problem: "+e.toString());
			}
		}// if(!motherNodes.isEmpty())
	}// public void runKMeans()
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
	/* If a node is reported ONLY as a parent, it could be an intruder (alien).
	 * Mark it as a node, but dont set the boolean parameter "responding"
	 */	
	public boolean onlyAddNodeifNotExist(String nodeId) {	
		boolean answer = false;
		
		if (graph.getNode(nodeId) == null) {/* node does not exist, needs to be added */
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
				
				graph.getNode(nodeId).setAttribute("UDPRecv",0);
				graph.getNode(nodeId).setAttribute("ICMPSent",0);
				graph.getNode(nodeId).setAttribute("ICMPRecv",0);
				
				/* store last icmp numbers, in order to calculate moving average */
				graph.getNode(nodeId).setAttribute("icmpArraySent", 
						new CircularQueue(Main.meanICMPHistoryKept)); /* size parameter */
				graph.getNode(nodeId).setAttribute("icmpArrayRecv", 
						new CircularQueue(Main.meanICMPHistoryKept));				
				graph.getNode(nodeId).setAttribute("isOutlier",false);
			}
			
			answer = true; 
		} else if(!nodeId.equals(ipServer)) { /*  for all nodes EXCEPT the sink */
			
			/* node already exists, nd to be added, DONT reset its timer, 
			 * it was just mentioned, there was not direct contact
			 */
			//debug("Node: "+IPlastHex(graph.getNode(nodeId).toString())+ "appears as neighbor");	
		}
		if(!dontDoAnythingTest)
			return answer;
		else
			return false;
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

		/* color each node for the graph, accordingly */ 
		if(nodeId.equals(ipServer))
			graphstyling.sinkColor(nodeId); //TODO: redundant code, above is enough?
		else /* for all nodes except the sink */
			if (graph.getNode(nodeId) == null) {/* node does not exist, needs to be added */
				graph.addNode(nodeId);
				debug("New node added: "+IPlastHex(nodeId)+", "+graph.getNode(nodeId));
	
				graph.getNode(nodeId).setAttribute("parentOnly", false);
				graphstyling.nodeStyle(nodeId);
				
				graph.getNode(nodeId).setAttribute("verNumAttacks",0);
				graph.getNode(nodeId).setAttribute("verNumAttacksState","T0");
	
				graph.getNode(nodeId).setAttribute("UDPRecv",0);
				graph.getNode(nodeId).setAttribute("ICMPSent",0);
				graph.getNode(nodeId).setAttribute("ICMPRecv",0);
				
				graph.getNode(nodeId).setAttribute("timeSeenCounter",1);
				graph.getNode(nodeId).setAttribute("avgTimeSeen",0.0);
				graph.getNode(nodeId).setAttribute("keepAliveTimer",currentTime);
				
				/* store last icmp numbers, in order to calculate moving average */
				graph.getNode(nodeId).setAttribute("icmpArraySent", 
						new CircularQueue(Main.meanICMPHistoryKept)); /* size parameter */
				graph.getNode(nodeId).setAttribute("icmpArrayRecv", 
						new CircularQueue(Main.meanICMPHistoryKept));				
				graph.getNode(nodeId).setAttribute("isOutlier",false);
				
				answer = true; 
	 
			} else { /* node exists, just reset timers */
			
				/* node already exists, no need to be added, just reset its timer */
				graph.getNode(nodeId).setAttribute("keepAliveTimer",currentTime);
				
				/* Node has just responded as a child, hence it is not only parent */
				if ( (boolean) graph.getNode(nodeId).getAttribute("parentOnly")) {
						graph.getNode(nodeId).setAttribute("parentOnly", false);
						graphstyling.nodeStyle(nodeId);	/* node is regular now (not parent) */
				}
				
				double curAvgTimeSeen = (double) graph.getNode(nodeId).getAttribute("avgTimeSeen");
							
				timeSeenCounter = (int) graph.getNode(nodeId).getAttribute("timeSeenCounter");
				updatedAvgSeen = ( curAvgTimeSeen+currentTime ) /++timeSeenCounter;
				graph.getNode(nodeId).setAttribute("timeSeenCounter",timeSeenCounter);
				graph.getNode(nodeId).setAttribute("avgTimeSeen",updatedAvgSeen);
			}	
			if(!dontDoAnythingTest)
				return answer;
			else
				return false;
				//return answer;
	}
/***************************************************************************/	
	/* if an edge does not exist, add it to the graph AND remove the old incoming one if any */
	public void checkEdge(String ip1, String ip2) {

		String curEdge = ip1+"-"+ip2;
		if (graph.getEdge(curEdge)==null) {
			if((graph.getNode(ip2).getInDegree()>0)) { /* node has an old parent */
	
				List<Edge> edges = graph.edges()
					    .filter(edge -> edge.getNode1() == graph.getNode(ip2))
					    .collect(Collectors.toList());
				
				for(Edge edge : edges){
					Node oldParent = edge.getSourceNode();
					debug("Removing an old edge: "
							+IPlastHex(oldParent.toString())+"-->"+IPlastHex(ip2));
					removeEdgeifExists(oldParent.toString(),ip2);
				} 
			}
			graph.addEdge(curEdge,ip1,ip2, true);/* true means directional */

			//TODO: Is it needed here???
			//printEdgesInfo();
		}	
	}
/***************************************************************************/	
	public void addICMPStats(String nodeId, String inICMP, String outICMP) {
		try{

			Node node = graph.getNode(nodeId);
			int oldICMPSent = (int)node.getAttribute("ICMPSent");
			int oldICMPRecv = (int)node.getAttribute("ICMPRecv");
			
			/* Those numbers come accumulated (Since the beginning of the simulation */
			node.setAttribute("ICMPSent", 
						Integer.parseInt(outICMP)// - (int)node.getAttribute("ICMPSent")	
					);

			node.setAttribute("ICMPRecv", 
						Integer.parseInt(inICMP)// - (int)node.getAttribute("ICMPRecv")
					);
			
		}catch (Exception e) {
			debug("addICMPStats error for Node "+IPlastHex(nodeId)+": "+e.toString());
		}
	}
/***************************************************************************/		
	public void addICMPArrays(String nodeId, String inICMP, String outICMP) {
		Node node = null;
		/* Last accumulated values stored */
		int getLastSent = 0;
		int getLastRecv = 0;
		
		CircularQueue<Integer> circularqueueSent = null;
		CircularQueue<Integer> circularqueueRecv = null;
		
		try {
			node = graph.getNode(nodeId);
		}catch (NullPointerException e){
			debug("addICMPArrays node "+nodeId+" does not exist...");
		}
		
		try{
			/* Last accumulated values stored */
			getLastSent = getLastICMPSent(nodeId);
			getLastRecv = getLastICMPRecv(nodeId);
		}catch(Exception e) {
			debug("addICMPArrays getLastSent/getLastRecv error for node "+IPlastHex(nodeId));	
		}
		
		try {
			circularqueueSent = node.getAttribute("icmpArraySent", CircularQueue.class);
			circularqueueRecv = node.getAttribute("icmpArrayRecv", CircularQueue.class);
		}catch(Exception e) {
			debug("addICMPArrays circularqueueSent/circularqueueRecv error for node "+IPlastHex(nodeId));	
		}
			
		try {
			/* Those are the latest differences between the stored and the incoming values */
			getLastSent = Integer.parseInt(outICMP) - getLastSent;
			getLastRecv = Integer.parseInt(inICMP) - getLastRecv;
			
			/* queue remains untouched */
			Object[] sentArray = circularqueueSent.toArray();
			Object[] recvArray = circularqueueRecv.toArray();
			
			if(circularqueueSent.size() == Main.meanICMPHistoryKept) { /* Let the network stabilize */
				//df.format(double number)

				double[] doubleSentArray = object2double.convert(sentArray);
				double[] doubleRecvArray = object2double.convert(recvArray);
					
				if(Main.chebysevIneq) { /* GUI ToggleButton */
					/* Both standard deviations must be outliers */
					if(chebIneq.isOutlier(((double)getLastSent), doubleSentArray, IPlastHex(nodeId)) &&
					   chebIneq.isOutlier(((double)getLastRecv), doubleRecvArray, IPlastHex(nodeId))){
							debugBoth("Node "+IPlastHex(nodeId)+" Chebysev ICMP BOTH ISOUTLIER = 1. POSSIBLE ATTACK...");
							node.setAttribute("isOutlier",true);
					}
					//else
						//debug("Node "+IPlastHex(nodeId)+" is within Chebyshev limits");
				}
			}	
			
			/* Current incoming accumulated values. Store these now for the next round */
			setICMPSent(nodeId, outICMP);
			setICMPRecv(nodeId,inICMP);

			/* storing the difference between old <--> new value */
			circularqueueSent.add(getLastSent);
			circularqueueRecv.add(getLastRecv);

		}catch (Exception e) { 
			//e.printStackTrace();
			debug("ClientHelper: addICMPArrays for Node "+IPlastHex(nodeId)+": "+e.toString());
		}
	}
/***************************************************************************/	
	public void printNodeDetails(Node node) {
		debug("Node "+IPlastHex(node.getId())+" Edges: ");
		node.edges().forEach(edge->{
			debug("N0:"+IPlastHex(edge.getNode0().toString())+
				  " --> N1:"+IPlastHex(edge.getNode1().toString()));
		});
	}
/***************************************************************************/	
	public void colorMotherNode(Node node) {
		graphstyling.nodeColorMother(node);
	}
/***************************************************************************/	
	public void setICMPSent(String nodeId, String outICMP) {	
		Node node = graph.getNode(nodeId);
		/* Those numbers come accumulated (Since the beginning of the simulation */
		node.setAttribute("ICMPSent", 
					Integer.parseInt(outICMP)// - (int)node.getAttribute("ICMPSent")	
				);
	}
/***************************************************************************/	
	public void setICMPRecv(String nodeId, String inICMP) {
		Node node = graph.getNode(nodeId);
		
		node.setAttribute("ICMPRecv", 
					Integer.parseInt(inICMP)// - (int)node.getAttribute("ICMPRecv")
				);	
	}	
/***************************************************************************/	
	public int getLastICMPSent(String node) {
		int val = 0;
		try {
			val = (int) graph.getNode(node).getAttribute("ICMPSent");
		}catch (NullPointerException e){
			debug("getLastICMPSent error for node "+IPlastHex(node)+" "+e);
		}
		return val;
	}
/***************************************************************************/
	public int getLastICMPRecv(String node) {
		int val = 0;
		try {
			val = (int) graph.getNode(node).getAttribute("ICMPRecv");
		}catch (NullPointerException e){
			debug("getLastICMPRecv for node "+IPlastHex(node)+" "+e);
		}
		return val;
	}
/***************************************************************************/	
	private boolean checkVersionTimeAttack(Node node) {
		boolean answer = false;
		long curTime = System.currentTimeMillis();
		try {
			if(node.getAttribute("verNumAttackTime") == null) { /* first time only */
				node.setAttribute("verNumAttackTime",curTime);
			}
			
			long nodeVerNumAttackT = (long)node.getAttribute("verNumAttackTime");
			int attacksPerHour = (int) node.getAttribute("verNumAttacks");
			long timeDif = curTime - nodeVerNumAttackT;
		
			if(timeDif < 3600000) { /* within one hour */
				node.setAttribute("verNumAttacks", ++attacksPerHour);
				
				/* Papers suggest stop resetting trickle after VERSION_NUM_CHANGES attacks per hour */
				if((int)node.getAttribute("verNumAttacks")> Main.VERSION_NUM_CHANGES){ 
					answer = true;
				}
			}
			else {
				node.setAttribute("verNumAttacks", 1); /* resetting the time since it was more than one hour */
			}
			
			debug("Node "+IPlastHex(node.getId())+" attacksPerHour: "+attacksPerHour);
			
		}catch (Exception e){
			debug("Node "+node+" :"+e.toString());
		}
		return answer;
	}
/***************************************************************************/	
	public void addVerNumAttacks(String nodeId, String verNumAttacks) {
		try{
			Node node = graph.getNode(nodeId);
			if (checkVersionTimeAttack(node)) { /* The node is still in less than XX attacks per hour */
				/* Just in case the node was ordered to stop resetting trickle timer long ago */
				if ("T1".equals((String) node.getAttribute("verNumAttacksState"))) {
					String message = "T0 "+node+"\n"; //T0 start resetting trickle timer  
					sendMsg2Serial(message);					
				}					
			}
			else { /* The node is in more than XX attacks per hour, hence it is under attack */
				if (!"T1".equals((String) node.getAttribute("verNumAttacksState"))) { /* only once */
					node.setAttribute("verNumAttacksState", "T1"); /* By default is T0 */
					String message = "T1 "+node+"\n"; /* T1 = stop resetting trickle timer */  
					sendMsg2Serial(message);
				}				
			}
		}catch (Exception e) {
			debug("Node "+IPlastHex(nodeId)+": "+e.toString());
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
			debug("ClientHelper: hex2dec problem, last part: "+lastPart);
			debug("Incoming IPv6: "+IPv6);
			//debug(e.toString());
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
	public boolean legitIncomIP(String incomingIP) {
    	/* if chars 1-4 in str1 match chars 1-4 in str2 */
    	return incomingIP.regionMatches(1, ipServer, 1, 4);
    }
/***************************************************************************/
	public void printEdgesOnly(int roundsCounter, long timeStart){ //TODO: Redundant method ??? Below method is enough?
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
	private void debug(String message){
		Main.debug((message));
	}
/***************************************************************************/    
	private void debugBoth(String message) {
		Main.debug(message);
		Main.debugEssential(message);
	}
}
