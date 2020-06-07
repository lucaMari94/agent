package auction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class RequestPerformed extends Behaviour{
	
	private AID maxOfferBidder = null;
	private int maxOffer;
	
	private int step = 0;
	private int repliesCnt = 0;
	private int proposeNumber = 0;
	
	private MessageTemplate mt;
	
	public void action() {
		
		switch(step) {
			case 0: // sending CFP to all bidder Agents
				
				printToFile("-------------------------------------");
				printToFile("(case 0: SEND CFP)\nAUCTIONEER: " 
						+ myAgent.getLocalName() 
						+ " offers(CFP) the good '" 
						+ ((AuctioneerAgent)myAgent).asset + "'" +  " at the initial price: " 
						+ ((AuctioneerAgent)myAgent).initialPrice);
				
				// System.out.println("-------------------------------------");
				/*System.out.println("AUCTIONEER: " 
						+ myAgent.getLocalName() 
						+ " offers(CFP) the good '" 
						+ ((AuctioneerAgent)myAgent).asset + "'" +  " at the initial price: " 
						+ ((AuctioneerAgent)myAgent).initialPrice);*/
				// System.out.println("-------------------------------------");
				
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for(int i = 0; i < ((AuctioneerAgent)myAgent).bidderAgents.length; i++) {
					cfp.addReceiver(((AuctioneerAgent)myAgent).bidderAgents[i]);
				}
				
				cfp.setContent(String.valueOf(((AuctioneerAgent)myAgent).initialPrice)); 
				//cfp.setContent(((AuctioneerAgent)myAgent).asset);
				
				cfp.setConversationId("asset-auction");
				cfp.setReplyWith("cfp" + System.currentTimeMillis());
				
				// System.out.println("AUCTIONEER: Sending message... " + cfp + "\n");
				printToFile("\nAUCTIONEER: Sending message:\n" + cfp);
				printToFile("-------------------------------------\n");
				myAgent.send(cfp); // sending CFP to all bidder Agents
				
				step = 1;
				
				mt = MessageTemplate.and(
						MessageTemplate.MatchConversationId("asset-auction"), 
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				
				break;
			
			case 1:
				ACLMessage reply = myAgent.receive(mt); // waiting for the answer
				if(reply != null) {
					if(reply.getPerformative() == ACLMessage.PROPOSE) { // if a proposal has been received
						
						proposeNumber++;
						
						int offer = Integer.parseInt(reply.getContent());
						
						printToFile("\n-------------------------------------\n");
						printToFile("(case 1: RECEIVE PROPOSE)\nAUCTIONEER: offer > maxOffer = " + offer + " > " + maxOffer +" ? ");
						
						System.out.println("-------------------------------------");
						System.out.println("AUCTIONEER: offer > maxOffer = " + offer + " > " + maxOffer +" ? ");
						
						if(maxOfferBidder == null || offer > maxOffer) {
							maxOffer = offer;
							maxOfferBidder = reply.getSender();
							printToFile("YES\nAUCTIONEER: NEW MAX offer: " + maxOffer + " of " + maxOfferBidder.getLocalName());
							System.out.println("YES\nAUCTIONEER: NEW MAX offer: " + maxOffer + " of " + maxOfferBidder.getLocalName());
							
						} else {
							printToFile("NO");
							System.out.println("NO");
						}
						printToFile("-------------------------------------\n");
						
					}
					
					// get bidders
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
					
					repliesCnt++;
					
					if(repliesCnt >= bidderAgents.length && bidderAgents.length > 1) { // NEW PROPOSE
						
						step = 2;
						
					} else if(bidderAgents.length == 1 && proposeNumber >= 1){ // ACCEPT PROPOSAL è rimasta una proposta e c'è stata almeno una proposta
						
						step = 3;
					
					} else if(bidderAgents.length == 0){ // REFUSE FROM ALL BIDDERS
						
						step = 4;
						
					}
				} else {
					block();
				}
				
				break;
				
			case 2: // sending CFP to bidder Agents interested with maxOffer value
				
				// INFORM BIDDERS MAX OFFER VALUE
				printToFile("\n-------------------------------------");
				printToFile("\n(case 2: SEND INFORM)\nAUCTIONEER: INFORM MAX OFFER VALUE: " + maxOffer + " of " + maxOfferBidder.getLocalName());
				printToFile("-------------------------------------\n");
				
				// get bidders
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
				
				ACLMessage cfp2 = new ACLMessage(ACLMessage.INFORM);
				for(int i = 0; i < bidderAgents.length; i++) {
					cfp2.addReceiver(bidderAgents[i]);
				}
				
				cfp2.setContent(String.valueOf(maxOffer)); 
				
				cfp2.setConversationId("asset-auction");
				cfp2.setReplyWith("cfp" + System.currentTimeMillis());
				
				myAgent.send(cfp2); // sending INFORM to all bidder Agents
				
				mt = MessageTemplate.and(
						MessageTemplate.MatchConversationId("asset-auction"), 
						MessageTemplate.MatchInReplyTo(cfp2.getReplyWith()));
				
				step = 1;
				
				repliesCnt = 0;
				
				break;

			case 3:
				
				printToFile("\n(case 4)-------------------------------------");
				printToFile("SEND ACCEPT_PROPOSAL TO " + maxOfferBidder.getLocalName());
				printToFile("-------------------------------------\n");
				
				ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				order.addReceiver(maxOfferBidder);
				order.setContent(String.valueOf(maxOffer));
				order.setConversationId("asset-auction");
				order.setReplyWith("cfp" + System.currentTimeMillis());
				myAgent.send(order);
				
				mt = MessageTemplate.and(
						MessageTemplate.MatchConversationId("asset-auction"),
						MessageTemplate.MatchInReplyTo(order.getReplyWith()));
				
				step = 4;
				
				break;
			
			case 4:
				reply = myAgent.receive(mt);
				if(reply != null) {
					
					printToFile("\n\n-------------------------------------");
					
					if(reply.getPerformative() == ACLMessage.INFORM) {
						printToFile("\n\nAuction ended");
						printToFile("Sale occurred");
						printToFile("ASSET SOLD: " + ((AuctioneerAgent)myAgent).asset);
						printToFile("TO BIDDER : " + maxOfferBidder.getLocalName());
						printToFile("FOR: " + maxOffer + " $");
						
						System.out.println("\n\nAuction ended");
						System.out.println("Sale occurred");
						System.out.println("ASSET SOLD: " + ((AuctioneerAgent)myAgent).asset);
						System.out.println("TO BIDDER : " + maxOfferBidder.getLocalName());
						System.out.println("FOR: " + maxOffer + " $");
						
					} else {
						
						printToFile("\n(case 5)\nAUCTION ENDED WITHOUT SALE");
						System.out.println("Auction ended without sale");
						
						
					}
					// System.out.println("-------------------------------------");
					printToFile("-------------------------------------");
					
					step = 5;
					myAgent.doDelete();
					
				} else {
					block();
				}
				break;
				
			}
	}
	
	public boolean done() {
		return (step == 5);
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