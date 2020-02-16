package com.uom.georgevio;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import weka.clusterers.HierarchicalClusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.experiment.AveragingResultProducer;

public class ClusterMonitor{

	private List<Node> clusterWithAttackedNodes = new ArrayList<>();
	
	public ClusterMonitor() {
		//debug("ClusterMonitor Class Initialized...");
	}

	Instances data; /* used in both kMeans and Hierarchical clustering */
	

		
		//implement memory: keep the last 10 data sets, so kmeans will run on those?
		// but kmeans has NO MEMORY, hence it does not make sense....

		/* Partitioning-based clustering
	    K-means clustering
	    K-medoids clustering
	    EM (expectation maximization) clustering
		*/
		
		//The following are taken by an interesting net-discussion on k-means...
		
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

		/* TODO: wtf?
		List<Integer> UDPRecv = null;

		for (int i=0; i<10; i++) {
			for (Node node : nodes) {
				debug("Node "+IPlastHex(node.getId())+
					", UDPRecv:"+(Integer)node.getAttribute("UDPRecv"));
				//UDPRecv.add((Integer) node.getAttribute("UDPRecv"));
			}
		}
		debug("--------End of inserting UDPs into UDPRecv List------------");
		*/

	public String getClusters(List<Node> nodes) {
	
		String pinakas = null;
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
		    }catch(Exception e) {
				debug("PrintWriter"+e.toString());
			}
		    pinakas = strOut.toString();
	    }catch(Exception e) {
			debug("StringWriter"+e.toString());
		}	
		//debug(pinakas);
		return pinakas;
	}
/*******************************************************************************/			
	/* kMeans implementation. Needs cluster number in advance.
	 * Important parameters to set: preserver order, number of cluster 
	 */
	public List<Node> kMeans(int numCLusters, List<Node> nodes) throws Exception {
		SimpleKMeans kmeans = new SimpleKMeans();
		kmeans.setSeed(10);
		kmeans.setPreserveInstancesOrder(true);
		kmeans.setNumClusters(numCLusters);
		
		List<Node> cluster0 = new ArrayList<>();
		List<Node> cluster1 = new ArrayList<>();
		
		List<Node> motherNodes2Return = new ArrayList<>();
		
		String pinakas = getClusters(nodes);
		
		BufferedReader bufRead = new BufferedReader(new StringReader(pinakas));

		data = new Instances(bufRead);
		
		kmeans.buildClusterer(data);
		
		/* This array returns the kmeans cluster number (starting with 0) for 
		 * each instance. The array has as many elements as the number of instances.
		 */
		int[] assignments = kmeans.getAssignments();

		/* when this is smaller than 0.1, it correctly classifies nodes */
		if (kmeans.getSquaredError() < 0.1) {
			debug("-------- k-MEANS clustering ------------------");
			//debug("nodes.size="+nodes.size()+" assingments.length="+assignments.length);
			int counter = 0;
			for (Node node : nodes) {
				debug("Node: "+IPlastHex(node.getId())+" with UDP: "
							  +(int) node.getAttribute("UDPRecv")
							  +" in cluster "+assignments[counter]);
				if(assignments[counter] == 0 )
					cluster0.add(node);
				else
					cluster1.add(node);
				counter++;
			}
			DecimalFormat df = new DecimalFormat("#.000");  
			Float shortKMeans = Float.valueOf(df.format(kmeans.getSquaredError()));
			debug("cluster0.size="+cluster0.size()+", cluster1.size="+cluster1.size());
			debug("--------End k-means. kmeans square error = "+shortKMeans+"-------");
			
			if (!cluster0.isEmpty() && !cluster1.isEmpty()) { /* if not all belong to one cluster only */
				debug("both clusters are not empty...");

				if(cluster0.size() < cluster1.size()) { /* for now, the attacked cluster has less nodes */
					motherNodes2Return = analyzeCluster(cluster0);
					clusterWithAttackedNodes = cluster0;
				}
				else {
					motherNodes2Return = analyzeCluster(cluster1);
					clusterWithAttackedNodes = cluster1;
				}
			}
		}
		return motherNodes2Return;
	} // public void kMeans
/*******************************************************************************/						
	/* If kMeans identified two clusters with high confidence,then it should
	 * call an algorithm like kosaraju to graph in strongly connected components
	 * and then find the 'mother' of those different components by graph.
	 * the mother of each subgraph is the attacker.
	 * Optionally, feed the mother to all nodes as a parent to avoid
	 */		
	private List<Node> analyzeCluster(List<Node> cluster0) {
		
		List<Node> motherNodes = new ArrayList<>();
		
		if(cluster0.size()>2) { /* there must be at least three (3) nodes outliers for an attack */
			
			/* subgraph with all the nodes that are under attack */				
			SingleGraph graph2Check = new SingleGraph("graph2Check");

		for(Node node : cluster0) {
			for(Edge edge: node) {
				String edgeId = edge.getId();
				String n0 = edge.getNode0().toString();
				String n1 = edge.getNode1().toString();
				if (graph2Check.getNode(n0) == null) 
					graph2Check.addNode(n0);
				if (graph2Check.getNode(n1) == null) 
					graph2Check.addNode(n1);				
				if (graph2Check.getEdge(edgeId) == null)
					graph2Check.addEdge(edgeId, n0, n1);
			}
		} // for(Node node : cluster0)
			
/* we now have all the nodes under attack in a new graph
 * we can now find how many clusters there are, and find the
 * mother of each cluster. This mother is the attacker(s)
 */					
			FindConnectedComponents fCC = new FindConnectedComponents(graph2Check);
			fCC.findCC();
			motherNodes = fCC.getMotherNodes();

			for(Node node : motherNodes) { /* be careful, they are not the same nodes, just same name */
				debug("node to be mother-colored:"+node);
			}
		}// if(cluster0.size()>2) 
		return motherNodes;
	} //private void analyzeClusters() t
/*******************************************************************************/		
/* Hierarchical clustering implementation. No need for cluster number in advance */
	public void HierarchicalCluster(List<Node> nodes) throws Exception {	
		HierarchicalClusterer hierarchicalclusterer = new HierarchicalClusterer();
		
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
/***************************************************************************/	
	public List<Node> getClusterWithAttackedNodes() {
		return clusterWithAttackedNodes;
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
