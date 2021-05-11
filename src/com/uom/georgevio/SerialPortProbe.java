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

	public SerialPort getSerialPort() {		
		StringBuilder portString = new StringBuilder();
		do {
			for (int i=1; i<20; i++) {
				portString.append("dev/pts/");
				portString.append(String.valueOf(i));
				debug("Trying port: "+portString);				
				try{
					/********* Set & open the serial port ***************************/            
					//debug("Opening port:"+ portName);
					motePort = SerialPort.getCommPort(portString.toString());
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
						//return null;
					}		
				} catch (Exception e) {
					debug(e.toString());
				} 
				//motePort=findPort(portString.toString());
				portString.setLength(0);
				portString.append("dev/pts/");
			}
		}while (motePort == null); 
		//debug("OPEN PORT FOUND...");
	/********* Set & open the serial port ***************************/      
		//return motePort;
	}
	
/***********METHODS*******************************************/	
	protected SerialPort findPort(String portName ) {
		try{
			/********* Set & open the serial port ***************************/            
			//debug("Opening port:"+ portName);
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
	
/***********UNIVRSAL PRINT IN GUI OUTPUT ***********************************/		
    private static void debug(String message){
    	Main.debug((message));
	}    				
}
