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

    Handler consoleHandler = null;
    Handler fileHandler = null;
    Formatter simpleFormatter = null;

    public LogService(){
    	
        try{
            //Creating consoleHandler and fileHandler
            consoleHandler = new ConsoleHandler();
            fileHandler  = new FileHandler("./javalog.log");
             
            //Assigning handlers to LOGGER object
            LOGGER.addHandler(consoleHandler);
            LOGGER.addHandler(fileHandler);
             
            //Setting levels to handlers and LOGGER
            consoleHandler.setLevel(Level.ALL);
            fileHandler.setLevel(Level.ALL);
            LOGGER.setLevel(Level.ALL);
             
            LOGGER.config("Configuration done.");
            
             
            LOGGER.log(Level.FINE, "Finer logged");
        }catch(IOException exception){
            LOGGER.log(Level.SEVERE, "Error occur in FileHandler.", exception);
        }         
    }  
    
    public void removeLogger() {
        //Console handler removed
        LOGGER.removeHandler(consoleHandler);
    }
 
}