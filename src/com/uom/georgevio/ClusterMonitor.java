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
		
		debug("k-MEANS STARTED with "+numCLusters+" Clusters");
		
		SimpleKMeans kmeans = new SimpleKMeans();
		kmeans.setSeed(10);
		
		//important parameter to set: preserver order, number of cluster.
		kmeans.setPreserveInstancesOrder(true);
		kmeans.setNumClusters(numCLusters);
		
		String pinakas;
		try (StringWriter strOut = new StringWriter()) {
		    try (PrintWriter out = new PrintWriter(strOut)) {		
		    	out.println("@relation Node-attacked");
		    	out.print("@attribute nodeId {");
		    	String lastComma = "";
		    	for (Node node : nodes) {
			    	out.print(lastComma+IPlastHex(node.getId()));
			    	lastComma = ",";
		    	}
		    	out.println("}");
		    	out.println("@attribute UDPRecv numeric");
		    	out.println("@data");
		    	
		    	for (Node node : nodes) {	    
					int UDPRecv = (int) node.getAttribute("UDPRecv");			
					//debug("Cluster: "+ "Node: "+IPlastHex(node.toString())+" UDP Received: "+UDPRecv+"\n");
					
					out.println(IPlastHex(node.toString())+","+UDPRecv);	
				}
		    }
		    pinakas = strOut.toString();
		}	
		debug(pinakas);
		
		BufferedReader bufRead = new BufferedReader(new StringReader(pinakas));

		Instances data = new Instances(bufRead);
		kmeans.buildClusterer(data);
 
		// This array returns the cluster number (starting with 0) for each instance
		// The array has as many elements as the number of instances
		int[] assignments = kmeans.getAssignments();
 
		for (int i=0;i<data.size();i++) {
			debug("Node: "+data.get(i)+" in cluster "+assignments[i]);
			
		}
		
		int i=0;
		for(int clusterNum : assignments) {
		    //debug("Instance "+i+" -> Cluster "+clusterNum+"\n");
		    i++;
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
