package com.uom.georgevio;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.graphstream.algorithm.ConnectedComponents;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/* 
 * This is step No 2 in clustering suspicious nodes. Step No 1 already
 * identified "suspicious' nodes and created a (sub)graph.
 * Hence, the incoming graph must be a (sub)graph with only 'suspicious' nodes 
 * This class will do two things:
 * 1. identify and separate the incoming graph in strongly connected components,
 * i.e. identify how many different neighborhoods of attacked nodes exist.
 * 2. For each neighborhood, find the 'mother' node, i.e., node with no incoming 
 * edge. This exact node is the intruder/attacker.
 * 
 * Not implemented step: the intruders are returned as such to the controller,
 * which in return communicates them with all nodes to avoid using them as "parents".
 * */

public class FindConnectedComponents { 
	
	private Graph graph;
	
	ConnectedComponents cc = new ConnectedComponents(); /* graphstream algo using Kosaraju style */

	FindConnectedComponents(Graph graph){
		this.graph = graph; 
	}
	
	public List<Node> findCC() {
	
		List<Node> motherNodes = new ArrayList<>();
		
		cc.init(graph);
		cc.compute(); 	
		int ccGraphEnum = cc.getConnectedComponentsCount();
		debug("There are "+ccGraphEnum+" connected (sub)graphs");

		Iterator<? extends Node> nodess = graph.iterator();
		while(nodess.hasNext()){
			Node n = nodess.next();
			String comp = cc.getConnectedComponentOf(n).toString();
			comp = comp.substring(comp.lastIndexOf("#")+1); //components numbering from zero
			debug("Node: "+IPlastHex(n.toString())+" belongs to component No "+comp);
			try {
				debug("FCC: Searching Node "+IPlastHex(n.toString())+" for incoming edges");
				Edge e = n.getEnteringEdge(0);
				
				//TODO: Do we need the edges of the node under attack?
				
			}catch(IndexOutOfBoundsException NE) {
				debug("FCC: Node "+n+" nominated as a 'mother node'.");
				motherNodes.add(n);
			}
		}
		return motherNodes;
	}
/***************************************************************************/
	/* Convert the last part of IPv6 to DEC for short print */
	private int IPlastHex(String IPv6) {
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
}
