package com.uom.georgevio;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.graphstream.graph.Node;
import com.fazecast.jSerialComm.SerialPort;

public class Send2Serial implements Runnable{
	SerialPort motePort;

	public Send2Serial(SerialPort motePort) {
		try {
			this.motePort = motePort;
		} catch (Exception e) {
			debug("motePort is null");
			debug(e.toString());
		}  
	}
	
	public Send2Serial() { /* polymorphism */
		if (motePort == null) {
			debug("Serial Port not set yet...Halting");
		}
	}

    @Override
    public void run(){
    	debug("Send2Serial started...");
    }
    
/***************************************************************************/	
	public void probeNeighbors(Iterable<Node> nodes) {
		//List<Node> shuffled =  New ArrayList<>(nodes);
		
		/* Trying to iterate RANDOMLY */
		Iterator<Node> randomNodes = nodes.iterator();
		
		List<Node> nodeArray = new ArrayList<Node>();
		while (randomNodes.hasNext()) {
			nodeArray.add(randomNodes.next());
		}
		
		Collections.shuffle((List<?>) randomNodes);
		
		//while (randomNodes.hasNext()) {
			//Node node = randomNodes.next();
		for (Node node : nodeArray) {
			debug(" Probing for neighbors the Node\t"+IPlastHex(node.toString()));
			String message = "N1 "+node.toString()+"\n";
			/*
			try {
				send2Serial(message);	
				Thread.sleep(500);	 //trying to keep the serial port from hanging 
			} catch (InterruptedException e) {
				  Thread.currentThread().interrupt();
			}*/
			
			
			/* trying to implement NON-blocking */	
			  new Thread( new Runnable() {
			        public void run()  {
			            try  { Thread.sleep( 1000 ); }
			            catch (InterruptedException ie)  {}
			            send2Serial(message);
			        }
			    } ).start();
			  
			  
		}//while
	}
/***************************************************************************/		
	public void sendSpecificMessage(String message) {		
		//debug("Sending: "+message);
		send2Serial(message);
		//Thread.sleep(1000); /* trying to keep the serial port from hanging */
	}			
/******************** SEND A MESSAGE TO UART PORT **************************/
	private void send2Serial(String message){		
		try{
			OutputStream a = motePort.getOutputStream();			
			
			a.write(message.getBytes());
			a.flush();
		}
		catch(Exception e){
			debug("Error sending to serial port :"+motePort.getSystemPortName()+"!!!");
		}
	}
/***************************************************************************/
	/* Convert the last part of IPv6 to DEC for short print */
	public int IPlastHex(String IPv6) {
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
    private static void debug(String message){
    	Main.debug((message));
	}
}