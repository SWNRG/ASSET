package com.uom.georgevio;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.javafx.FxGraphRenderer;
import org.graphstream.ui.view.Viewer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class GUISetup {
	
	private Stage primaryStage;
	private static Graph graph;
	private TextArea console;
	private TextField nodesOutput;
	private TextField edgesOutput;
	private TextField inDegreeOutput;
	private TextField outDegreeOutput;
	private Button bttnStart;
	 
	//@Override
	public GUISetup(Stage primaryStage) throws IOException {	
		/* for advanced graph effects */
    	System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
    	System.setProperty("org.graphstream.ui", "javafx"); 
    	
    	
    	this.primaryStage = primaryStage;
    	
    	//graph = new SingleGraph("Graph");
		//Viewer viewer = graph.display(true); /* show the graph in a standalone window */
		//viewer.enableAutoLayout();	
		
        Parent root = FXMLLoader.load(getClass().getResource("simpleGUI.fxml"));
        Scene scene = new Scene(root, 700, 1000);
		
        nodesOutput = (TextField) scene.lookup("#nodes");
		edgesOutput = (TextField) scene.lookup("#edges");
		inDegreeOutput = (TextField) scene.lookup("#inDegree");
		outDegreeOutput = (TextField) scene.lookup("#outDegree");
		console = (TextArea) scene.lookup("#console");
        /* Transfer the Client.start() to the GUI button */
        bttnStart = (Button) scene.lookup("#bttnStart");        
        
        primaryStage.setTitle("RPL Controller");
        primaryStage.setScene(scene);
        primaryStage.show(); /* without this, the JavaGUI does not show */


        /*
    	FxViewer v = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
    	v.enableAutoLayout();
    	FxViewPanel panel = (FxViewPanel)v.addDefaultView(false, new FxGraphRenderer());
    	scene = new Scene(panel, 800, 600);
    	primaryStage.setScene(scene);	
		primaryStage.show();  */

	


        //bttnStart.setOnAction(e->thread.start()); 	
        
        
	}	
	
	public TextArea getConsole() {
		return console;
	}
	
	public void out2Console(String message) {
		console.appendText(message);
	}
	
	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}
	
	public static Graph getGraph() {
		return graph;
	}
	
	public void setnodesOutput(int num) {
		//this.nodesOutput.setText(Integer.toString(num));
	}

	private static String formatTime(long millis) {
	    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss.SSS");
	    String strDate = sdf.format(millis);
	    return strDate;
	}
	
    public void debug(String message){
    	message = formatTime(System.currentTimeMillis())+": "+message;
		if (Main.consoleOutputTrue) { /* All output to JavaFX console (TextArea) */ 
	    	if (!message.endsWith("\n"))
	    		message = message+"\n";
			console.appendText(message);	
		}
		else /* standard Java output */
			System.out.println(formatTime(System.currentTimeMillis())+": "+ message+".");

	}
}
