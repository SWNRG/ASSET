package com.uom.georgevio;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import org.graphstream.graph.Node;

import weka.clusterers.HierarchicalClusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.experiment.AveragingResultProducer;

public class ClusterMonitor{

	public ClusterMonitor() {

	}

	public void kMeans(int numCLusters, List<Node> nodes) throws Exception {
		SimpleKMeans kmeans = new SimpleKMeans();
		kmeans.setSeed(10);
		
		//implement memory: keep the last 10 data sets, so kmeans will run on those?
		// but kmeans has NO MEMORY, hence it does not make sense....
		
		HierarchicalClusterer hierarchicalclusterer = new HierarchicalClusterer();
		
		
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
		
		/*
		Average Linkage is a type of hierarchical clustering in which the distance 
		between one cluster and another cluster is considered to be equal to the 
		average distance from any member of one cluster to any member of the 
		other cluster.
		*/	
		
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
		//debug(pinakas);
		
		BufferedReader bufRead = new BufferedReader(new StringReader(pinakas));

		Instances data = new Instances(bufRead);
		
		kmeans.buildClusterer(data);
		
		/* This array returns the kmeans cluster number (starting with 0) for 
		 * each instance. The array has as many elements as the number of instances.
		 */
		int[] assignments = kmeans.getAssignments();
		
		debug("");
		
/* Kmeans implementation. Needs cluster number in advance     */
		/* when this is smaller than 0.1, it correctly classifies nodes */
		if (kmeans.getSquaredError() < 0.1) {
			debug("-------- K-MEANS clustering ------------------");
			int counter = 0;
			for (Node node : nodes) {
				debug("Node: "+IPlastHex(node.getId())+" in cluster "+assignments[counter]);
				counter++;
			}
			DecimalFormat df = new DecimalFormat("#.00");  
			Float shortKMeans = Float.valueOf(df.format(kmeans.getSquaredError()));
			debug("--------End k-means. kmeans square error = "+shortKMeans+"-------");
		}
		
		debug("");
		
/* Hierarchical clustering implementation. No need for cluster number in advance */
		if(hierarchicalclusterer.graph() != null){ /* avoid null pointer exception */
			debug("---------Hierarchy clusters number: "+hierarchicalclusterer.getNumClusters()+"------------");
			hierarchicalclusterer.buildClusterer(data);
			debug("Graph: "+hierarchicalclusterer.graph());
			
			double[] arr;
			for(int i=0; i<nodes.size(); i++) {
			      arr = hierarchicalclusterer.distributionForInstance(data.get(i));
			      double probability = 0;
			      int cluster = -1;
			      for(int j=0; j< arr.length; j++) {
			          if (probability < arr[j]) {
			        	  probability = arr[j];
			        	  cluster = j;
			          }
			      }
		    	  debug("Hierarc.Clust: Node "+IPlastHex(nodes.get(i).toString())+
		        		  "\tbelongs to cluster "+cluster+"\t(with probability "+probability+")");   
			      
			}
			debug("-------------End Hierarcy clustering-----------------");
		}
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
