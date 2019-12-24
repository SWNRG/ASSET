package com.uom.georgevio;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.graphstream.algorithm.Kruskal;
import org.graphstream.algorithm.Prim;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;

public class SpanningTree {
	
	static Graph graph;
	
	public SpanningTree(Graph graph) {
		SpanningTree.graph = graph;
	}

	public void spanTreePrim() {

		Prim prim = new Prim("InsideGraph", "Y", "N");
		prim.init(graph);
		prim.compute();
		
		List<Edge> edges = graph.edges()
			    .filter(edge -> edge.getAttribute("InsideGraph")=="N")
			    .collect(Collectors.toList());
		
		prim.clear();
		
		Iterable<Edge> primEdges = edges::iterator; /* Only the once not in the graph */		
		for (Edge edge : primEdges ) {
			//System.out.println(edge.getAttribute("InsideGraph"));
			System.out.println("Nodes outside spanning tree: "+edge.getNode0()+", "+edge.getNode1());
		}
		
		
	}
	

	public List<Edge> spanTreeKruskal() {
  		Kruskal kruskal = new Kruskal("ui.class", "intree", "notintree");
  	  
  		kruskal.init(graph);
  		kruskal.compute();
		
  		List<Edge> edgesP = graph.edges() 
  				/* Only edges outside spanning tree */
			    .filter(edge -> edge.getAttribute("ui.class")=="notintree")
			    .collect(Collectors.toList());
		
		kruskal.clear();

		return edgesP;
	}
}