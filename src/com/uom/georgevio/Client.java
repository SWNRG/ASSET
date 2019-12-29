package com.uom.georgevio;

import com.fazecast.jSerialComm.SerialPort;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import org.apache.commons.math.stat.clustering.Cluster;
import org.graphstream.graph.Edge;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.javafx.FxGraphRenderer;
import org.graphstream.ui.view.Viewer;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Client implements Runnable{

	private volatile boolean exit = false; /* to start/restart/stop the thread */
	
	private static String ipServer; /* hardwire the sink's IP if not found */
	
	int roundsCounter=0;
	
	SerialPortProbe serialportprobe = new SerialPortProbe();

	ClientHelper clienthelper = new ClientHelper();
	
	SerialPort motePort = null;
	
	ClusterMonitor clustermonitor;
	
    @Override
    public void run(){
    	
    	while(!Thread.interrupted()) {

	    	debug("Client searching for Serial port...");	    	
	    	/* it will continue, ONLY IF port is found */
	    	while(motePort == null)
	    		motePort = serialportprobe.getSerialPort();
			debug("Client: Serial port found");
			
			clienthelper.setMotePort(motePort);

	        /* read serial port output line by line */
	        Scanner lineReader = new Scanner(motePort.getInputStream());
			while(lineReader.hasNextLine() ){
				
				String inComingLine = lineReader.nextLine();
				if(inComingLine!=null){
	
	            	if(inComingLine.startsWith("Tentative")){ /* only the sink prints at the serial port */
	        			String[] parts = inComingLine.split("Tentative link-local IPv6 address ",2);
	        			ipServer = "["+parts[1]+"]";
	        			debug("found ipServer: "+ipServer);
	        			clienthelper.setSink(ipServer); /* ONCE ONLY, at the beginning */
	        			
	
	        			//clienthelper.checkNode(ipServer);
	        			
		
	        		}/* end if InPut.startsWith("Tentative") */
	        		
	            	if (inComingLine.startsWith("Route")){
	        			String[] parts = inComingLine.split(" ",4);
	        			String ip1 = parts[1];
	        			String ip2 = parts[2];
	        			String[] ltime = parts[3].split(":",2);
	        			String lt = ltime[1];
	        			int intlt = Integer.parseInt(lt);
	        			//TODO: use the intlt in the graph
	
	        			if(ip1.equals(ip2)) {/* node is a direct child of sink's */
	        				debug("found a direct child of sink: "+ip1);
	        				if(ipServer == null) {
	        					debug("ATTENTION: Found a sink's child, SINK's IP IS NOT SET YET!");
	        					ipServer = "[fe80:0000:0000:0000:c30c:0000:0000:0001]";
	        					//clienthelper.setSink(ipServer);
	        				}
	        				clienthelper.checkEdge(ipServer,ip2);
	        			}else { /* ip1 != ip2 */
	            			clienthelper.onlyAddNodeifNotExist(ip1); 
	            			clienthelper.checkNode(ip2);  
	        			}
	        			
	        		}/* end if InPut.startsWith("Route") */
	        		 
	        		if(inComingLine.startsWith("NP")){
	        			try{
	        				String[] parts = inComingLine.split("NP:",2);
	        				parts = parts[1].split(" ",3);
	        				String ipParent = parts[0];
	        				String ipChild = parts[2];	        					
	        				debug("R:"+roundsCounter+" edge "
	        						  + clienthelper.IPlastHex(ipParent)
	        						  + "-->"+clienthelper.IPlastHex(ipChild));

	        				clienthelper.onlyAddNodeifNotExist(ipParent);
	        				clienthelper.checkNode(ipChild);
	        				clienthelper.checkEdge(ipParent, ipChild);	
	        			
	        			}catch (ArrayIndexOutOfBoundsException e) {
	        				debug(e.toString());
	        			}
	        		} /* end if InPut startsWith "NP" */
	        		        		
	        		/* Nodes were forced to send neighbors. They don't necessary 
	        		 * have an edge with those neighbors, so we only add the node 
	        		 * if it does not exist, BUT NOT the edge. 
	        		 * After that, we need to ask the node to print its father.
	        		 */
	        		if(inComingLine.startsWith("N1")){
	        			try{
	        				String[] parts = inComingLine.split("N1:",2);
	        				parts = parts[1].split(" ",3);
	        				String neighbor = parts[0];
	        				String nodeProbed = parts[2];	        				

	        				/* sometimes an IP "0000" comes along */
	        				if(clienthelper.legitIncomIP(neighbor) && 
	        				   clienthelper.onlyAddNodeifNotExist(neighbor)) {
	        					/* The node exists(?) but there was no contact with it yet */
		        					debug("found a NEW neighbor! Lets probe its parents");
		        					String message = "SP "+neighbor+"\n";	
		        					clienthelper.sendMsg2Serial(message);
	        				}
		        			if(clienthelper.legitIncomIP(nodeProbed) && 
		        			   clienthelper.checkNode(nodeProbed)) {
		        					debug("found a NEW node! Lets probe its parents");
		        					String message = "SP "+nodeProbed+"\n";
		        					clienthelper.sendMsg2Serial(message);
	        				}
		        			
	        			}catch (ArrayIndexOutOfBoundsException e) {
	        				debug("could not break apart: "+inComingLine);
	        				debug(e.toString());
	        			}
	        		}
	
	        		if(inComingLine.startsWith("Custom ")){ /* Custom Data coming from node */
	        			try{
	        				String[] parts = inComingLine.split("from ",2);
	        				String nodeAlive = parts[1];	        				
	
		        			if(clienthelper.legitIncomIP(nodeAlive)) {
		        				clienthelper.checkNode(nodeAlive); /* it will also reset the keepAliveTimer */	
		        				
		        				
		        				
		        				
		        				
		        				
		        				
		        				
		        				
		        				
		        				
		        				
		        				clienthelper.addRecvdPacket(nodeAlive); /* keep the num of received data packets */
		        			
		        			
		        			
		        			
		        			
		        			
		        			
		        			
		        			
		        			
		        			}
	
	        			}catch (ArrayIndexOutOfBoundsException e) {
	        				debug("could not break apart: "+inComingLine);
	        				debug(e.toString());
	        			}
	        		}
	        		
	        		
/*************** PRINTOUTS **********************************/		
	
					clienthelper.probeForHiddenEdges(roundsCounter);
					clienthelper.printEdgesInfo(roundsCounter);
	
/******** Checking for nodes with degree == 0 occasionally****************/
	        		if(roundsCounter%20==0) {/* Every fifty rounds */
		        		clienthelper.getInDegrees(roundsCounter); /* Just in case there is someone hiding? */
		        		
		        		//TODO: This is not needed anymore?
	        		}
/********* IP of sink was probably lost by now. Hardwire it...*********/      
	        		
	        		
	        		/*
	        		if(roundsCounter > 9 && ipServer == null){
	        			ipServer = "[fe80:0000:0000:0000:0212:7401:0001:0101]";
	        			debug("R:"+roundsCounter+" Setting ipServer by value...");
	        			clienthelper.setSink(ipServer);
	        		}
	        		*/
	        		
	        		
	        		
	        		
	        		
/*********************kMeans on UDPRecv. Clustering =2 ********/        		
	        		
	        		
	        		
	        		
	        		

	        		clienthelper.runKMeans(2); 
	        		
	        		
	        		
	        		
	        		
	        		
	        		
	        		
/*********************End of reading the serial lines ********/
				 }/* end if InPut!=null */
				
	    		 roundsCounter++;
	    		 
			}/* end while nextline() */
			
			clienthelper.removeGraph();
			debug("Closing scanner...Program ended");
			lineReader.close(); /* Normally, it should never come here, unless exit == true */
	
    	}/* end while(!exit) */
    	
    }/* end run() */
/**************END OF run()***********************************/
    
    public void setExit(boolean exit) {
    	/* stopping the runnable Client */
    	this.exit = exit;
    }

/***************************************************************************/    
	private void debug(String message){
		Main.debug((message));
	}
/***************************************************************************/
} //Client class