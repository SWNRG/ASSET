package com.uom.georgevio;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
 
public class LogService {
 
    private static final Logger LOGGER = Logger.getLogger(LogService.class.getName());
    private static final Logger LOGGERBASIC = Logger.getLogger(LogService.class.getName()+"2");
    Handler consoleHandler = null;
    Handler fileHandler = null;
    Handler fileHandler2 = null;
    Formatter simpleFormatter = null;

    public LogService(){
    	System.setProperty("java.util.logging.SimpleFormatter.format", 
                "%5$s%6$s%n");
        try{
            // Creating SimpleFormatter
            simpleFormatter = new SimpleFormatter();
            
            //Creating consoleHandler and fileHandler
            //consoleHandler = new ConsoleHandler();
            fileHandler  = new FileHandler("./javalog.log");
            fileHandler2  = new FileHandler("./essentialLog.log");
            //Assigning handlers to LOGGER object
            //LOGGER.addHandler(consoleHandler);
            LOGGER.addHandler(fileHandler);
            LOGGERBASIC.addHandler(fileHandler2);
            
            // Setting formatter to the handler
            fileHandler.setFormatter(simpleFormatter);
            fileHandler2.setFormatter(simpleFormatter);
            
            //Setting levels to handlers and LOGGER
            //consoleHandler.setLevel(Level.ALL);
            fileHandler.setLevel(Level.FINE);
            LOGGER.setLevel(Level.FINE);

            fileHandler2.setLevel(Level.FINE);
            LOGGERBASIC.setLevel(Level.FINE);
            
        }catch(IOException exception){
            LOGGER.log(Level.SEVERE, "Error occur in FileHandler.", exception);
            LOGGERBASIC.log(Level.SEVERE, "Error occur in FileHandler.", exception);
        }         
    }  
    
    public void logMessage(String message) {
    	LOGGER.log(Level.FINE, message);
    }
    
    public void logBasics(String message) {
    	LOGGERBASIC.log(Level.FINE, message);
    }
    
    public void removeLogger() {
        //Console handler removed
        LOGGER.removeHandler(consoleHandler);
    }
 
}