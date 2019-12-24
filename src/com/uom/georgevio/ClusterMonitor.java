package com.uom.georgevio;

import org.graphstream.graph.Graph;

import javafx.application.Application;
import javafx.stage.Stage;

public class ClusterMonitor extends Application{

	static Graph graph;
	
	public ClusterMonitor(Graph graph) {
		ClusterMonitor.graph = graph;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		// implement clustering to check for nodes with ubnormalities.
		// then instruct the nodes to send more info
		
	}
	
	
	
	// to send the message from Client
	//String message = "SP "+nodeProbed+"\n";
	//send2serial.sendSpecificMessage(message);
	
	
}
