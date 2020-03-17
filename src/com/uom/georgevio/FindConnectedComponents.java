package com.uom.georgevio;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

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
	
	ConnectedComponents cc = new ConnectedComponents(); /* graphstream algo using Kosaraju style */
	
	List<Node> motherNodes = new ArrayList<>();
	
	FindConnectedComponents(){ 
	}
	
	public List<Node> findCC(Graph graph) {

		//printInGraphEdges(graph); /* use for debugging */
		
		cc.init(graph); /* subgraph of attacked / attacker only */
		cc.compute(); 	
		int ccGraphEnum = cc.getConnectedComponentsCount();
		debug("There are "+ccGraphEnum+" connected (sub)graphs");

		Stream<Node> nodes =graph.nodes();
		Iterator<Node> n = nodes.iterator();
		while (n.hasNext()){
			Node node = n.next();
			String comp = cc.getConnectedComponentOf(node).toString();
			comp = comp.substring(comp.lastIndexOf("#")+1); //components numbering from zero
			debug("Node: "+IPlastHex(node.toString())+" belongs to component No "+comp);

			if(node.enteringEdges().count() == 0) { /* orphan node, hence ATTACKER */
				debug("Node "+IPlastHex(node.getId().toString())+" has no EdgeToward ==> IT IS AN ATTACKER");
				motherNodes.add(node);				
			}
		}
		//TODO: Do we need the edges of the node under attack?	
		return motherNodes;
	}
/***************************************************************************/
	/* Just for debugging purposes */	
	private void printInGraphEdges(Graph graph) {		
		debug("Printing graph Edges....");
		Stream<Edge> e = graph.edges();
		Iterator<Edge> iE = e.iterator();	
		while (iE.hasNext()){
			Edge ed =iE.next();
			debug("Edge n0:"+IPlastHex(ed.getNode0().getId())+" n1:"+IPlastHex( ed.getNode1().getId()));
		}
		debug("End of graph Edges.....\n");
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
