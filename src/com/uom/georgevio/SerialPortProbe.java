package com.uom.georgevio;

import com.fazecast.jSerialComm.SerialPort;

public class SerialPortProbe {
	
	private SerialPort motePort = null;
	
	public SerialPort getMotePort() {
		return motePort;
	}

	private void setMotePort(SerialPort motePort) {
		this.motePort = motePort;
	}    	
	
	protected volatile static boolean portFound = false;
	
	/***** Return the pts port used by Cooja ***********/	
	public void searchPort() {	
	
		String portName = null;
		
		while(!portFound) {
			try{
				for(int p = 1; p < 4; p++) {
					 portName = "/dev/pts/"+String.valueOf(p);
					/********* Set & open the serial port **************/            
					debug("Opening port: "+ portName);
					motePort = SerialPort.getCommPort(portName);
					motePort.closePort();
					motePort.setBaudRate(115200);
					//motePort.setParity(1);
					motePort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
					if(motePort.openPort()){
						debug("Serial Port found: "+portName);
						setMotePort(motePort);
						portFound = true;
						break;
					} 
				}
				debug("Serial Port not found... Sleeping for 3 sec");
				Thread.sleep(3000);	
			} catch (Exception e) {
				debug(e.toString());
			} 
		}
	}
	
/***********UNIVERSAL PRINT IN GUI OUTPUT ***********************************/		
    private static void debug(String message){
    	Main.debug((message));
	}				
}
