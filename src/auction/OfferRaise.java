package auction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class OfferRaise extends CyclicBehaviour{
	public void action() {
		
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM); 
		ACLMessage msg = myAgent.receive(mt); // aspetta una inform
		
		if(msg != null) {
			
			printToFile("\n-------------------------------------");
			
			ACLMessage reply = msg.createReply(); // creazione della risposta
			
			String value =  msg.getContent();
			int offer = Integer.parseInt(value) + randomIncrement();
			
			int budget = ((BidderAgent)myAgent).budget;
			
			if(budget >= offer) { // if budget >= value
				
				// propose with increment random value
				reply.setContent(String.valueOf(offer));
				reply.setPerformative(ACLMessage.PROPOSE); 
				
				printToFile("BIDDER " + myAgent.getLocalName() + " NEW PROPOSE: offer: " + offer + "  < " + ((BidderAgent)myAgent).budget);
				
				myAgent.send(reply);
				
			} else { // bidder not-interested -> refuse
				
				reply.setPerformative(ACLMessage.REFUSE);
				reply.setContent("budget not sufficient: " + offer + "  > " + ((BidderAgent)myAgent).budget);
				
				printToFile("BIDDER: " + myAgent.getLocalName() + " REFUSE BECAUSE 'BUDGET NOT SUFFICIENT': " + offer + "  > " + ((BidderAgent)myAgent).budget);
				
				// bidder agent refuse
				// removal from yellow pages
				try {
					DFService.deregister(myAgent); 
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}
				
				myAgent.send(reply);
			
			printToFile("-------------------------------------\n");
			}
		} else {
			// System.out.println("Nessun messaggio trovato in coda"); 
			block(); // COMPORTAMENTO NON ESEGUITO DALLO SCHEDULER
		}
	}
	
	public int randomIncrement() {
		// Simulate an evaluation by generating a random number
		Random r = new Random();
		int low = 1;
		int high = 10;
		return r.nextInt(high-low) + low;
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
	
	public boolean getRandomBoolean() {
	    Random random = new Random();
	    return random.nextBoolean();
	}
	
}