package server;
import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.*;

import server.Item;
import server.MessageServerHandler;

public class Auctioneer implements Runnable {
	
	private List<Item> bidItems = new ArrayList<Item>();
	private List<RegTableEntry> regTable = new ArrayList<RegTableEntry>();
	private Item currentItem;
	private List<RegTableEntry> interestedBidders = new ArrayList<RegTableEntry>(); //interested
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
	
	public void removeFromRegTable(RegTableEntry entry) {
 		regTable.remove(entry);
 	}

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
	
	public void removeFromInterestedBidders(RegTableEntry entry) {
		interestedBidders.remove(entry);
	}
	
	public void addToInterestedBidders(RegTableEntry entry) {
		interestedBidders.add(entry);
	}
	
	
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
		ServerSocketChannel ssc=null;
		InetSocketAddress isa;
		Selector selector=null;
	   
		try {
			selector= Selector.open();
			ssc = ServerSocketChannel.open();
			isa = new InetSocketAddress( bidderPort );
			ssc.bind( isa );
			ssc.configureBlocking( false );
			int ops = ssc.validOps();
			ssc.register(selector, ops, null);
		} catch (IOException e) {
			System.out.println("Couldn't listen on bidders port");
		}
		
		
		//Auction will end when all items sold etc.
		int L=1;
		long tStart,tEnd;
		tEnd = System.currentTimeMillis();
		tStart = System.currentTimeMillis();
		//Loop constantly waiting for bidders to connect
		while ((tEnd-tStart)<L){
			try {
				
				int num = selector.select();
				if (num == 0) {
			          continue;
			        }
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> iter = selectedKeys.iterator();
		        
		        while (iter.hasNext()) {
		        	 
		        	SelectionKey key = (SelectionKey) iter.next();
		        	 
		        	if (key.isAcceptable()) {
		        		 
		        		// Accept the new client connection
		        		SocketChannel client = ssc.accept();
		        		client.configureBlocking(false);
		        		 
		        		// Add the new connection to the selector
		        		client.register(selector, SelectionKey.OP_READ);
		        		}
		        }
		        
				tEnd = System.currentTimeMillis();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		int interested=0;
		int bidded=0;
		int offer_is_on=1;
		int counter=0;
		
		
		for (Item item : bidItems){
			
			counter=0;
			interested=0;
			bidded=0;
			//set currentItem=item
			currentItem.setItemId(item.getItemId());
			currentItem.setInitialPrice(item.getInitialPrice());
			currentItem.setDescription(item.getDescription());
			currentItem.setCurrentPrice(item.getCurrentPrice());
			currentItem.setHighestBidderName(item.getHighestBidderName());

			//send start bidding to all
			this.bidItem();
			
			//receive i am interested messages or new connections
			tEnd = System.currentTimeMillis();
			tStart = System.currentTimeMillis();
			
			while ((tEnd-tStart)<L){
				
				
				try {
					
					int num = selector.select();
					if (num == 0) {
				          continue;
				        }
					Set<SelectionKey> selectedKeys = selector.selectedKeys();
					Iterator<SelectionKey> iter = selectedKeys.iterator();
			        
			        while (iter.hasNext()) {
			        	 
			        	SelectionKey key = (SelectionKey) iter.next();
			        	 
			        	if (key.isAcceptable()) {
			        		 
			        		// Accept the new client connection
			        		SocketChannel client = ssc.accept();
			        		client.configureBlocking(false);
			        		// Add the new connection to the selector
			        		client.register(selector, SelectionKey.OP_READ);
			        		}
			        	else if (key.isReadable()) {
			        			 
			        		// Read the data from client 
			        		SocketChannel client = (SocketChannel) key.channel();
			        		int res=handler.receiveMessage(client);
			        		if (res==1) interested++;
			        		}
			        }
			        
					tEnd = System.currentTimeMillis();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			       
			}
			
			if (interested>=2){
				
				this.startBidding();
				
				while (offer_is_on==1){
					
					tEnd = System.currentTimeMillis();
					tStart = System.currentTimeMillis();
				
					//receive bids or new connections
					while ((tEnd-tStart)<L){
						
						try {
							
							int num = selector.select();
							if (num == 0) {
						          continue;
						        }
							Set<SelectionKey> selectedKeys = selector.selectedKeys();
							Iterator<SelectionKey> iter = selectedKeys.iterator();
					        
					        while (iter.hasNext()) {
					        	 
					        	SelectionKey key = (SelectionKey) iter.next();
					        	 
					        	if (key.isAcceptable()) {
					        		 
					        		// Accept the new client connection
					        		SocketChannel client = ssc.accept();
					        		client.configureBlocking(false);
					        		// Add the new connection to the selector
					        		client.register(selector, SelectionKey.OP_READ);
					        		}
					        	else if (key.isReadable()) {
					        			 
					        		// Read the data from client 
					        		SocketChannel client = (SocketChannel) key.channel();
					        		int res=handler.receiveMessage(client);
					        		if (res==2) {
					        			tStart = System.currentTimeMillis();
					        			bidded++;
					        		}
					        		}
					        }
					        
							tEnd = System.currentTimeMillis();
							
						} catch (IOException e) {
							e.printStackTrace();
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
			
	

