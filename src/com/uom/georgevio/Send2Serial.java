package com.uom.georgevio;

import java.io.OutputStream;
import java.util.Iterator;
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
		for (Node node : nodes) {
			debug(" Probing for neighbors Node\t"+IPlastHex(node.toString()));
			String message = "N1 "+node.toString()+"\n";
			try {
				send2Serial(message);	
				Thread.sleep(300);	/* trying to keep the serial port from hanging */
			} catch (InterruptedException e) {
				  Thread.currentThread().interrupt();
			}
		}//while
	}
/***************************************************************************/		
	public void sendSpecificMessage(String message) {		
		try {
			send2Serial(message);
			Thread.sleep(300); /* trying to keep the serial port from hanging */
		} catch (InterruptedException e) {
			  Thread.currentThread().interrupt();
		}	
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