package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
	
	//TODO
	public void addToRegTable(RegTableEntry entry) {
		
	}
	
	//TODO
	public void addToCurrentBidders(RegTableEntry entry) {
		
	}
	
	//TODO
	public void receiveBid(int amount, int itemId) {
		//check if valid amount
		//change item's current price, highest bidder
		//If many auctioneers synchronize!!
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
			
			//New client - bidder connects
			try {
				//blocks on accept until a client connects
				//if i am currently accepting or no more than five waiting
				clientSocket = serverSocket.accept();
				System.out.println("Bidder connects to Auctioneer");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//Any type of messages can come at any point
			
			//TODO: At this point auctioneer receives many messages from multiple bidders
			//see select() so that it can see when any socket has data to be read
			MessageServerHandler handler = new MessageServerHandler(this, clientSocket);
			handler.receiveMessage();
			
			/*Launch auction
			for all items in bidItems
				send new_item(currentItem) to bidders
				receives i_am_interrested_messages mixed with
				receive new_high_bid message
				
				see what happens on timeout L
				if timeout L
					drop 10% of the price
				if no interest or 5 rounds passed go to the next item (loop for rounds?)
			*/
			
			//TODO: Check if end of auction 
			/*if (bidItems.isEmpty()) {
				auctionIsUp = false;
			}*/
		}
		
		
		
		
	}

}
