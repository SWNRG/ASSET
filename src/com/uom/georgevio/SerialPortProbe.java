package com.uom.georgevio;

import com.fazecast.jSerialComm.SerialPort;

public class SerialPortProbe {
	
	private SerialPort motePort = null;
	
	private static final int searchUpToNum = 22;
	private static final int searchFromNum = 15; /* Faster search. Be careful */
	
	public SerialPort getMotePort() {
		return motePort;
	}

	private void setMotePort(SerialPort motePort) {
		this.motePort = motePort;
	}    	
	
	protected static volatile boolean portFound = false;
	
	/***** Return the pts port used by Cooja ***********/	
	public void searchPort() {	
	
		String portName = null;

		while(!portFound) {
				for(int p = searchFromNum; p < searchUpToNum; p++) {
					 portName = "/dev/pts/"+p;
					/********* Set & open the serial port **************/            
					debug("Opening port: "+ portName);
					motePort = SerialPort.getCommPort(portName);
					motePort.closePort();
					motePort.setBaudRate(115200);
					//motePort.setParity(1);
					motePort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
					if(motePort.openPort()){
						debug("SUCCESS! Serial Port found: "+portName);
						setMotePort(motePort);
						portFound = true;
						break;
					} 
				}
				if(!portFound) {
					debug("Serial Port not found from pts/"+searchFromNum+" to pts/"
							+searchUpToNum+". Sleeping for 3 sec...");
					try{
						Thread.sleep(3000);	
					} catch (Exception e) {
						debug(e.toString());
					} 
				}
		}
	}
	
/***********UNIVERSAL PRINT IN GUI OUTPUT ***********************************/		
    private static void debug(String message){
    	Main.debug((message));
	}				
}
