package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import server.Item;
import server.MessageServerHandler;

public class Auctioneer implements Runnable {
	
	private List<Item> bidItems = new ArrayList<Item>();
	private List<RegTableEntry> regTable = new ArrayList<RegTableEntry>();
	private Item currentItem;
	//For the bidders interested on the current item we can have either a list (replicated data) 
	//or an integer "interested" field on regTable entry
	private List<RegTableEntry> interestedBidders = new ArrayList<RegTableEntry>(); //interested
	private int highestBid;
	private String highestBidder;
	MessageServerHandler handler = new MessageServerHandler(this);
	
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

	public Item getCurrentItem() {
		return currentItem;
	}

	public void setCurrentItem(Item currentItem) {
		this.currentItem = currentItem;
	}
	
	//TODO
	public void addToRegTable(RegTableEntry entry) {
		String temp = (entry.getBidder()).getBidderName();
		int flag = 0;
		for (RegTableEntry entry2 : regTable) {
			if (temp.equals((entry2.getBidder()).getBidderName())) {
				String message = "9 duplicate_name Please abort";
				handler.sendMessage(message,entry);
				flag = 1;
				break;
			}
		}
		if (flag==0)
			regTable.add(entry);
	}
	
	//TODO
	public void addToInterestedBidders(RegTableEntry entry) {
		interestedBidders.add(entry);
	}
	
	//TODO
	public void receiveBid(int amount, int itemId, RegTableEntry entry) {
		if (itemId == currentItem.getItemId()) {
			if (currentItem.getCurrentPrice() < amount) {
				currentItem.setCurrentPrice(amount);
				currentItem.setHighestBidderName((entry.getBidder()).getBidderName());
				String message = "6 new_high_bid" + ' ' + currentItem.getCurrentPrice() + currentItem.getHighestBidderName();
				for (RegTableEntry entry2 : interestedBidders) {
					handler.sendMessage(message, entry2);
				}
			}
		}
		//check if valid amount
		//change item's current price, highest bidder
		//If many auctioneers synchronize!!
	}
	
	public void bidItem() {
		//for all bidders in regtable send message bid_item
		for (RegTableEntry entry : regTable) {
			String message = "4 new_item" + ' ' + currentItem.getItemId() + ' ' + currentItem.getInitialPrice() + ' ' + currentItem.getDescription();
			handler.sendMessage(message, entry);
		}
		//handler.sendMessage("bid_item")
	}
	
	public void startBidding() {
		String message = "5 start_bidding" + ' ' + currentItem.getItemId() + ' ' + currentItem.getInitialPrice();
		for (RegTableEntry entry : interestedBidders)
			handler.sendMessage(message, entry);
	}

	public void newHighBid() {
		String message = "6 new_high_bid" + ' ' + currentItem.getCurrentPrice() + ' ' + currentItem.getHighestBidderName() + ' ' + currentItem.getItemId();
		for (RegTableEntry entry : interestedBidders)
			handler.sendMessage(message, entry);
	}
	
	public void stopBidding() {
		String message = "7 stop_bidding" + ' ' + currentItem.getCurrentPrice() + ' ' + currentItem.getHighestBidderName() + ' ' + currentItem.getItemId();
		for (RegTableEntry entry : interestedBidders)
			handler.sendMessage(message, entry);
	}
	
	public void auctionComplete() {
		String message = "8 auction_complete";
		for (RegTableEntry entry : regTable)
			handler.sendMessage(message, entry);
	}
	
	
	
	//What an auctioneer does
	public void run() {
		System.out.println("Auctioneer up and running!");
		
		//listens on 2 ports: 1 for bidders 1 for bids
		//Server side socket
		int bidderPort = 2223;
		ServerSocket serverSocket = null;
		try {
			//backlog queue size = 5 (not working yet)
			serverSocket = new ServerSocket(bidderPort, 5);
			System.out.println("Server side socket for bidders is up.");
		} catch (IOException e) {
			System.out.println("Couldn't listen on bidders port");
		}
		
		
		//Auction will end when all items sold etc.
		boolean auctionIsUp = true;
		//Loop constantly waiting for bidders to connect
		Socket clientSocket = null;
		
		int time=1;
		int L=1;
		int interested=1;
		int bidded=1;
		int offer_is_on=1;
		int counter=1;
		
		for (Item item : bidItems){
			
			//L time to connect
			while (time<L){
				try {
					clientSocket = serverSocket.accept();
					String message="temp message";
					Socket socketid=null;
					handler.receiveMessage(message,socketid);
					System.out.println("Bidder connects to Auctioneer");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}//
			
			//set currentItem=item
			currentItem.setItemId(item.getItemId());
			currentItem.setInitialPrice(item.getInitialPrice());
			currentItem.setDescription(item.getDescription());
			currentItem.setCurrentPrice(item.getCurrentPrice());
			currentItem.setHighestBidderName(item.getHighestBidderName());

			//send start bidding to all
			this.bidItem();
			
			//receive i am interested messages
			while (time<L){
				try {
					clientSocket = serverSocket.accept();
					String message="temp message";
					Socket socketid=null;
					handler.receiveMessage(message,socketid);
					System.out.println("Bidder connects to Auctioneer and others send i_am_interested");
					interested++;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}//
			
			if (interested>=2){
				
				this.startBidding();
				
				while (offer_is_on==1){
					
				
					while (time<L) {
					
						String message="one of the bids";
						Socket socketid=null;
						int res=handler.receiveMessage(message, socketid);
						if (res==2) {
							time=0;
							bidded++;
						}
					}
					
					if (bidded==0){
						currentItem.setInitialPrice(0.9*currentItem.getInitialPrice());
						counter++;
						if (counter>=6){
							offer_is_on=0;
						}
						else
							this.newHighBid();						
					}
					else {
						this.stopBidding();
						offer_is_on=0;
					}
				}
			}
				
			
		}
		
		this.auctionComplete();
		
		
	}

}
			
	
