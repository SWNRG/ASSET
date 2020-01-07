package com.uom.georgevio;

/* The JavaFX GUI is ready, it receives some parameters (e.g., nodes and edges numbers)
 * but it is not activated. Uncomment below the line primaryStage.show();
 */
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main extends Application {
    //TODO: private static Logger logger = LogManager.getRootLogger();
	//TODO: Find an algorithm to detect loops and no connection to sink

	
	LogService logservice = new LogService();
	
	/* if true all output will be sent to console, else to standard output */
	public static final boolean consoleOutputTrue = true;
	
	public static final long keepAliveNodeBound = 80000;
	public static final long grayZoneNodeBound = 150000;

	Stage primaryStageLocal = new Stage();
	
    private Scene scene;
    private static TextArea console;
    private static TextField nodesOutput;
    private static TextField edgesOutput;
    private TextField inDegreeOutput;
    private static TextField OutDegreesOutput; 

    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("simpleGUI.fxml"));
        scene = new Scene(root, 700, 1000);
        primaryStageLocal.setTitle("Hello World");
        primaryStageLocal.setScene(scene);
        primaryStageLocal.show(); /* without this, the JavaGUI does not show */

        console = (TextArea) scene.lookup("#console");
        nodesOutput = (TextField) scene.lookup("#nodes");
    	edgesOutput = (TextField) scene.lookup("#edges");
    	inDegreeOutput = (TextField) scene.lookup("#inDegree");
    	OutDegreesOutput = (TextField) scene.lookup("#outDegree"); 
		
        debug("Waiting for \"Start\" button ");

        Client client = new Client();
        Thread thread = new Thread(client);
        thread.start();
        
        /* Transfer the Client.start() to the GUI button */
        Button bttnStart = (Button) scene.lookup("#bttnStart");
         /* only if a thread has not started yet */
        	bttnStart.setOnAction(e->{
	        	if(!thread.isAlive()) {
	        		thread.start();
	        	}
	            else {
	            	debug("Stopping thread");	            	
	            	//client.setExit(true);
	            	thread.interrupt();
	            	try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
	            	debug("Starting thread again");	 
	            	Thread thread1 = new Thread(client);
	            	thread1.start(); 
	            }
        	});

        
        Button bttnStop = (Button) scene.lookup("#bttnStop");
        if(thread.isAlive()) /* only if thread has started already */
        	bttnStop.setOnAction(e->client.setExit(true));
        
        

    }
/******************************************************************************/
    public static void main(String[] args) {
    	/* for advanced graph effects */
    	System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
    	System.setProperty("org.graphstream.ui", "javafx"); 
    	
    	//Application.launch(args);
        launch(args);
    }

	private static String formatTime(long millis) {
	    //SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss.SSS");
	    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
	    return sdf.format(millis);
	}
	
	public static void nodesOutput(String nodes) {
		nodesOutput.clear();
		runLateMsgTextField(nodesOutput, nodes);
	}
	
	public static void edgesOutput(String inEdges) {
		edgesOutput.clear();
		runLateMsgTextField(edgesOutput, inEdges);
	}
	
	public static void totalOutDegreesOutput(String totalOutDegrees) {
		OutDegreesOutput.clear();
		runLateMsgTextField(OutDegreesOutput, totalOutDegrees);
	}
	
	private static void runLateMsgTextField (TextField tf, String message) {
		try {
			Platform.runLater(() -> tf.insertText(0, message));
		} catch (IndexOutOfBoundsException e) {
			console.appendText(e.toString()+"\n");
		}
	}
	
	private static void runLateMsg (String message) {
		try {
			Platform.runLater(() -> nodesOutput.insertText(0, message));
		} catch (IndexOutOfBoundsException e) {
			console.appendText(e.toString());
		}
	}
	
    public static void debug(String message){
    	message = formatTime(System.currentTimeMillis())+": "+message;
		if (consoleOutputTrue) { /* All output to JavaFX console (TextArea) */ 
	    	if (!message.endsWith("\n"))
	    		message = message+"\n";
	    	final String finalMessage = message;
	    	Platform.runLater(() -> console.appendText(finalMessage));	/* without it, console crashes */
		}
		else /* standard Java output */
			System.out.println(formatTime(System.currentTimeMillis())+": "+ message+".");
    }
}

class MyThread implements Runnable{
	private volatile boolean exit = false;
	
	@Override
	public void run() {
		while(!exit){
		
		}
		
	}
    public void stop(){
        exit = true;
    }
}

