package com.uom.georgevio;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

public class GraphStyling {
	
	private Graph graph = new SingleGraph("Graph");
	
	Viewer viewer;
	
	private boolean viewerStarted = false;
	
	public GraphStyling() { /* constructor */
		if(!viewerStarted) { /* Singleton */
			viewInit(); 
			viewerStarted = true;
		}
	}
	
	String sinkNodeCSS  = "shape:circle;"
						+ "fill-color: green, orange; "
						+ "fill-mode: gradient-diagonal1; "
						+ "size:45px; "
						+ "text-alignment: center; "
						+ "text-style:bold; "
						+ "text-color:blue; "
						+ "text-size:15px;";
		
	String normalNodeCSS = "shape:circle; " 
						+ "	fill-color:yellow, orange; "
						+ " fill-mode: gradient-diagonal1; " 
						+ " size:40px; "
						+ "	text-alignment:center; "
						+ "	text-color:black; "
						+ "	text-style:bold; " 
						+ "	text-size:15px;";
	
	String quietNodeCSS = "shape:circle; "
						+ "fill-color: #EEE3E3, #FFF9F9; "
						+ "fill-mode: gradient-diagonal1; "
						+ "size:40px; "
						+ "text-alignment: center; "
						+ "text-color:red; "
						+ "text-style:bold; "
						+ "text-size:15px;";
	
	String allienNodeCSS = "shape:circle;"
						  + "fill-color: #FF0000, #D5D5D5; "
						  + "fill-mode: gradient-diagonal1; "
						  + "size:40px; "
						  + "text-alignment: center; "
						  + "text-color:white; "
						  + "text-style:bold; "
						  + "text-size:15px;";

	String motherNodeCSS =  "shape:cross;"
						  + "fill-color: #ff4100, #ff4100; "
						  + "fill-mode: gradient-diagonal1; "
						  + "size:50px; "
						  + "text-alignment: center; "
						  + "text-color:white; "
						  + "text-style:bold; "
						  + "text-size:15px;"
			       		  + "shadow-mode:plain;"
						  + "shadow-offset: 4,4;"
						  + "shadow-width:0;"
						  + "shadow-color:#C9C9C9;";

	String underAttackNodeCSS = "shape:diamond;"
							  + "fill-color: #C9C9C9, #FF0000; "
							  + "fill-mode: gradient-diagonal1; "
							  + "size:45px; "
							  + "text-alignment: center; "
							  + "text-color:white; "
							  + "text-style:bold; "
							  + "text-size:15px;"
				     		  + "shadow-mode:plain;"
							  + "shadow-offset: 4,4;"
							  + "shadow-width:0;"
							  + "shadow-color:#C9C9C9;";

	String backgroundCSS = "graph {"
							  + "fill-image: url(\"background.png\");" /* DON'T USE TRANSPARENT BACKGROUND */
							  + "fill-mode: image-tiled;"
							  + "}";
	
/***************************************************************************/	
	public void viewInit() {
		
		/* Insert a background image in the graph */
		graph.setAttribute("ui.stylesheet", backgroundCSS);

		viewer = graph.display(true); /* show the graph in a standalone window */
		viewer.enableAutoLayout();	
	}
/***************************************************************************/	
	public void removeView() {
		try {
			viewer.close();
		} catch(Exception e) {
			debug("Viewer closing failed: "+e.toString());
		}
	}
/***************************************************************************/		
	public Graph returnGraph() { /* getter */
		return graph;
	}
/***************************************************************************/	
	public Node nodeStyle(Node node) {
		node.setAttribute("ui.style", normalNodeCSS);
		node.setAttribute("ui.label", IPlastHex(node.getId())); /* id of node in the graph */
		return node;
	}
/***************************************************************************/	
	public Node nodeStyle(String inNode) {/* polymorphism */
		Node node =graph.getNode(inNode);
		node.setAttribute("ui.style", normalNodeCSS);
		node.setAttribute("ui.label", IPlastHex(inNode)); /* id of node in the graph */
		return node;
	}
/***************************************************************************/	
	public void nodeName(String IPv6) {/* id of node in the graph */ 
		Node node =graph.getNode(IPv6);		
		node.setAttribute("ui.label", IPv6);
	}
/***************************************************************************/	
	public void nodeColorGrey(Node inNode) {
		inNode.setAttribute("ui.style", quietNodeCSS);
		inNode.setAttribute("ui.label", IPlastHex(inNode.getId())); /* id of node in the graph */
	}
/***************************************************************************/	
	public void nodeColorAlien(Node inNode) {
		inNode.setAttribute("ui.style", allienNodeCSS);
		inNode.setAttribute("ui.label", IPlastHex(inNode.getId())); /* id of node in the graph */
	}
/***************************************************************************/	
	public void nodeColorMother(Node inNode) {
		inNode.setAttribute("ui.style", motherNodeCSS);
		inNode.setAttribute("ui.label", IPlastHex(inNode.getId())); /* id of node in the graph */
	}
/***************************************************************************/	
	public void nodeColorMother(String inNode) {/* polymorphism */
		Node node =graph.getNode(inNode);
		node.setAttribute("ui.style", motherNodeCSS);
		node.setAttribute("ui.label", IPlastHex(inNode)); /* id of node in the graph */
	}
/***************************************************************************/	
	public void nodeUnderAttack(Node inNode) {
		inNode.setAttribute("ui.style", underAttackNodeCSS);
		inNode.setAttribute("ui.label", IPlastHex(inNode.getId())); /* id of node in the graph */
	}
/***************************************************************************/	
	public void sinkColor(String inNode) {
		Node node =graph.getNode(inNode);
		node.setAttribute("ui.style", sinkNodeCSS);
		node.setAttribute("ui.label", IPlastHex(inNode)); /* id of node in the graph */
	}
/***************************************************************************/	
	public Edge edgeColorRed(String inEdge) {	
		Edge edge = graph.getEdge(inEdge);
		edge.setAttribute("ui.style","fill-color:red;"
	       		  		+ "shadow-mode:plain;"
	       		  		+ "shadow-offset: 2,0;"
				  		+ "shadow-width:0;"
				  		+ "shadow-color:#C9C9C9;"
		);
		return edge;	
	}
/***************************************************************************/	
	public Edge edgeColorBlack(String inEdge) {	
		Edge edge = graph.getEdge(inEdge);
		edge.setAttribute("ui.style","fill-color:black;");
		return edge;	
	}	
/***************************************************************************/
	/* Convert the last part of IPv6 to DEC for short print */
	public int IPlastHex(String IPv6) {
		int decValue = 0;
		int index = IPv6.lastIndexOf(":");
		String lastPart = IPv6.substring(index+3,index+5);// last hex number (e.g. 0e0e)
		try{
			decValue = Integer.valueOf(lastPart,16);
		}catch(NumberFormatException e) {
			debug(e.toString());
		}
		return decValue;
	}  
/***************************************************************************/    
	private static void debug(String message){
		Main.debug((message));
	}
/***************************************************************************/ 
}
