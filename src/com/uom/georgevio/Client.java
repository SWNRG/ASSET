package com.uom.georgevio;

import java.util.Scanner;
import javax.annotation.processing.Processor;
import com.fazecast.jSerialComm.SerialPort;

public class Client extends StringProcessor implements Runnable{

	private volatile boolean exit = false; /* to start/restart/stop the thread */
	
	protected static String ipServer; /* hardwire the sink's IP if not found */
	
	public static void setipServer(String ipServer) {
		Client.ipServer = ipServer;
	}
	
	public String getipServer() {
		return ipServer;
	}
	
	int roundsCounter=0;
	
	SerialPortProbe serialportprobe = new SerialPortProbe();

	ClientHelper clienthelper = new ClientHelper();
	
	SerialPort motePort = null;
	
	ClusterMonitor clustermonitor;
	
	StringProcessor stringproccessor = new StringProcessor();
	
	Scanner lineReader;
	String inComingLine;
	
    @Override
    public void run(){
   	 serialportprobe.searchPort();
   	 while (!SerialPortProbe.portFound) {
   		 /* stay here while searching for motePort */
   	 }
   	 motePort=serialportprobe.getMotePort();
   	 clienthelper.setMotePort(motePort);
   	 
   	 while(!Thread.interrupted()) {	 
	    	debug("CLIENT: Ready to read RPL network");
	    	
			/* read serial port output line by line */
			lineReader = new Scanner(motePort.getInputStream());
			
			while(lineReader.hasNextLine() ){	
				inComingLine = lineReader.nextLine();
				
				if(inComingLine!=null){
		
					
					 stringproccessor.matches(inComingLine, clienthelper, roundsCounter);
				
					
					
					

	        		
/*************** PRINTOUTS **********************************/		
	
					clienthelper.probeForHiddenEdges(roundsCounter);
					
					/* Close this from GUI toggle button for less redundant info */
					if(Main.printEdgesInfo)
						clienthelper.printEdgesInfo(roundsCounter);

/******** Checking for orphan nodes (InDegree = 0) occasionally****************/
	        		if(roundsCounter%100==0 && roundsCounter > 100) {/* Every hundred rounds */
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

	        			
/*-----------------------IMPLEMENTING BLACKLISTING AND MITIGATION MEASURES OMITTED */	        		
	        		
	        		} // if(Main.appTimeStarted...

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