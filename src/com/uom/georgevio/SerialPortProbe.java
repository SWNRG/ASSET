package com.uom.georgevio;

import com.fazecast.jSerialComm.SerialPort;

public class SerialPortProbe {
	
	SerialPort motePort = null; 
	
	public SerialPortProbe() {
		//motePort = getSerialPort();
	}

	public SerialPort returnSerialPort() {
		return motePort;
	}
	
	//TODO: Rewrite with a while loop
	public SerialPort getSerialPort() {
		
		
		String iVal = "";
		String portString = "dev/pts/";
		
		while (motePort == null) {
			for (int i=1; i<20; i++) {
				 iVal=String.valueOf(i);
				 portString+=iVal;
				 System.out.println("Trying port: "+portString);
				 motePort=findPort(portString);
				 portString = "dev/pts/";
			}
		}
	/********* Set & open the serial port ***************************/      
		/* if (motePort == null) 
			//motePort=findPort("dev/pts/1");
		//if (motePort == null) 
			//motePort=findPort("dev/pts/2");
		if (motePort == null)
			motePort=findPort("dev/pts/3");
		if (motePort == null)
			motePort=findPort("dev/pts/4");
		if (motePort == null)
			motePort=findPort("dev/pts/6");
		if (motePort == null)
			motePort=findPort("dev/pts/7");
		if (motePort == null)	
			motePort=findPort("dev/pts/14");
		//if (motePort == null)	
			//motePort=findPort("dev/pts/15");
		if (motePort == null)	
			motePort=findPort("dev/pts/17");
		if (motePort == null)
			motePort=findPort("dev/pts/18");
		if (motePort == null)
			motePort = findPort("dev/pts/19");
		if (motePort == null)
			motePort=findPort("dev/pts/20");
		if (motePort == null)
			motePort=findPort("dev/pts/21"); */
		return motePort;
	}
/***********METHODS*******************************************/	
	protected SerialPort findPort(String portName ) {
		try{
			/********* Set & open the serial port ***************************/            
			debug("Opening port:"+ portName);
			motePort = SerialPort.getCommPort(portName);
			motePort.closePort();
			motePort.setBaudRate(115200);
			//motePort.setParity(1);
			motePort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
			if(motePort.openPort()){
				debug("Serial Port found: "+motePort.getDescriptivePortName());
				//debug("Baud Rate:"+ motePort.getBaudRate());
				//debug(" Parity:"+ motePort.getParity());
				//debug(" Write-Timeout:"+ motePort.getWriteTimeout());
				return motePort;
			} else {
				//debug("Serial Port not found. Check if port number exists in SerialPortProbe.java");
				//debug("Going to sleep for 300ms");
				Thread.sleep(300);
				return null;
			}		
		} catch (Exception e) {
			debug(e.toString());
		} 
		return null;
	}
	
    private static void debug(String message){
    	Main.debug((message));
	}    				
}
