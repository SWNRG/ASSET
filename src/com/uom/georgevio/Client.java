package com.uom.georgevio;

import com.fazecast.jSerialComm.SerialPort;
import java.util.Scanner;

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
	    	/* it will continue, ONLY when port is found */
	    	while(motePort == null) {
	    		motePort = serialportprobe.getSerialPort();
	    		//debug("Trying to find motePort...");
	    	}
			debug("CLIENT: Ready to read RPL network");
			clienthelper.setMotePort(motePort);

	        /* read serial port output line by line */
	        Scanner lineReader = new Scanner(motePort.getInputStream());
			while(lineReader.hasNextLine() ){
				
				String inComingLine = lineReader.nextLine();
				if(inComingLine!=null){
					//debug("DEBUGGING IN-LINE:"+inComingLine);
	            	if(inComingLine.startsWith("Tentative")){ /* only the sink prints at the serial port */
	        			String[] parts = inComingLine.split("Tentative link-local IPv6 address ",2);
	        			ipServer = "["+parts[1]+"]";
	        			debug("found ipServer: "+ipServer);
	        			clienthelper.setSink(ipServer); /* ONCE ONLY, at the beginning */
	        			
	        			//TODO: Is this redundant? ipServer already exists by now?
	        			//clienthelper.checkNode(ipServer);

	        		}/* end if InPut.startsWith("Tentative") */
	            	else
	            	if (inComingLine.startsWith("Route")){
	        			String[] parts = inComingLine.split(" ",4);
	        			String ip1 = parts[1];
	        			String ip2 = parts[2];
	        			String[] ltime = parts[3].split(":",2);
	        			String lt = ltime[1];
	        			int intlt = Integer.parseInt(lt); //TODO: use the intlt in the graph
	
	        			if(ip1.equals(ip2)) {/* node is a direct child of sink's */
	        				debug("found a direct child of sink: "+ip1);
	        				if(ipServer == null) {
	        					debug("ATTENTION: Found a sink's child, SINK's IP IS NOT SET YET!");
	        					ipServer = "[fe80:0000:0000:0000:c30c:0000:0000:0001]";
	        					//clienthelper.setSink(ipServer); //TODO:Redundant? Sink is always found
	        				}
	        				clienthelper.checkEdge(ipServer,ip2);
	        			}else { /* ip1 != ip2 */
	        				clienthelper.onlyAddNodeifNotExist(ip1); 
	            			clienthelper.checkNode(ip2);  
	        			}        			
	        		}/* end if InPut.startsWith("Route") */	        		 
	            	else 
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
	        				debug("NP line problem: "+ e.toString());
	        			}
	        		} /* end if InPut startsWith "NP" */
	        		        		
	        		/* Nodes were forced to send neighbors. They don't necessary 
	        		 * have an edge with those neighbors, so we only add the node 
	        		 * if it does not exist, BUT NOT the edge. 
	        		 * After that, we need to ask the node to print its father.
	        		 */
	            	else 
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
	        				//debug("could not break apart: "+inComingLine);
	        				debug("N1 line problem: "+e.toString());
	        			}
	        		}
	            	else
	        		if(inComingLine.startsWith("Custom ")){ /* Custom Data coming from node */
	        			try{
	        				String[] parts = inComingLine.split("from ",2);
	        				String nodeAlive = parts[1];	        				
	
		        			if(clienthelper.legitIncomIP(nodeAlive)) {
		        				clienthelper.checkNode(nodeAlive); /* it will also reset the keepAliveTimer */			        				
		        				clienthelper.addRecvdPacket(nodeAlive); /* keep the num of received data packets */
		        			}
	
	        			}catch (ArrayIndexOutOfBoundsException e) {
	        				//debug("could not break apart: "+inComingLine);
	        				debug("Custom line problem: "+e.toString());
	        			}
	        		}
	            	else /* Version number attack */
	        		if(inComingLine.startsWith("[VA")){ /* Version attack(s) number */
	        			try{
	        				String[] parts = inComingLine.split(" from ",2);
	        				String nodeUnderVerAttack = parts[1];	        				
	        				
		        			if(clienthelper.legitIncomIP(nodeUnderVerAttack)) {
		        				parts = parts[0].split(":",2);
		        				String verNumAttacks =  parts[1].substring(0, parts[1].length() - 1); /* remove ']' */
			        			clienthelper.checkNode(nodeUnderVerAttack); /* it will also reset the keepAliveTimer */			        				
			        			clienthelper.addVerNumAttacks(nodeUnderVerAttack, verNumAttacks); /* keep the num of version number attacks suffered */
		        			}			
	        			}catch (ArrayIndexOutOfBoundsException e) {
	        				//debug("could not break apart: "+inComingLine);
	        				debug("Version Attack line problem: "+e.toString());
	        			}
	        		}	            	
	        		else
	        		/* Info from Attacker(s) when they Start/Stop. Only for logging */	
	        		if(inComingLine.startsWith("DATA Intercept")){ 
        				Main.debugEssential(inComingLine);
        			}
	        		else
	        		if(inComingLine.startsWith("[SI:")){ /* ICMP statistics coming from node */
	        			try{
	        				String[] parts = inComingLine.split(" from ",2);
	        				String nodeAlive = parts[1];	        				
	        				
		        			if(clienthelper.legitIncomIP(nodeAlive)) {

		        				// this creates a DOUBLE NODE !!!!!
		        				// The above statement SEEMS WRONG. CHECK AGAIN if in doubt
		        				clienthelper.checkNode(nodeAlive); /* it will also reset the keepAliveTimer */	

		        				parts = parts[0].split(":",2);
		        				parts = parts[1].split(" ",2);
		        				String ICMPRecv = parts[0];
		        				String ICMPSent =  parts[1].substring(0, parts[1].length() - 1); /* remove ']' */
		        				
		        				/* keep the num of Send/Recv ICMP packets */
		        				//clienthelper.addICMPStats(nodeAlive, ICMPRecv, ICMPSent); 
		        				
		        				// using an array now, not one old value above
		        				clienthelper.addICMPArrays(nodeAlive, ICMPRecv, ICMPSent);
		        			}
	        			}catch (ArrayIndexOutOfBoundsException e) {
	        				debug("SI line problem: "+e.toString());
	        			}
	        		}
	        		
