package com.uom.georgevio;

import org.graphstream.algorithm.ConnectedComponents;
import org.graphstream.graph.Graph;

public class ConnectedGraph {
	
	static Graph graph;
	
	ConnectedComponents cc;
	
	public ConnectedGraph(Graph graph) {
		 cc.init(graph);
		 cc = new ConnectedComponents();
	}
	
	public void checkConnectedGraph(Graph graph) {
		cc.compute();
		System.out.println("Connected components: "+cc.getConnectedComponentsCount());
	}

}
