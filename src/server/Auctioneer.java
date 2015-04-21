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

	private int bidderPort;
	private int L;
	private Timer timer;
	private Selector selector;
	private List<Item> bidItems;
	private List<RegTableEntry> regTable = new ArrayList<RegTableEntry>();
	private Item currentItem;
	private List<RegTableEntry> interestedBidders = new ArrayList<RegTableEntry>(); 
	MessageServerHandler handler = new MessageServerHandler(this);
	
	//Timer that unblocks selector.select()
	class WakeUp extends TimerTask {
	    public void run() {
	      //System.out.println("Timer to the rescue!");
	      selector.wakeup();
	    }
	  }

	//Constructor
	public Auctioneer(int L, List<Item> bidItems, int port){
		this.setBidItems(bidItems);
		this.L = L;
		this.currentItem = new Item(0,0,"none_yet");
		this.timer = new Timer();
		this.bidderPort = port;
	}

	//Getters - setters
	public List<Item> getBidItems() {
		return bidItems;
	}

	public void setBidItems(List<Item> bidItems) {
		this.bidItems = bidItems;
		//TODO: Copy items one by one so that each auctioneer has his own bidItems list
	}

	public Item getCurrentItem() {
		return currentItem;
	}

	public void setCurrentItem(Item currentItem) {
		this.currentItem = currentItem;
	}
	
	//Called at quit
	public void removeFromRegTable(RegTableEntry entry) {
		try {
			entry.getSocketChannel().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
 		if (regTable.remove(entry))
 			System.out.println("Entry removed");
 	}
	//Called at connect
	public void addToRegTable(RegTableEntry entry) {
		String temp = (entry.getBidder()).getBidderName();
		int flag = 0;
		//Check for duplicate_name
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
	//Called at quit
	public void removeFromInterestedBidders(RegTableEntry entry) {
		interestedBidders.remove(entry);
	}
	//Called at i_am_interested
	public void addToInterestedBidders(RegTableEntry entry) {
		interestedBidders.add(entry);
	}
	//Called at bid amount
	public int receiveBid(double amount, int itemId, RegTableEntry entry) {
		if ((itemId == currentItem.getItemId()) && (interestedBidders.contains(entry))) {
			if (currentItem.getCurrentPrice() < amount) {
				currentItem.setCurrentPrice(amount);
				currentItem.setHighestBidderName((entry.getBidder()).getBidderName());
				//send new_high_bid
				this.newHighBid();
				return 2;
			}
		}
		return 10;
	}

	//-----Messages that a server sends------
	
	//new_item
	public void bidItem() {
		//send to all registered bidders
		for (RegTableEntry entry : regTable) {
			String message = "4 new_item" + ' ' + currentItem.getItemId() + ' ' + currentItem.getInitialPrice() + ' ' + currentItem.getDescription();
			handler.sendMessage(message, entry.getSocketChannel());
		}
	}
	//start_bidding
	public void startBidding() {
		String message = "5 start_bidding" + ' ' + currentItem.getItemId();
		//send to all interested bidders
		for (RegTableEntry entry : interestedBidders)
			handler.sendMessage(message, entry.getSocketChannel());
	}
	//new_high_bid
	public void newHighBid() {
		String message = "6 new_high_bid" + ' ' + currentItem.getCurrentPrice() + ' ' + currentItem.getHighestBidderName() + ' ' + currentItem.getItemId();
		//send to all interested bidders
		for (RegTableEntry entry : interestedBidders)
			handler.sendMessage(message, entry.getSocketChannel());
	}
	//stop_bidding
	public void stopBidding() {
		String message = "7 stop_bidding" + ' ' + currentItem.getCurrentPrice() + ' ' + currentItem.getHighestBidderName() + ' ' + currentItem.getItemId();
		//send to all interested bidders
		for (RegTableEntry entry : interestedBidders)
			handler.sendMessage(message, entry.getSocketChannel());
	}
	//auction_complete
	public void auctionComplete() {
		String message = "8 auction_complete";
		//send to all registered bidders
		for (RegTableEntry entry : regTable)
			handler.sendMessage(message, entry.getSocketChannel());
		try {
			//ensure message sent to all
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//close all channels
		for (RegTableEntry entry : regTable)
			try {
				(entry.getSocketChannel()).close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	//What an auctioneer does
	@SuppressWarnings("unused")
	public void run() {
		
		System.out.println("Auctioneer up and running!");
						
		//set up connection
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
		
		//cat auct_name so that the bidder can find out the ip and port of the auctioneer
		try {
			String content = "localhost" + " " + bidderPort + " ";
			//The auct_name file will be created at the current directory
			String workingDir = System.getProperty("user.dir");
			File file = new File(workingDir + "/auct_name");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//Channel preparation
		try {
			selector= Selector.open();
			ssc = ServerSocketChannel.open();
			isa = new InetSocketAddress(hostname, bidderPort);
			System.out.println("Successful connection!");
			ssc.bind( isa );
			ssc.configureBlocking( false );
			int ops = ssc.validOps();
			ssc.register(selector, ops, null);
		} catch (IOException e) {
			System.out.println("Couldn't listen on bidders port! Server must be reset!");
			return;
		}

		long tStart,tMid,tEnd=0;
		tStart = System.currentTimeMillis();
		
		//Loop constantly waiting for at least one bidder to connect, so that auction can start		
		while (this.regTable.size() < 1) {

			tEnd = System.currentTimeMillis();//current time
			tMid = System.currentTimeMillis();//start time
			
			//Auction timeout at 3L
			if ((tEnd-tStart)/1000 > 3*L) {
				System.out.println("No bidders, auction was aborted");
				timer.cancel();
				return;
			}
			
			//Set selector's wakeup time, to unblock him
			timer.schedule(new WakeUp(), L*1000); 
		
			//What happens in L sec
			while ((tEnd-tMid)/1000<L){
				try {		
					//select == blocking
					selector.select();
					Set<SelectionKey> selectedKeys = selector.selectedKeys();
					Iterator<SelectionKey> iter = selectedKeys.iterator();
						//Loop through the channels that have been selected
						while (iter.hasNext()) {
							SelectionKey key = (SelectionKey) iter.next();
							//Physical connections
							if (key.isAcceptable()) {		        		 
								//Accept the new client connection
								SocketChannel client = ssc.accept();
								client.configureBlocking(false);
								//Add the new connection to the selector
								client.register(selector, SelectionKey.OP_READ);
							}
							//Read from channel
							else if (key.isReadable()) {	        			 
								//Read the data from client
								SocketChannel client = (SocketChannel) key.channel();
								//Handler's read non blocking
								handler.receiveMessage(client);
							}
							iter.remove();
						}				
				} catch (IOException e) {
					e.printStackTrace();
				}
				tEnd = System.currentTimeMillis();
			}
		}
		
		//Now some users have set up a channel, so auction can begin 
		
		int interested = 0;
		int bidded = 0;
		int offer_is_on = 1;
		int counter = 0;
		
		//System.out.println("reached list!");
		
		//TODO: while (bidItems.length() > 0) - override equals at Item
		for (Item item : bidItems){
			
			counter=0;
			interested=0;
			interestedBidders.clear();
			bidded=0;
			currentItem.setItemId(item.getItemId());
			currentItem.setInitialPrice(item.getInitialPrice());
			currentItem.setDescription(item.getDescription());
			currentItem.setCurrentPrice(item.getCurrentPrice());
			currentItem.setHighestBidderName(item.getHighestBidderName());

			//Send new_item to registered bidders
			this.bidItem();
			
			System.out.format("\nNew item: %s \n", item.getDescription());
			
			tEnd = System.currentTimeMillis();
			tStart = System.currentTimeMillis();
			
			timer.schedule(new WakeUp(), L*1000);

			//receive i am interested messages or new connections
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
			        		//System.out.println("Successful accept2!");
			        		}
			        	else if (key.isReadable()) {			        			 
			        		// Read the data from client 
			        		SocketChannel client = (SocketChannel) key.channel();
			        		//System.out.println("Successful is readable");
			        		int res=handler.receiveMessage(client);
			        		if (res == 1) 
			        			interested++;
			        	}
				        iter.remove();
			        }						
				} catch (IOException e) {
					e.printStackTrace();
				}
				tEnd = System.currentTimeMillis();				
			}
						
			//System.out.println("after I am interested!");
			
			//If at least one bidders are interested
			if (this.interestedBidders.size()>=1){
				
				//begin the auction for the current item
				this.startBidding();
				//System.out.println("start bidding was sent!");
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
					        		//Received bid so restart L
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
										
					//System.out.println("after bids were done!");
					
					//TODO: lazy sync check agreement in winner
					
					//If no one wanted the item = no_holder then reduce price
					if ((currentItem.getHighestBidderName()).equals("no_holder")){
						//TODO: the item should also change on bidItems list so that if noone buys it, the reduced price stays on bidItems
						currentItem.setCurrentPrice(0.9*currentItem.getCurrentPrice());
						counter++;
						//Reduced price, check if you should send it or not
						// if all bidders have quit
						if (this.interestedBidders.size() == 0) {
							//System.out.println("Nobody to bid! Moving on to next item!");
							offer_is_on = 0;
						}
						else {
							//5 attempts - stop bidding
							if (counter>=6){
								offer_is_on=0;
								this.stopBidding();
								//System.out.println("Value can't drop more! Moving on to next item!");
							}
							else {
								//Send new reduced price
								this.newHighBid();
								//System.out.println("10 down! ");
							}
						}							
					}
					//if there were interested bidders on the item
					else {
						//if a successful bid was placed, stop the bidding process and sell the item
						this.stopBidding();
						offer_is_on = 0;
						//TODO: bidItems.remove(currentItem); override equals for itemId at Item class  
						//System.out.println("item sold! ");
					}
				}
			}
			//Auction for that item done
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//TODO: All items sold if while!! 
		System.out.println("Auction finished!");		
		//wait for system to stabilize before sending new message
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
		//TODO: terminate auction, close all channels, if user quited then his channel closed
		this.auctionComplete();
		timer.cancel();
	}

}
			
	

