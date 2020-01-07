package com.uom.georgevio;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import org.graphstream.graph.Node;

import weka.clusterers.SimpleKMeans;
import weka.core.Instances;

public class ClusterMonitor{

	public ClusterMonitor() {

	}

	public void kMeans(int numCLusters, List<Node> nodes) throws Exception {
		SimpleKMeans kmeans = new SimpleKMeans();
		kmeans.setSeed(10);
		
		//implement memory: keep the last 10 data sets, so kmeans will run on those?
		// but kmeans has NO MEMORY, hence it does not make sense....
		
		
		/* Partitioning-based clustering
	    K-means clustering
	    K-medoids clustering
	    EM (expectation maximization) clustering
		*/
	    
		/*
		List<Integer> UDPRecv = null;

		for (int i=0; i<10; i++) {
			for (Node node : nodes) {
				debug("Node "+IPlastHex(node.getId())+", UDPRecv:"+(Integer)node.getAttribute("UDPRecv"));
				//UDPRecv.add((Integer) node.getAttribute("UDPRecv"));
			}
		}
		debug("--------End of inserting UDPs into UDPRecv List------------");
		*/
		
		
		
		// And if you don't use k-means? Say, average linkage clustering? 
		// I fear that your answer is overfitting to k-means
		
		//Clustering is a tool to help the human explore a data set, not a automatic thing. 
		// But you will not "deploy" a clustering. They are too unreliable, and a single 
		//clustering will never "tell the whole story".
		
		//You do not use training and testing in unsupervised learning
		
		
		
		/* important parameters to set: preserver order, number of cluster */
		kmeans.setPreserveInstancesOrder(true);
		kmeans.setNumClusters(numCLusters);
		
		String pinakas;
		try (StringWriter strOut = new StringWriter()) {
		    try (PrintWriter out = new PrintWriter(strOut)) {		
		    	out.println("@RELATION Nodes-attacked");
		    	//out.println("@ATTRIBUTE nodeId String");
		    	out.println("@ATTRIBUTE UDPRecv numeric");
		    	out.println("@DATA");
		    	
		    	for (Node node : nodes) {	    		
					//out.println(IPlastHex(node.toString())+","+((int) node.getAttribute("UDPRecv")));	
					out.println(((int) node.getAttribute("UDPRecv")));	// only one parameter
				}
		    }
		    pinakas = strOut.toString();
		}	
		debug(pinakas);
		
		BufferedReader bufRead = new BufferedReader(new StringReader(pinakas));

		Instances data = new Instances(bufRead);
		kmeans.buildClusterer(data);
 

		/* This array returns the cluster number (starting with 0) for each instance
		 * The array has as many elements as the number of instances
		 */
		int[] assignments = kmeans.getAssignments();
		
		int counter = 0;
		for (Node node : nodes) {
			debug("Node: "+IPlastHex(node.getId())+" in cluster "+assignments[counter]);
			counter++;
		}
		
		/* Is there kmeans confidence? if yes, less than certain confidence 
		 * should not consider the classification
		 */
		
		
		/* when this is smaller than 0.1, it correctly classifies nodes */
		debug("--------End clustering. square error = "+kmeans.getSquaredError()+"-------");

	}
	
	
	// to send the message from Client
	//String message = "SP "+nodeProbed+"\n";
	//send2serial.sendSpecificMessage(message);
	
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
