package com.uom.georgevio;

/* The JavaFX GUI is ready, it receives some parameters
 * (e.g., nodes and edges numbers) but it is not
 * activated. Uncomment below the line primaryStage.show();
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

public class Main extends Application {
	
	//TODO: Implement an algorithm to detect no connection to sink

	static LogService logservice = new LogService();
	private static final boolean logging2File = true;
	
	/* if true all output will be sent to console, else to standard output */
	public static final boolean consoleOutputTrue = true;
	
	/* Start/Stop the kMeans Clustering method in ClientHepler */
	public static boolean kMeansStart = false;
	
	/* Start/Stop the printEdgesInfo from ClientHelper */
	public static boolean printEdgesInfo = true;
	
	/* Start/Stop the cebysev inequality check for outliers in addICMPArrays in ClientHelper */
	public static boolean chebysevIneq = false;
	
	public static final long appTimeStarted = System.currentTimeMillis(); /* Time this Application first started */
	
	// TODO: those times have a GREAT problem with the speed of the emulated network	
	public static final long keepAliveNodeBound = 200000; //it was 80,000 looked small
	public static final long grayZoneNodeBound = 350000;  //it was 150,000 looked small
	
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
	
	private static boolean appRunning = false;
	Client client;
	Thread thread; /* Thread will be started below with a button */
	
	@Override
	public void start(Stage primaryStage) throws Exception {

		Parent root;
		/* in jar export needs the relevant path, in eclipse needs the module name */
		try {
			root = FXMLLoader.load(getClass().getResource("/src/com/uom/georgevio/simpleGUI.fxml"));
		} catch (NullPointerException e) {
			root = FXMLLoader.load(getClass().getResource("simpleGUI.fxml"));
		}

		scene = new Scene(root, 700, 800); /* Size of GUI console output */

		primaryStageLocal.setTitle("ASSET IDS");
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

		/* Transfer the Client.start() to the GUI button */
		bttnStart = (Button) scene.lookup("#bttnStart");
		bttnStart.setOnAction(e -> {

			client = new Client();
			thread = new Thread(client);
			debug("Starting application...");

			if (!thread.isAlive()) {
				thread.start();
				debug("Thread is " + thread.getState());

			} else {
				debug("Thread NOT null, cannot restart it");
				debug("Thread's state BEFORE: " + thread.getState());
				client.terminate();
				thread.interrupt();
				thread.stop();
				debug("Thread's state AFTER: " + thread.getState());
			}
		});

		/* Stop the application button in GUI */
		Button bttnStop = (Button) scene.lookup("#bttnStop");
		bttnStop.setOnAction((event) -> {
			debug("STOP Button pressed...");
			client.terminate();
			thread.interrupt();
			thread.stop();
		});

		/* Start/Stop kMeans button in GUI */
		ToggleButton toggleBttnkMeans =
				(ToggleButton) scene.lookup("#toggleBttnkMeans");
		toggleBttnkMeans.setOnAction(e -> {
			if (toggleBttnkMeans.isSelected()) {
				kMeansStart = true;
			    debug("kMeans turned ON..................");
			} else {
				kMeansStart = false;
				debug("kMeans turned off..................");
		   }
		});
		
     /* Start/Stop Print Edges button in GUI */
      ToggleButton toggleBttnPrintEdgesInfo = 
      			(ToggleButton) scene.lookup("#toggleBttnPrintEdgesInfo");
      toggleBttnPrintEdgesInfo.setOnAction(e -> {
      	if (toggleBttnPrintEdgesInfo.isSelected()) {
      		printEdgesInfo = true;
      		debug("printEdgesInfo turned ON..................");
      	} else {
      		printEdgesInfo = false;
      		debug("printEdgesInfo turned off..................");
      	}
		});
      
      /* Start/Stop Chebysev Inequality button in GUI */
      ToggleButton toggleBttnChebysevIneq = 
      			(ToggleButton) scene.lookup("#toggleBttnChebysevIneq");
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
				//System.out.println(formatTime(System.currentTimeMillis())+": "+ message+".");
				System.out.println(message);
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

