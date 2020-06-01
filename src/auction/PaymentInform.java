package auction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import jade.core.behaviours.CyclicBehaviour; 
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

// seller si occupa dell'ordine di acquisto
public class PaymentInform extends CyclicBehaviour{
	public void action() {
		//System.out.println("Operation X");
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL); 
		ACLMessage msg = myAgent.receive(mt); // aspetta una accept_proposal
		if (msg != null) {
			// ACCEPT_PROPOSAL Message received. Process it
			String offer = msg.getContent();
			ACLMessage reply = msg.createReply();
			
			if (offer != null) {
				reply.setConversationId("asset-auction");
				reply.setPerformative(ACLMessage.INFORM);
				reply.setContent("PAYMENT: " + offer + " of " + myAgent.getLocalName());
				
				printToFile("BIDDER " + myAgent.getLocalName() + " PAYMENT: " + offer);
			}
			else {
				// The requested book has been sold to another buyer in the meanwhile .
				reply.setPerformative(ACLMessage.FAILURE);
				reply.setContent("not-available");
			}
			myAgent.send(reply);
		}
		else {
			block();
		}
	}
	
	public void printToFile(String value) {
		String fileName = new SimpleDateFormat("yyyyMMddHHmm'.txt'").format(new Date());
		try(FileWriter fw = new FileWriter(fileName, true);
	    BufferedWriter bw = new BufferedWriter(fw);
	    PrintWriter out = new PrintWriter(bw)){
			
			out.println(value);
			    	
		} catch (IOException e) {
		    //exception handling left as an exercise for the reader
		}
	}
}
