package com.uom.georgevio;

import com.fazecast.jSerialComm.SerialPort;

public class SerialPortProbe {
	
	SerialPort motePort ; 
	
	public SerialPortProbe() {
		//motePort = getSerialPort();
	}

	public SerialPort returnSerialPort() {
		return motePort;
	}
	
	public SerialPort getSerialPort() {
		
	/********* Set & open the serial port ***************************/            
		if (motePort == null) 
			motePort=findPort("dev/pts/2");
		if (motePort == null)	
			motePort=findPort("dev/pts/17");
		if (motePort == null)
			motePort=findPort("dev/pts/18");
		if (motePort == null)
			motePort = findPort("dev/pts/19");
		if (motePort == null)
			motePort=findPort("dev/pts/20");
		if (motePort == null)
			motePort=findPort("dev/pts/21");
		if (motePort == null)
			motePort=findPort("dev/pts/3");
		if (motePort == null)
			motePort=findPort("dev/pts/6");
		
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
			if(motePort.openPort()==true){
				debug("Baud Rate:"+ motePort.getBaudRate());
				debug(" Parity:"+ motePort.getParity());
				debug(" Write-Timeout:"+ motePort.getWriteTimeout());
				return motePort;
			} else {
				Thread.sleep(300);
				return null;
			}		
		} catch (Exception e) {
			debug(e.toString());
		} 
		return null;
	}
	
    private static void debug(String message){
    	//Main.debug((message));
	}    				
}
