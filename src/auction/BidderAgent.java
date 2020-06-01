package auction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import jade.core.Agent; 
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class BidderAgent extends Agent {
	
	public int budget;
	public int actualValue;
	public boolean interested = false;
	
	public void setup() {
		budget = initialBudget();
		
		Object [] args = getArguments();
		
		if(args != null && args.length > 0) {
			// interested = getRandomBoolean();
			interested = Boolean.parseBoolean((String) args[0]);
			
		}
		
		printToFile("\n-------------------------------------");
		printToFile("INITIALIZATION BIDDER AGENT: " + this.getLocalName());
		printToFile("BUDGET: " + budget);
		printToFile("INTERESTED TO AUCTION: " + interested);
		printToFile("-------------------------------------\n");
		
		// register bidder agent in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("asset-auction");
		sd.setName("JADE-asset-auction");
		dfd.addServices(sd);
		try {
			DFService.register(this,dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		// 3 behaviour 
		
		// CFP
		addBehaviour(new OfferOrRefuse());
		
		// INFORM
		addBehaviour(new OfferRaise()); 
	
		// ACCEPT_PROPOSAL
		addBehaviour(new PaymentInform());
	}
	
	public void takeDown() {
		// DE-REGISTRAZIONE DELL'AGENTE DALLE PAGINE GIALLE		
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		System.out.println("Seller agent '"+ getAID().getLocalName() +" terminating'");
		// printToFile("Seller agent '"+ getAID().getLocalName() +" terminating'");
	}
	
	public int initialBudget() { // range: 100 - 150
		// Simulate an evaluation by generating a random number
		Random r = new Random();
		int low = 100;
		int high = 200;
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

	/*public boolean getRandomBoolean() {
	    Random random = new Random();
	    return random.nextBoolean();
	}*/
}
