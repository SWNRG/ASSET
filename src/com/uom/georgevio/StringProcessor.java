package com.uom.georgevio;

public class StringProcessor {
   String pattern;


   StringProcessor(){
   	
   }
   
   String[] cases = {
   		"Tentative", 		/* 0. Read & Set the sink */
   		"Route", 	 		/*	1. Details on routing of every node*/
   		"NP", 		 		/*	2. (New) Parent for each node. Essential for constructing graph */
   		"N1", 		 		/*	3. Nodes were forced to send neighbors */
   		"Custom ", 	 		/*	4. Custom Data coming from node, e.g. temp, humidity */
   		"[VA", 				/*	5. Version number increased, indicating Version Attack */
   		"DATA Intercept",	/*	6. Sent by the ATTACKER. Used for statistics*/
   		"[SI:"				/*	7. ICMP statistics coming from node */
   		};
   
   public void matches(String inComingLine, ClientHelper clienthelper, int roundsCounter) {

   	String[] parts;
   	
	   int i;
	   for(i = 0; i < cases.length; i++)
	   	if(inComingLine.startsWith(cases[i])) break;
	
			switch(i) {
				/* Read & Set the sink / root node. In Cooja it prints a specific message */
				case 0: /* "Tentative": Only the sink prints at the serial port */
					parts = inComingLine.split("Tentative link-local IPv6 address ",2);
					String ipServer = "["+parts[1]+"]";
					debug("found ipServer: "+ipServer);
					clienthelper.setSink(ipServer); /* ONCE ONLY, at the beginning */
				
					//TODO: Is this redundant? ipServer already exists by now?
					//clienthelper.checkNode(ipServer);
					break;
				case 1: /* "Route" */
	     			parts = inComingLine.split(" ",4);
	     			String ip1 = parts[1];
	     			String ip2 = parts[2];
	     			String[] ltime = parts[3].split(":",2);
	     			String lt = ltime[1];
	     			int intlt = Integer.parseInt(lt); //TODO: use the intlt in the graph

	     			if(ip1.equals(ip2)) {/* node is a direct child of sink's */
	     				debug("found a direct child of sink: "+ip1);
	     				if(Client.ipServer == null) {
	     					debug("ATTENTION: Found a sink's child, SINK's IP IS NOT SET YET!");
	     					Client.setipServer("[fe80:0000:0000:0000:c30c:0000:0000:0001]");
	     					//clienthelper.setSink(ipServer); //TODO:Redundant? Sink is always found
	     				}
	     				clienthelper.checkEdge(Client.ipServer,ip2);
	     			}else { /* ip1 != ip2 */
	     				clienthelper.onlyAddNodeifNotExist(ip1); 
	         			clienthelper.checkNode(ip2);  
	     			}  
	     			break;
				case 2: /* "NP": NEW PARENT */
					/* Absolute minimum information to build the graph is the New Parent info */	
					try{
						parts = inComingLine.split("NP:",2);
						parts = parts[1].split(" ",3);
						String ipParent = parts[0];
						String ipChild = parts[2];	        					
						debug("R:"+roundsCounter+" edge "
								  + clienthelper.IPlastHex(ipParent)
								  + "-->"+clienthelper.IPlastHex(ipChild));
			
						clienthelper.onlyAddNodeifNotExist(ipParent);
						clienthelper.checkNode(ipChild);
						clienthelper.checkEdge(ipParent, ipChild);	
					
					}catch (ArrayIndexOutOfBoundsException e) {
						debug("NP line problem: "+ e.toString());
					}
					break;
				case 3:
			  		/* Nodes were forced to send neighbors. They don't necessary 
			  		 * have an edge with those neighbors, so we only add the node 
			  		 * if it does not exist, BUT NOT the edge. 
			  		 * After that, we need to ask the node to print its father.
			  		 */
					try{
	     				parts = inComingLine.split("N1:",2);
	     				parts = parts[1].split(" ",3);
	     				String neighbor = parts[0];
	     				String nodeProbed = parts[2];	        				

	     				/* sometimes an IP "0000" comes along */
	     				if(clienthelper.legitIncomIP(neighbor) && 
	     				   clienthelper.onlyAddNodeifNotExist(neighbor)) {
	     					/* The node exists(?) but there was no contact with it yet */
	        					debug("found a NEW neighbor! Lets probe its parents");
	        					String message = "SP "+neighbor+"\n";	
	        					clienthelper.sendMsg2Serial(message);
	     				}
	        			if(clienthelper.legitIncomIP(nodeProbed) && 
	        			   clienthelper.checkNode(nodeProbed)) {
	        					debug("found a NEW node! Lets probe its parents");
	        					String message = "SP "+nodeProbed+"\n";
	        					clienthelper.sendMsg2Serial(message);
	     				}
	     			}catch (ArrayIndexOutOfBoundsException e) {
	     				//debug("could not break apart: "+inComingLine);
	     				debug("N1 line problem: "+e.toString());
	     			}				
					break;
				case 4: /* "Custom " Custom Data coming from node */
	     			try{
	     				parts = inComingLine.split("from ",2);
	     				String nodeAlive = parts[1];	        				

	        			if(clienthelper.legitIncomIP(nodeAlive)) {
	        				clienthelper.checkNode(nodeAlive); /* it will also reset the keepAliveTimer */			        				
	        				clienthelper.addRecvdPacket(nodeAlive); /* keep the num of received data packets */
	        			}
	     			}catch (ArrayIndexOutOfBoundsException e) {
	     				//debug("could not break apart: "+inComingLine);
	     				debug("Custom line problem: "+e.toString());
	     			}	
	     			break;
				case 5: /* "[VA" Version attack(s) number */
        			try{
        				parts = inComingLine.split(" from ",2);
        				String nodeUnderVerAttack = parts[1];	        				
        				
	        			if(clienthelper.legitIncomIP(nodeUnderVerAttack)) {
	        				parts = parts[0].split(":",2);
	        				String verNumAttacks =  parts[1].substring(0, parts[1].length() - 1); /* remove ']' */
		        			clienthelper.checkNode(nodeUnderVerAttack); /* it will also reset the keepAliveTimer */			        				
		        			clienthelper.addVerNumAttacks(nodeUnderVerAttack, verNumAttacks); /* keep the num of version number attacks suffered */
	        			}			
        			}catch (ArrayIndexOutOfBoundsException e) {
        				//debug("could not break apart: "+inComingLine);
        				debug("Version Attack line problem: "+e.toString());
        			}
        			break;
				case 6: /* "DATA Intercept": Sent by the ATTACKER. Used for statistics */
     				Main.debugEssential(inComingLine);					
					break;
					
				case 7: /* ICMP statistics coming from node */
	     			try{
	     				parts = inComingLine.split(" from ",2);
	     				String nodeAlive = parts[1];	        				
	     				
	        			if(clienthelper.legitIncomIP(nodeAlive)) {

	        				// this creates a DOUBLE NODE !!!!!
	        				// The above statement SEEMS WRONG. CHECK AGAIN if in doubt
	        				clienthelper.checkNode(nodeAlive); /* it will also reset the keepAliveTimer */	

	        				parts = parts[0].split(":",2);
	        				parts = parts[1].split(" ",2);
	        				String ICMPRecv = parts[0];
	        				String ICMPSent =  parts[1].substring(0, parts[1].length() - 1); /* remove ']' */
	        				
	        				/* keep the num of Send/Recv ICMP packets */
	        				//clienthelper.addICMPStats(nodeAlive, ICMPRecv, ICMPSent); 
	        				
	        				// using an array now, not one old value above
	        				clienthelper.addICMPArrays(nodeAlive, ICMPRecv, ICMPSent);
	        			}
	     			}catch (ArrayIndexOutOfBoundsException e) {
	     				debug("SI line problem: "+e.toString());
	     			}	
					break;

			} // switch(i)
		} //boolean matches
	/************************************************************/    
	private void debug(String message){
		Main.debug((message));
	}
}