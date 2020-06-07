package auction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

// CyclicBehavour 
// il bidder partecipa all'asta
// è interessato al bene? 
public class OfferOrRefuse extends CyclicBehaviour{
	public void action() {
		
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
		ACLMessage msg = myAgent.receive(mt);
		
		// printToFile("BIDDER: " + myAgent.getLocalName() + " checking message...");
		
		// System.out.println("BIDDER: " + myAgent.getLocalName() + " checking message...");
		
		if(msg != null) {
			ACLMessage reply = msg.createReply(); // creazione della risposta
			
			boolean interested = ((BidderAgent)myAgent).interested;
			
			String value =  msg.getContent();
			int offer = Integer.parseInt(value) + randomIncrement();
			int budget = ((BidderAgent)myAgent).budget;
			
			AID[] bidderAgents = null;
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("asset-auction");
			template.addServices(sd);
			try {
				DFAgentDescription[] results = DFService.search(myAgent, template);
				bidderAgents = new AID[results.length];
				for(int i = 0; i < results.length; i++) {
					bidderAgents[i] = results[i].getName();
				}
				
			} catch (FIPAException fe) {
				fe.printStackTrace();
			}
			
			if(interested && budget >= offer) { 
				
				reply.setContent(String.valueOf(offer));
				reply.setPerformative(ACLMessage.PROPOSE);
				
				printToFile("BIDDER PROPOSE: " + myAgent.getLocalName() + " offer: " + offer);
				
				myAgent.send(reply);
				
			} else { // bidder not-interested -> refuse
				
				reply.setPerformative(ACLMessage.REFUSE);
				reply.setContent("BIDDER: not-interested");
				
				printToFile("BIDDER REFUSE: BidderAgent '" + myAgent.getLocalName() + "' isn't interested to auction");
				
				// bidder agent is no longer interested in the auction
				// removal from yellow pages
				try {
					DFService.deregister(myAgent); 
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}
				
				myAgent.send(reply);
			}
			
		} else {
			// System.out.println("Nessun messaggio trovato in coda"); 
			block(); // COMPORTAMENTO NON ESEGUITO DALLO SCHEDULER
		}
				
	}
	
	public int randomIncrement() { // range: 1 - 10
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
}
