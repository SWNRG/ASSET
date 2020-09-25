package com.uom.georgevio;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.stream.Stream;

import org.graphstream.graph.Edge;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

public class ReadInput {
	
	protected Graph graph = new SingleGraph("Graph");	

	private String ipServer =null;
	int roundsCounter=0;
	
	public void splitString(String strInPut){

		if(strInPut.startsWith("Tentative")){
			String[] parts = strInPut.split("Tentative link-local IPv6 address ",2);
			ipServer = "["+parts[1]+"]";
			debug("found ipServer: "+ipServer);
			checkNode(ipServer);
		}
		if (strInPut.startsWith("Route")){			
			String[] parts = strInPut.split(" ",4);			
			String ip1 = parts[1];
			String ip2 = parts[2];

			String[] ltime = parts[3].split(":",2);
			String lt = ltime[1];
			
			checkNode(ip1); 
			checkNode(ip2); 	
			if(ip1.equals(ip2)) {//node is a direct child of sink's
				debug("direct child of sink: "+ip1);
				if(ipServer==null) {
					ipServer = "[fe80:0000:0000:0000:0212:7401:0001:0101]";
					checkNode(ipServer);
				}
				checkEdge(ipServer,ip2);
			}else {
				//nothing to do here
			}
		}

		if(strInPut.startsWith("NP")){
			debug("NP incoming");
			try{
				String[] parts = strInPut.split("NP:",2);
				parts = parts[1].split(" ",3);

				String ipParent = parts[0];
				String ipChild = parts[2];
				
				debug("ipParent: "+ipParent+" "+IPlastHex(ipParent));
				debug("ipChild: "+ipChild+" "+IPlastHex(ipChild));		
				
				checkNode(ipParent);
				checkNode(ipChild);
				checkEdge(ipParent, ipChild);	
			
			}catch (ArrayIndexOutOfBoundsException e) {
				debug(e.toString());
			}
		}
		roundsCounter++;
/*************** PTINTOUTS **********************************/		
		//printAllNodes();
		printGraph();
		printEdges();
		//getDegrees(); //TODO: Check if there is a case where this is needed
/************************************************************/
		if(roundsCounter>5 && ipServer==null){// IP of sink was probably lost by now. Hardwire it...
			ipServer = "[fe80:0000:0000:0000:0212:7401:0001:0101]";
			debug("Setting ipServer by value...");
			checkNode(ipServer);
		}
	}

	public void printEdges(){
		int numEdges=0;	
		Stream<Edge> edgesStr = graph.edges();
		Iterable<Edge> edges = edgesStr::iterator; 
		
		debug("-------Edges---------------");
		for (Edge edge : edges) {
			numEdges++;
			Node n0 = edge.getNode0();
			Node n1 = edge.getNode1();	
			debug("EDGE: "+IPlastHex(n0.toString())+"--->"+IPlastHex(n1.toString()));//+", EDGE:"+edge.toString());
		}
		debug("------Current Edges:"+numEdges+"-------------");
		
	}

	// TODO: is this method needed? Graph seeems to be discovered at all times, without it
	public void getDegrees() {

		Stream<Node> nodesStr = graph.nodes();
		Iterable<Node> nodes = nodesStr::iterator; 
		for (Node node : nodes) {
			debug(" Node "+IPlastHex(node.toString())+", degree: "+node.getDegree());
			if(node.getDegree()==0 && !(node.toString()).equals(ipServer)){//orphan node, probe it again
				//debug("Sending SP to "+IPlastHex(node.toString()));
				//Client.outSend("SP");
			}
		}
	}

	public void printGraph() {

		Stream<Node> nodesStr = graph.nodes();
		Iterable<Node> nodes = nodesStr::iterator; 
		for (Node node : nodes) {
			
			node.setAttribute("ui.style", "shape:circle;fill-color:yellow; size:20px; text-alignment: center;");
			node.setAttribute("ui.label", IPlastHex(node.toString())); 

		}
	}

	/* remove and old edge (usually the node changed parent) */
	public void removeEdgeifExists(String from, String to) {
		try {
			graph.removeEdge(from, to);
		}catch(ElementNotFoundException e) {
			debug(e.toString());
		}
	}
	
	/* If a node does not exist, add it to the graph */
	public void checkNode(String nodeId) {
		/*try {
			graph.addNode(nodeId);
		}catch(Exception IdAlreadyInUseException) {
			debug("id exists: "+nodeId);
		}*/
		
		
		if (graph.getNode(nodeId)==null) {
			graph.addNode(nodeId);
		}	
	}
	
	/* if an edge does not exist, add it to the graph */
	public void checkEdge(String ip1, String ip2) {
		String curEdge = ip1+"-"+ip2;
		if (graph.getEdge(curEdge)==null) {
			graph.addEdge(curEdge,ip1,ip2, true);/* true means directional graph */
		}		
	}

	
	public Iterator<Node> getNodesOrdering(Graph g, Node v) {// SORTING THE NODES
		Stream<Node> nodes = g.nodes().sorted(); //TODO: Find out how to make it work ???
		return nodes.iterator();
	}
	
	
	/* Print all nodes in the graph */
	public void printAllNodes() {
		//Iterator<? extends Node> nodes = graph.getNodeIterator();
		
		Stream<Node> nodesStr = graph.nodes();
		Iterable<Node> nodes = nodesStr::iterator; 
		
		int nodesCount=0;
		debug("---------Graph Nodes--------");
		for (Node node : nodes) {
			nodesCount++;
			debug(nodesCount+"\t"+node.toString()+"\t"+IPlastHex(node.toString()));
		}
		debug("---------------------------\n");
	}
	/* Convert a string to a byte IPv6. Currently it does not work? */
	public  byte[] string2IP(String IpString) throws Exception{
		byte[] bytes =
				null;
		try {
			InetAddress a = InetAddress.getByName(IpString);
		    bytes = a.getAddress();
		} catch(Error e) {
		    //e.getMessage has validation error
		}
		return bytes;
	}

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
	
	private static void debug(String message){
		//Main.debug((message));
	}
}
