package server;
import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.*;

import server.Item;
import server.MessageServerHandler;
import java.util.Timer;
import java.util.TimerTask;


public class Auctioneer implements Runnable {

	
	private Timer timer;
	private Selector selector;
	private List<Item> bidItems;
	private List<RegTableEntry> regTable = new ArrayList<RegTableEntry>();
	private Item currentItem;
	private List<RegTableEntry> interestedBidders = new ArrayList<RegTableEntry>(); //interested
	MessageServerHandler handler = new MessageServerHandler(this);
	
	//Timer that unblocks selector.select()
	class WakeUp extends TimerTask {
	    public void run() {
	      System.out.println("Timer to the rescue!");
	      selector.wakeup();
	      //timer.cancel(); //Not necessary because we call System.exit
	    }
	  }

	
	//Constructor
	public Auctioneer(List<Item> bidItems){
		this.setBidItems(bidItems);
		this.bidItems = new ArrayList<Item>();
		this.currentItem = new Item(50,50,"nkdsn");
		this.timer = new Timer();
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
				handler.sendMessage(message,entry.getSocketChannel());
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
	
	
	public void receiveBid(double amount, int itemId, RegTableEntry entry) {
		if (itemId == currentItem.getItemId()) {
			if (currentItem.getCurrentPrice() < amount) {
				currentItem.setCurrentPrice(amount);
				currentItem.setHighestBidderName((entry.getBidder()).getBidderName());
				this.newHighBid();
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
			handler.sendMessage(message, entry.getSocketChannel());
		}
		//handler.sendMessage("bid_item")
	}
	
	public void startBidding() {
		String message = "5 start_bidding" + ' ' + currentItem.getItemId();
		for (RegTableEntry entry : interestedBidders)
			handler.sendMessage(message, entry.getSocketChannel());
	}

	public void newHighBid() {
		String message = "6 new_high_bid" + ' ' + currentItem.getCurrentPrice() + ' ' + currentItem.getHighestBidderName() + ' ' + currentItem.getItemId();
		for (RegTableEntry entry : interestedBidders)
			handler.sendMessage(message, entry.getSocketChannel());
	}
	
	public void stopBidding() {
		String message = "7 stop_bidding" + ' ' + currentItem.getCurrentPrice() + ' ' + currentItem.getHighestBidderName() + ' ' + currentItem.getItemId();
		for (RegTableEntry entry : interestedBidders)
			handler.sendMessage(message, entry.getSocketChannel());
	}
	
	public void auctionComplete() {
		String message = "8 auction_complete";
		for (RegTableEntry entry : regTable)
			handler.sendMessage(message, entry.getSocketChannel());
	}
	
	
	
	//What an auctioneer does
	public void run() {
		System.out.println("Auctioneer up and running!");
		
		//listens on 2 ports: 1 for bidders 1 for bids
		//Server side socket
		
		//temporary item list
		Item tempo = new Item(1,100,"none_yet");
		bidItems.add(tempo);
		tempo = new Item(2,244,"non_yet");
		bidItems.add(tempo);
		
		
		//set up connection
		int bidderPort = 2223;
		ServerSocketChannel ssc = null;
		InetSocketAddress isa;
		selector = null;
		InetAddress hostname = null;
		try {
			//Now it's the local host IPv4, it could also be a VM IPv4.
			hostname = InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
	   
		try {
			selector= Selector.open();
			ssc = ServerSocketChannel.open();
			isa = new InetSocketAddress(hostname, bidderPort);
			System.out.println("successful connection!");
			ssc.bind( isa );
			ssc.configureBlocking( false );
			int ops = ssc.validOps();
			ssc.register(selector, ops, null);
		} catch (IOException e) {
			System.out.println("Couldn't listen on bidders port");
		}
		
		
		//Loop constantly waiting for bidders to connect		
		int L=10;
		long tStart,tEnd;
		tEnd = System.currentTimeMillis();
		tStart = System.currentTimeMillis();

		timer.schedule(new WakeUp(), L*1000);
		
		while ((tEnd-tStart)/1000<L){
			
			try {
				
				selector.select();
			         
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
		        		System.out.println("successful accept!");
		        		}
		        	else if (key.isReadable()) {
	        			 
		        		// Read the data from client
		        		//System.out.println("entered readable!");
		        		SocketChannel client = (SocketChannel) key.channel();
		        		int res=handler.receiveMessage(client);
		        		if (res != 10) System.out.format("Received Id: %d %n",res);
		        		if (res==0) System.out.println("entered client!");
		        		}
			        iter.remove();
				}
		        //System.out.println("reached selection!");
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			tEnd = System.currentTimeMillis();
		}
		
		int interested=0;
		int bidded=0;
		int offer_is_on=1;
		int counter=0;
		
		System.out.println("reached list!");
		
		//Auction will end when all items sold etc.
		for (Item item : bidItems){
			System.out.println("item1");
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
			
			System.out.format(" %s ", item.getDescription());
			
			//receive i am interested messages or new connections
			tEnd = System.currentTimeMillis();
			tStart = System.currentTimeMillis();
			
			timer.schedule(new WakeUp(), L*1000);
			
			while ((tEnd-tStart)/1000<L){
				
				try {
					
					selector.select();

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
			        		System.out.println("Successful accept2!");
			        		}
			        	else if (key.isReadable()) {
			        			 
			        		// Read the data from client 
			        		SocketChannel client = (SocketChannel) key.channel();
			        		//System.out.println("Successful is readable");
			        		int res=handler.receiveMessage(client);
			        		if (res==1) interested++;
			        		}
				        iter.remove();
			        }
					
					
				} catch (IOException e) {
					e.printStackTrace();
				}

				tEnd = System.currentTimeMillis();
				
			}
			
			System.out.println("after I am interested!");
			
			//if at least two bidders are interested
			if (interested>=1){
				
				//begin the auction for the curent item
				this.startBidding();
				System.out.println("start bidding was sent!");
				offer_is_on = 1;
				
				while (offer_is_on==1){
					
					tEnd = System.currentTimeMillis();
					tStart = System.currentTimeMillis();
				
					timer.schedule(new WakeUp(), L*1000);
					
					//receive bids or new connections
					while ((tEnd-tStart)/1000<L){
						try {
							
							selector.select();
							
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
					        			timer.cancel();
					        			timer.purge();
					        			timer = new Timer();
					        			timer.schedule(new WakeUp(), L*1000);
					        		}
					        		}
						        iter.remove();
					        }
					        
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						tEnd = System.currentTimeMillis();
					       
					}
					
					System.out.println("after bids were done!");
					
					//check if nobody has bid for the current item
					if ((currentItem.getHighestBidderName()).equals("no_holder")){
						currentItem.setCurrentPrice(0.9*currentItem.getCurrentPrice());
						counter++;
						if (counter>=6){
							offer_is_on=0;
							this.stopBidding();
							System.out.println("count = 6! ");
						}
						else {
							this.newHighBid();
							System.out.println("10 down! ");
						}
							
					}
					else {
						//if a successful bid was placed, stop the bidding process and sell the item
						this.stopBidding();
						offer_is_on=0;
						System.out.println("item sold! ");
					}
				}
			}
				
			//wait for system to stabilize before sending new item
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		System.out.println("reached auction!");
		
		//wait for system to stabilize before sending new message
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//terminate auction
		System.out.println("reached auction2!");
		this.auctionComplete();
		System.out.println("after auction!");
		timer.cancel();
	}

}
			
	

