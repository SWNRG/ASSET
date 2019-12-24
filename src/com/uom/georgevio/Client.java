package com.uom.georgevio;

import com.fazecast.jSerialComm.SerialPort;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.graphstream.graph.Edge;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.javafx.FxGraphRenderer;
import org.graphstream.ui.view.Viewer;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Client implements Runnable{

	private String ipServer = null; /* hardwire the sink's IP if not found */
	int roundsCounter=0;
	long timeStart = System.currentTimeMillis();
	long lastTimeFired = System.currentTimeMillis();
	SerialPort motePort = null;

	SerialPortProbe serialportprobe =new SerialPortProbe();
	Send2Serial send2serial;

	GraphStyling graphstyling = new GraphStyling();
	protected Graph graph = graphstyling.returnGraph();

	SpanningTree spanningtree = new SpanningTree(graph); /* graph exists now */
	
	Scanner lineReader; /* read serial port output line by line */

	
    @Override
    public void run(){

    	debug("Client thread started...");
		setSerialPort();
        String inComingLine = null;
/* if there are no updates in the network, nothing comes outside the serial port.
 * We need a mechanism to trigger events by TIME, not by waiting from serial port.    		
 */
		//printGraph(); //TODO: Check if needed and remove, it looks suspicious 

		send2serial= new Send2Serial(motePort);
		lineReader = new Scanner(motePort.getInputStream());
		while(lineReader.hasNextLine() ){
			inComingLine = lineReader.nextLine();
			if(inComingLine!=null){

            	if(inComingLine.startsWith("Tentative")){ /* only the sink prints at the serial port */
        			String[] parts = inComingLine.split("Tentative link-local IPv6 address ",2);
        			ipServer = "["+parts[1]+"]";
        			debug("found ipServer: "+ipServer);
        			checkNode(ipServer);
        		}/* end if InPut.startsWith("Tentative") */
        		
            	if (inComingLine.startsWith("Route")){
        			String[] parts = inComingLine.split(" ",4);
        			String ip1 = parts[1];
        			String ip2 = parts[2];
        			String[] ltime = parts[3].split(":",2);
        			String lt = ltime[1];
        			int intlt = Integer.parseInt(lt);
        			//TODO: use the intlt in the graph
        			
        			checkNode(ip1); 
        			checkNode(ip2); 	
        			if(ip1.equals(ip2)) {//node is a direct child of sink's
        				debug("found a direct child of sink: "+ip1);
        				if(ipServer==null) {
        					ipServer = "[fe80:0000:0000:0000:c30c:0000:0000:0001]";
        					checkNode(ipServer);
        				}
        				checkEdge(ipServer,ip2);
        			}else {
        				//nothing to do here
        			}
        		}/* end if InPut.startsWith("Route") */
        		 
        		if(inComingLine.startsWith("NP")){
        			try{
        				String[] parts = inComingLine.split("NP:",2);
        				parts = parts[1].split(" ",3);
        				String ipParent = parts[0];
        				String ipChild = parts[2];	        					
        				debug("R:"+roundsCounter+" edge "+IPlastHex(ipParent)+"-->"+IPlastHex(ipChild));
        				checkNode(ipParent);
        				checkNode(ipChild);
        				checkEdge(ipParent, ipChild);	
        			
        			}catch (ArrayIndexOutOfBoundsException e) {
        				debug(e.toString());
        			}
        		} /* end if InPut startsWith "NP" */
        		        		
        		/* Nodes were forced to send neighbors. They don't necessary 
        		 * have an edge with those neighbors, so we only add the node 
        		 * if it does not exist, BUT NOT the edge. 
        		 * After that, we need to ask the node to print its father.
        		 */
        		if(inComingLine.startsWith("N1")){
        			try{
        				String[] parts = inComingLine.split("N1:",2);
        				parts = parts[1].split(" ",3);
        				String neighbor = parts[0];
        				String nodeProbed = parts[2];	        				

        				/* sometimes an IP "0000" comes along */
        				if(legitIncomIP(neighbor) && checkNode(neighbor)) {
	        					debug("found a NEW neighbor! Lets probe its parents");
	        					String message = "SP "+neighbor+"\n";	
	        					send2serial.sendSpecificMessage(message);
        				}
	        			if(legitIncomIP(nodeProbed) && checkNode(nodeProbed)) {
	        					debug("found a NEW node! Lets probe its parents");
	        					String message = "SP "+nodeProbed+"\n";
	        					send2serial.sendSpecificMessage(message);
        				}

        			}catch (ArrayIndexOutOfBoundsException e) {
        				debug("could not break apart: "+inComingLine);
        				debug(e.toString());
        			}
        		}

        		if(inComingLine.startsWith("Custom")){
        			try{
        				String[] parts = inComingLine.split("from ",2);
        				String nodeAlive = parts[1];	        				

	        			if(legitIncomIP(nodeAlive)) {
	        				checkNode(nodeAlive); /* it will also reset the keepAliveTimer */	
        				}

        			}catch (ArrayIndexOutOfBoundsException e) {
        				debug("could not break apart: "+inComingLine);
        				debug(e.toString());
        			}
        		}
        		
        		
/*************** PRINTOUTS **********************************/		
        		//printAllNodes();		        		
        		//printGraph();
        		
				countEdges();
				printEdgesInfo();

/******** Checking for nodes with degree == 0 occasionally****************/
        		if(roundsCounter%50==0) {/* Every fifty rounds */
	        		getInDegrees(); /* Just in case there is someone hiding? */
	        		
	        		//TODO: This is not needed anymore?
        		}
/********* IP of sink was probably lost by now. Hardwire it...*********/       		
        		if(roundsCounter>5 && ipServer == null){
        			ipServer = "[fe80:0000:0000:0000:0212:7401:0001:0101]";
        			debug("R:"+roundsCounter+" Setting ipServer by value...");
        			checkNode(ipServer);
        		}
/*********************End of reading the serial lines ********/
			 }/* end if InPut!=null */
    		 roundsCounter++;
		}/* end while nextline() */
		debug("Closing scanner...Program ended");
		lineReader.close(); /* it should never come here */
    }/* end run() */
/*************************************************************/
/**************END OF run()***********************************/
/*************************************************************/
    
public void setSerialPort(){
	try{
/********* Set & open the serial port ***************************/
		debug("Will keep on trying to open motePort every 2000ms");
		while(motePort == null) {
			motePort = serialportprobe.getSerialPort();
			Thread.sleep(2000);
			debug("port not found");
		}
		debug("Serial Port found :)");
	} catch (Exception e) {
		debug(e.toString());
	}
}
/***************************************************************************/	
    public boolean legitIncomIP(String incomingIP) {
    	/* sometimes an IP "0000" comes along, but we need to compare it
    	 * with the SINK's IP, hence AFTER the sink IP is first set
    	 */
    	return incomingIP.regionMatches(1, ipServer, 1, 4);
    }    
/***************************************************************************/	
	public void printEdgesInfo(){
		int nodesNum = graph.getNodeCount();
		int edgesNum = graph.getEdgeCount();
		int inDegree = 0;
		int outDegree = 0;
		int totalOutDegrees = 0;
		long keepAliveTimer;
		
		if(roundsCounter%6 == 0 && timeStart>50000) { /* print every six rounds */	
			
			debug("R:"+roundsCounter+" -------Nodes - I/O Edges---------------");

			/* Also can get data from Stream<Nodes> */
			List<Node> nodes = graph.nodes()
				    .collect(Collectors.toList());

			for (Node node : nodes) {

				inDegree = node.getInDegree();
				outDegree = node.getOutDegree();
				totalOutDegrees+=outDegree;
				keepAliveTimer = (long)node.getAttribute("keepAliveTimer");
				keepAliveTimer = System.currentTimeMillis() - keepAliveTimer;
				debug(" Node: "+IPlastHex(node.toString())+"\tInEdges: "+inDegree+
							"\tOutEdges: "+outDegree+",\tLast seen: "+keepAliveTimer+" seconds ago"); 
				
				if (node.getId() != ipServer){ /* Sink should not be probed */
					if (keepAliveTimer<80000) { 
						 /* Node reappears, or appears for first time */
						graphstyling.nodeStyle(node);
					}	
					
					if (keepAliveTimer>80000 &&  keepAliveTimer<150000 ) { 
						debug("Node "+IPlastHex(node.toString())+" is very quiet lately...");
						graphstyling.nodeColorGrey(node);
					}				
					
					if (keepAliveTimer>150000 ) { 
						debug("Node "+IPlastHex(node.toString())+" is long gone... Removing it");
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
	public void countEdges(){
		int nodesNum = graph.getNodeCount();
		int edgesNum = graph.getEdgeCount();
		if(roundsCounter%15 == 0 && timeStart>80000) { /* print every ten rounds with initial delay */

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
		/* float curTimeDiff = (System.currentTimeMillis() - timeStart) / 1000F; // seconds */
		float initialDelayTimer = System.currentTimeMillis()-timeStart;

		int edgesPlus1 = edgesNum+1;
		
		/* Initial delay 2mins, every 5mins, after the network has settled down */
		if (initialDelayTimer>50000 && System.currentTimeMillis()>lastTimeFired+10000 ) {
			
			if(nodesNum!=edgesPlus1) { /* If nodes<>edges+1, there is missing information */
				debug("R:"+roundsCounter+" ALERT: nodes "+nodesNum+" <> (edges+1) "+edgesPlus1);
				
				if(!getInDegrees()) { /* if there is no node with zero inDegree, last resort, probe for neighbors */
	
					Stream<Node> nodesStr = graph.nodes();
					Iterable<Node> nodes = nodesStr::iterator; 
					send2serial.probeNeighbors(nodes);					
				}
				debug("Resetting lastTimeFired\n");
				lastTimeFired = System.currentTimeMillis();
			
			} else { /* Check the Spanning Tree */
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
	public boolean getInDegrees() {

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
	public void printGraph() { //TODO: What is this???
		
		List<Node> nodes = graph.nodes().collect(Collectors.toList());		
		for (Node node : nodes) {		
			
			node.setAttribute("ui.style", "shape:circle;fill-color:yellow; size:20px; text-alignment: center;");
			node.setAttribute("ui.label", IPlastHex(node.toString())); 

		}
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
	/* If a node does not exist, add it to the graph. If the node exists,
	 * make sure that the keepAliveTimer is reset. iF the timer remains
	 * unchanged, the node is not in the graph anymore
	 */
	public boolean checkNode(String nodeId) {
		boolean answer = false; 
		if (graph.getNode(nodeId)==null) {/* node does not exist, needs to be added */
			graph.addNode(nodeId);

			/* color each node for the graph, accordingly */ 
			if(nodeId.equals(ipServer))
				graphstyling.sinkColor(nodeId);
			else
				graphstyling.nodeStyle(nodeId);

			answer = true; 
		}
		/* node already exists, no need to be added, just reset its timer */
		graph.getNode(nodeId).setAttribute("keepAliveTimer",System.currentTimeMillis());
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
			printEdgesInfo();
		}	
	}
/***************************************************************************		
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
	private void debug(String message){
		Main.debug((message));
	}
/***************************************************************************/
} //Client class