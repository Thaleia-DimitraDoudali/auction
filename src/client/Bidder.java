package client;

import java.util.ArrayList;
import java.util.List;

public class Bidder {
	
	private int bidderId;
	private String bidderName;
	private List<server.Item> itemsBought = new ArrayList<server.Item>();
	
	//Constructor
	public Bidder(int bidderId, String bidderName){
		this.setBidderId(bidderId);
		this.setBidderName(bidderName);
	}

	//Getters - setters
	public int getBidderId() {
		return bidderId;
	}

	public void setBidderId(int bidderId) {
		this.bidderId = bidderId;
	}

	public String getBidderName() {
		return bidderName;
	}

	public void setBidderName(String bidderName) {
		this.bidderName = bidderName;
	}
	
}
