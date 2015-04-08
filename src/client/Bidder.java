package client;

import java.util.ArrayList;
import java.util.List;

public class Bidder implements Runnable{
	
	//private int bidderId;
	private String bidderName;
	private List<server.Item> itemsBought = new ArrayList<server.Item>();
	
	//Constructor
	public Bidder(String bidderName){
		//maybe id won't be useful
		//this.setBidderId(bidderId);
		this.setBidderName(bidderName);
	}

	//Getters - setters
	//public int getBidderId() {
	//	return bidderId;
	//}

	//public void setBidderId(int bidderId) {
	//	this.bidderId = bidderId;
	//}

	public String getBidderName() {
		return bidderName;
	}

	public void setBidderName(String bidderName) {
		this.bidderName = bidderName;
	}

	//What a bidder does - maybe not useful
	//public void run() {
		
	//}
	//TODO: use an item variable that will be used by MessageClientHandler. It can be passed as an argument or I can make set and get functions
	//		This is not needed if handler only decodes messages and calls the bidder to processes the decoded messages. We should talk about this.
	//TODO: make functions that will be called by the message handler to (perhaps process data and) print in terminal.
}