/*************** PRINTOUTS **********************************/		
	
					clienthelper.probeForHiddenEdges(roundsCounter);
					
					/* Close this from GUI toggle button for less redundant info */
					if(Main.printEdgesInfo)
						clienthelper.printEdgesInfo(roundsCounter);

/******** Checking for orphan nodes (InDegree = 0) occasionally****************/
	        		if(roundsCounter%20==0) {/* Every fifty rounds */
		        		clienthelper.getInDegrees(roundsCounter); /* Just in case there is someone hiding? */
		        		
		        		//TODO: This is not needed anymore since we have spanning tree?
	        		}
/********* IP of sink was probably lost by now. Hardwire it...*********/      
	        		/* So far, it was never lost. If it happens, uncomment it
	        		if(roundsCounter > 9 && ipServer == null){
	        			ipServer = "[fe80:0000:0000:0000:0212:7401:0001:0101]";
	        			debug("R:"+roundsCounter+" Setting ipServer by value...");
	        			clienthelper.setSink(ipServer);
	        		}
	        		*/
/*********************kMeans on UDPRecv. Clustering =2 ********/        		

	        		/* Initial delay & GUI button pressed to start */
	        		if(Main.appTimeStarted > 2*Main.keepAliveNodeBound 
	        				&& roundsCounter > 100 
	        				&& Main.kMeansStart /* GUI toggle button */
	        			){
	        			clienthelper.runKMeans(2); /* BE CAREFUL: Nothing else than two for now */
	        		}

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
    public void closeGraphViewer() {
    	/* calling the stopViewer in ClientHelper */
    	clienthelper.closeGraphViewer();
    }
/************************************************************/   
    public void setExit(boolean exit) {
    	/* stopping the runnable Client */
    	this.exit = exit;
    }
/************************************************************/    
	private void debug(String message){
		Main.debug((message));
	}
/***************************************************************************/
} //Client class