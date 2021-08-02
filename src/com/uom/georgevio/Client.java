package com.uom.georgevio;

import com.fazecast.jSerialComm.SerialPort;

import java.util.Scanner;

public class Client extends StringProcessor implements Runnable{
	
	protected static String ipServer; /* Hardware the sink's IP if not found */
	
	int roundsCounter=0;
	
	SerialPortProbe serialportprobe = new SerialPortProbe();
	SerialPort motePort = null;
	
	ClientHelper clienthelper = new ClientHelper();
	
	ClusterMonitor clustermonitor;
	
	StringProcessor stringproccessor = new StringProcessor();
	
	Scanner lineReader;
	String inComingLine;
	
	private volatile boolean running = true; /* to start/restart/stop the thread */

	@Override
	public void run() {

		while (running) {
			try {

				serialportprobe.searchPort();
				while (!SerialPortProbe.portFound) {
					Thread.sleep(100);
					/* stay here while searching for motePort */
				}
				motePort = serialportprobe.getMotePort();
				clienthelper.setMotePort(motePort);
				debug("CLIENT: Ready to read RPL network");

				/* read serial port output line by line */
				lineReader = new Scanner(motePort.getInputStream());

				while (lineReader.hasNextLine()) {

					if ((inComingLine = lineReader.nextLine()) != null) {
		
						/* Possible new messages sent by the sink, should be added here */
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
		        			) {
							clienthelper.runKMeans(2); /* BE CAREFUL: Nothing else than 2 for now */

							/*-----------------------IMPLEMENTING BLACKLISTING AND MITIGATION MEASURES OMITTED */

						} // if(Main.appTimeStarted...
					}/* end if InPut!=null */

					roundsCounter++;

/*********************End of reading the serial lines ********/
				}/* end while nextline() */

			} catch (InterruptedException iE) {
				debug("Client exception: " + iE.toString());
				//lineReader.close();
				running = false;
				break;
			}

		}/* end while(running) */

	}/* end run() */
/**************END OF run()***********************************/

/************************************************************/   
    public void terminate() {
		debug("Client thread is terminating...");
		/* stopping the runnable Client */
		running = false;
	}
/************************************************************/     
 	public static void setipServer(String ipServer) {
		Client.ipServer = ipServer;
	}
/************************************************************/ 	
	public String getipServer() {
		return ipServer;
	}
/************************************************************/    
	private void debug(String message){
		Main.debug((message));
	}
/***************************************************************************/

} //Client class