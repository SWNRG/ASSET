package com.uom.georgevio;

/* The JavaFX GUI is ready, it receives some parameters (e.g., nodes and edges numbers)
 * but it is not activated. Uncomment below the line primaryStage.show();
 */
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.fazecast.jSerialComm.SerialPort;

public class Main extends Application {
	//TODO: Find an algorithm to detect no connection to sink

	static LogService logservice = new LogService();
	private static final boolean logging2File = true;
	
	/* if true all output will be sent to console, else to standard output */
	public static final boolean consoleOutputTrue = true;
	
	/* Start/Stop the kMeans Clustering method in ClientHepler */
	public static boolean kMeansStart = false;
	
	/* Start/Stop the printEdgesInfo from ClientHelper */
	public static boolean printEdgesInfo = false;
	
	/* Start/Stop the cebysev inequality check for outliers in addICMPArrays in ClientHelper */
	public static boolean chebysevIneq = false;
	
	SerialPort motePort = null; /* will be set by the Client class */
	
	public static final long appTimeStarted = System.currentTimeMillis(); /* Time this Application first started */
	
	public static final long keepAliveNodeBound = 80000;
	public static final long grayZoneNodeBound = 150000;
	
	/* How many previous ICMP mean values to keep in order to check Chebyshev outliers */
	public static final int meanICMPHistoryKept = 7;

	/* Papers suggest to stop resetting trickle timer after a certain number of version changes per hour */
	public static final int VERSION_NUM_CHANGES = 20;
	
	Stage primaryStageLocal = new Stage();
	
    private static Scene scene;
    private static TextArea console;
    private static TextField nodesOutput;
    private static TextField edgesOutput;
    private TextField inDegreeOutput;
    private static TextField OutDegreesOutput; 
    private static Button bttnStart;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
    	Parent root;
    	
    	// in jar export needs the relevant path, in eclipse needs the module name
    	try {
    		root = FXMLLoader.load(getClass().getResource("/src/com/uom/georgevio/simpleGUI.fxml"));
    	}catch(NullPointerException e){
    		root = FXMLLoader.load(getClass().getResource("simpleGUI.fxml"));
    	}
        
        scene = new Scene(root, 700, 1000);
    	
        primaryStageLocal.setTitle("IoT SDN Centralized Console");
        primaryStageLocal.setScene(scene);
        primaryStageLocal.show(); /* without this, the JavaGUI does not show */
       
        //TODO: Suspect for crashes?
    	scene.getWindow().setX(0); /* left top of screen */
    	scene.getWindow().setY(0);
           	
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
        bttnStart = (Button) scene.lookup("#bttnStart");
        /* only if a thread has not started yet */
    	
        bttnStart.setOnAction(e->{
        	if(!thread.isAlive()) {
        		thread.start();
        	}
            else {
                     	
            	// TODO: Is this working ???
            	client.closeGraphViewer();

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
        bttnStop.setOnAction((event) -> {
        	  System.out.println("Button clicked");
        	});
        

        
        if(thread.isAlive()) /* only if thread has started already */
        	bttnStop.setOnAction(e->client.setExit(true));
       
        ToggleButton toggleBttnkMeans = (ToggleButton) scene.lookup("#toggleBttnkMeans");
		toggleBttnkMeans.setOnAction(e -> {
			if (toggleBttnkMeans.isSelected()) {
				kMeansStart = true;
			    debug("kMeans turned ON..................");
			} else {
				kMeansStart = false;
				debug("kMeans turned off..................");
		    }
		});
		
        ToggleButton toggleBttnPrintEdgesInfo = (ToggleButton) scene.lookup("#toggleBttnPrintEdgesInfo");
        toggleBttnPrintEdgesInfo.setOnAction(e -> {
			if (toggleBttnPrintEdgesInfo.isSelected()) {
				printEdgesInfo = true;
			    debug("printEdgesInfo turned ON..................");
			} else {
				printEdgesInfo = false;
				debug("printEdgesInfo turned off..................");
		    }
		});
        
        ToggleButton toggleBttnChebysevIneq = (ToggleButton) scene.lookup("#toggleBttnChebysevIneq");
        toggleBttnChebysevIneq.setOnAction(e -> {
			if (toggleBttnChebysevIneq.isSelected()) {
				chebysevIneq = true;
			    debug("chebysevIneq turned ON..................");
			} else {
				chebysevIneq = false;
				debug("chebysevIneq turned off..................");
		    }
		});
     
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
		if (logging2File) /* Only if logfile is needed */
			logservice.logMessage(message);
		
		if (consoleOutputTrue) { /* All output to JavaFX console (TextArea) */ 
	    	if (!message.endsWith("\n"))
	    		message = message+"\n";
	    	final String finalMessage = message;
	    	Platform.runLater(() -> console.appendText(finalMessage));	/* without it, console crashes */
		}
		else /* standard Java output */
			System.out.println(formatTime(System.currentTimeMillis())+": "+ message+".");
    }
    
    public static void debugEssential(String message) {
		if (logging2File){ /* Only if logfile is needed */
			message = formatTime(System.currentTimeMillis())+"\t"+message;
			logservice.logBasics(message);
		}
    }
    
    public static void debugEssentialTitle(String message) {
		if (logging2File){ /* Only if logfile is needed */
			logservice.logBasics(message);
		}
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

