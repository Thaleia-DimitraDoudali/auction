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
	
}
