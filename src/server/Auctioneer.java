package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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
		
		while (auctionIsUp) {
			
			for items in bidItems {
			
				highestBid = item.getInitialPrice();
				//PROS TO PARON
				//to time<L tha alla3ei
				//se auto to while sundeontai oi prwtoi bidders
			while (time<L) {
			//New client - bidder connects
			try {
				//blocks on accept until a client connects
				//if i am currently accepting or no more than five waiting
				clientSocket = serverSocket.accept();
				
				//add clientSocket to a list for select later
				
				//busy loop wait connect message of bidder
				//MessageServerHandler handler = new MessageServerHandler(this, clientSocket);
				//has to be connect
				handler.receiveMessage();
				//at this point bidder to regtable
				System.out.println("Bidder connects to Auctioneer");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			while (bidding_for_this_item_is_on) {
						
				//send message bid item
				//wait in while for all bidders to express interest in item
				while (time < L) {
					
					
					clientSocket = serverSocket.accept();

					
					//i should wait for i am interested messages from bidders
				
					//SELECT FROM sockets of all in regtable connected
					 
					//see each clientSocket if it has to read something from the bidder's socket
					//has to read only i_am_interested messages, if other message reject
					
					//if receive message interested receiveMessage()
				}
				
				clientSocket = serverSocket.accept();
				//autounou to socket prepei na to valeis sto sunolo tou select
				//kai apla de tha kaneis kati otan pairneis mhnuma
				
				//if no one interested skip item go to next item
				
				// send start biding message to those interested
				
				while (offer_is_on) {
					
					//an 5 kukloi offer_ison = false
				
					//stelnei ti timh to bid stous bidders
				
					while (time<L) {
						//SELECT apo olous ti exoun na poun
						//if new_high_bid time=0 and send message new high bid to bidders
					}
					if (no_one_bid_sth)
						//10% katw
						//ay3hse tous kuklous xwris offer
						//send new_high_bid message
						//h pigaine sto epomeno antikeimeno
					if (someone_bought)
						//vges apo to while
						//kai pes se olous poios pire ti

					
				}
				
				//TODO: Check if end of auction 
				/*if (bidItems.isEmpty()) {
					auctionIsUp = false;
					send auction_complete message to everyone
				}*/
			
		
		}
		
		
		
		
	}

}
			
	
