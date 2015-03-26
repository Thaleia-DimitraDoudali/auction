package server;

import java.util.ArrayList;
import java.util.List;

public class Auctioneer implements Runnable{
	private List<Item> bidItems = new ArrayList<Item>();
	private List<RegTableEntry> regTable = new ArrayList<RegTableEntry>();
	private int currentItem;
	//For the bidders interested on the current item we can have either a list (replicated data) 
	//or an integer "interested" field on regTable entry
	private List<RegTableEntry> currentBidders = new ArrayList<RegTableEntry>();
	
	//Constructor
	public Auctioneer(List<Item> bidItems){
		this.setBidItems(bidItems);
	}

	//Getters - setters
	public List<Item> getBidItems() {
		return bidItems;
	}

	public void setBidItems(List<Item> bidItems) {
		this.bidItems = bidItems;
	}

	public int getCurrentItem() {
		return currentItem;
	}

	public void setCurrentItem(int currentItem) {
		this.currentItem = currentItem;
	}

	//What an auctioneer does
	public void run() {
		System.out.println("Auctioneer up and running!");
		
		
	}

}
