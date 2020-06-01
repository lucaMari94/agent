package auction;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

//-gui -agents "autioneer:auction.AuctioneerAgent(x,100);luca:auction.BidderAgent;marco:auction.BidderAgent"
import jade.core.AID; 
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

// AuctioneerAgent wants to sell an asset 
// (e.g. clothing, furnishing accessories, jewelry, furniture, office equipment, appliances)
// (es. abbigliamento, complementi d’arredo, gioielli, mobili, attrezzature da ufficio, elettrodomestici)
public class AuctioneerAgent extends Agent{
	
	public String asset;
	public String initialPrice;
	
	public AID[] bidderAgents;
	
	@Override
	protected void setup() {
		
		// System.out.println("AUCTIONEER: Hello! I'm an Auctioneer Agent");
		// printToFile("AUCTIONEER: Hello! I'm an Auctioneer Agent");
		Object [] args = getArguments();
		
		if(args != null && args.length > 0) {
			
			asset = (String)args[0];
			initialPrice = (String)args[1];
			
			printToFile("\n-----------START AUCTION-------------");
			printToFile("ASSET: " + asset);
			printToFile("INITIAL PRICE: " + initialPrice + "\n");
			printToFile("-------------------------------------\n");
			
			addBehaviour(new TickerBehaviour(this, 20000){ // classe anonima (non ha un nome)
				protected void onTick() { // comportamento ripetuto ogni 20 secondi
					
					// search for bidders in the yellow pages
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
					
					myAgent.addBehaviour(new RequestPerformed());
				}
			
			});
			
		} else {
			printToFile("No auction available");
			//System.out.println("No auction available");
			
			doDelete();
		}
	}
	
	@Override
	protected void takeDown() {
		printToFile("Auctioneer Agent '"+ getAID().getLocalName() +" terminating'");
		System.out.println("Auctioneer Agent '"+ getAID().getLocalName() +" terminating'");
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
