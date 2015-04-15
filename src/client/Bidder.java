package client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.net.*;
import java.nio.channels.*;

import server.Item;
 
public class Bidder implements Runnable {
	
	private MessageClientHandler handler;
	private String bidderName;
	private SocketChannel channel = null;
	private int port;
	private InetAddress hostname;
	private List<server.Item> itemsBought = new ArrayList<server.Item>();
	private	Item item = new Item(0,0,"none_yet");

	//Constructor
	public Bidder(String bidderName, int port, InetAddress hostname){
		this.setBidderName(bidderName);
		this.setPort(port);
		this.setHostName(hostname);
	}

	//Getters - Setters
	public Item getItem(){
		return item;
	}

	public String getBidderName() {
		return bidderName;
	}

	public void setBidderName(String bidderName) {
		this.bidderName = bidderName;
	}
	
	public void setSocketChannel(SocketChannel channel) {
		this.channel = channel;
	}
	
	public SocketChannel getSocketChannel() {
		return this.channel;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public int getPort() {
		return this.port;
	}
	
	public void setHostName(InetAddress hostname) {
		this.hostname = hostname;
	}

	public InetAddress getHostName() {
		return this.hostname;
	}
	
	//Prints on terminal the items this bidder has bought (the elements of itemsBought)
	public void printItemsBought() {
		
		System.out.println("The items you bought are:");
		int i = 1;
		if (itemsBought.size()==0) {
			System.out.println(" None");
			return;
		}
		for (Item item: itemsBought) {
			double amount = item.getCurrentPrice();
			String description = item.getDescription();
			System.out.format(" %d) %s \n    Price: $%.2f\n", i, description, amount);
			i++;
		}
	}
	
	//Executes quit sequence
	public void quitFunction() {
		if (this.bidderName.equals(this.item.getHighestBidderName())) {
			//If client quits and is the highest bidder, although he won't see the item printed as an item bought, he will be charged for it
			System.out.println("You have placed the highest bid on this item!\nAlthough you are leaving, if you win, your bid will still be charged!");
			//That's why we say probable
			item.setDescription("(Probable) " + item.getDescription());
			itemsBought.add(item);
		}
		//leave auction
		System.out.print("\nYou have left the auction room! ");
		handler.sendQuit();
		printItemsBought();
		System.out.println("Thank you for participating!");
		return;
	}
	
	//Decodes messages entered in terminal - returns true if quit
	public boolean decodeTerminal(String s) {
		if (s.equals("list_high_bid")) {
			System.out.format(" Current highest bid is $%.2f by %s \n>> ", item.getCurrentPrice(), item.getHighestBidderName());
			return false;
		}
		if (s.equals("list_description")) {
			System.out.format(" Current item description: %n  %s\n>> ", item.getDescription());
			return false;
		}
		if (s.equals("quit")) {
			quitFunction();
			return true;
		}
		//bid amount
		String[] args = s.split("\\s+");
		if ((args[0].equals("bid")) && (args.length == 2)) {
			try {
				double amount = Double.parseDouble(args[1]);
				if (amount <= item.getCurrentPrice()) {
					System.out.println("Your bid doesn't exceed the current value of the item and thus was not taken into consideration!");
					System.out.print("Keep biding!\n>> ");
				}
				else
					handler.sendBid(item, amount);
				return false;
			}
			catch (NumberFormatException e) {			}
		}
		System.out.print("Please use the command format. \n Valid commands are \"list_high_bid\", \"list_description\", \"bid <amount>\", \"quit\"\n>> ");
		return false;
	}

	
	public String nonBlockingRead(BufferedReader in) {
		String s = "";
		try {
			if (in.ready())
				s = in.readLine();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return s;
	}
	
	//executes bidding sequence for one item
	public boolean biddingFunction(BufferedReader in) {
	
		String s;
		int bidding_is_on = 1;
		while (bidding_is_on == 1){
			
			s = nonBlockingRead(in);
			if (s.length() > 0)
				if (decodeTerminal(s))
					return true; 
			
			int id = handler.receiveMessage();
			
			switch (id) {
			//L timeout
			//stop_bidding
			case 7:
				System.out.println("\nYou can no longer bid for this item!");
				System.out.println("Please wait for the results of the auction...");
				bidding_is_on = 0;
				break;
			//new_high_bid
			case 6:
				//Current client is the higher bidder
				if ((item.getHighestBidderName()).equals(this.bidderName)) {
					System.out.print("Your bid has been accepted for the item! Keep bidding!\n>> ");
				}
				else {
					//No one iterested = no_holder so price is reduced by server
					if ((item.getHighestBidderName()).equals("no_holder")){
						System.out.format("The item has now a new reduced value: " + "$%.2f %n", item.getCurrentPrice());
						System.out.print("Start bidding now!\n>> ");
					}
					//Another client is the higher bidder
					else {
						System.out.format("\nThe current highest bid is " + "$%.2f" + " and belongs to " + "%s!", item.getCurrentPrice(), item.getHighestBidderName() );
						System.out.print("Keep bidding!\n>> ");
					}
				}
				break;
			//wrong itemId
			case 11:
				System.out.println("Communication error with server...  You will be informed about the next item soon...");
				bidding_is_on = 0;
				break;
			default:
				break;
			}
		}
		return false;
	}
	
	//What a bidder does
	public void run() {
		
		//set up channel
		InetSocketAddress hostAddress = new InetSocketAddress(hostname, port);
		try {
			channel = SocketChannel.open(hostAddress);
			channel.configureBlocking(false);
		} catch (IOException e2) {
			System.out.println("Cannot reach server! Please try again later...");
			return;
		}
		this.handler = new MessageClientHandler(channel, this);
		
		//send connect
		handler.sendConnect();
			
		System.out.println("You have entered the auction room!");
		
		int auction_is_on = 1;
		int id, id2 = 0;
		
		while (auction_is_on == 1){
			
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String s = nonBlockingRead(in);
			if (s.equals("quit")) {
				//If owner is unknown then we're referring to an older item - error detector
				item.setHighestBidderName("_unknown");
				quitFunction();
				return;
			}
			//Process messages that a bidder receives from server
			id = handler.receiveMessage();			
			switch (id) {
			//duplicate name
			case 9:
					System.out.println("\nError: Your bidder name is already taken. \nPlease, try again using another name.");
					return;
			//auction_complete
			case 8:
					System.out.print("\nThe auction is completed! ");
					printItemsBought();
					System.out.println("Thank you for participating!");
					auction_is_on = 0;
					break;
			//bid_item
			case 4:
				int nextItem = 1;
				while (nextItem == 1)
				{
					nextItem = 0;
					System.out.println("\nNew Item!");
					//There has to be a change to the local item values
					System.out.format(" Description: %s %n Initial price at $%.2f %n", item.getDescription(), item.getInitialPrice());
					System.out.print("Are you interested in it? If yes, type Y, otherwise type N:\n>> ");
					
					//We'll get out of the loop with break or return
					while (true) {
						//Read from terminal
						s = nonBlockingRead(in);
						//Check if you received a message from server
						id2 = handler.receiveMessage();
						if (s.equals("quit")) {
							quitFunction();
							return;
						}
						/*If bid_item then server moved on to the next item so client shouldn't get in the auction
						  It will get in the next if and break*/
						if (id2 == 4)
							nextItem = 1;
						if (id2 == 8){
							System.out.print("\nThe auction is completed! ");
							printItemsBought();
							System.out.println("Thank you for participating!");
							auction_is_on = 0;
							return;
						}
						if (id2 != 10) {
							System.out.println("\nThe auction has already began.\nYou will be informed about the next item soon...");
							break;
						}
						//Client not interested
						if (s.equals("N")) {
							System.out.println("You will be informed about the next item soon...");
							break;
						}
						//Client interested
						if (s.equals("Y")) {
							//Send i_am_interested message
							handler.sendInterested(item);
							//System.out.println("sent I am Interested!");
							id2 = 10;
							//Loop until you get a start_bidding or wrond itemId message
							while ((id2!=5) && (id2!=11)) {
								id2 = handler.receiveMessage();
							}
							//System.out.format("Received Id2: %d %n",id2);
							//Wrong itemId
							if (id2 == 11) {
								System.out.println("Server communication error... You will be informed about the next item soon...");
								break;
							}
							
							//Bidding started!
							System.out.print("You can now bid for the item! Type the word 'bid' and the amount you are willing to offer:\n>> ");
							if (biddingFunction(in)) 
								return;
							
							//When bidding stops check who bought the item
							//Current client is the higher bidder, thus he bought the item
							if ((item.getHighestBidderName()).equals(this.bidderName)) {
								System.out.println("Congratulations! The item is now yours!");
								itemsBought.add(item);
								//new Item, because the old one remains on itemsBought list
								item = new Item(0,0,"none_yet");
							}
							else {
								//No one interested on the item, moving on to the next one
								if ((item.getHighestBidderName()).equals("no_holder")) {
									System.out.println("Nobody bid for this item. Proceeding to the next item...");
								}
								//Another client bought it
								else if (!((item.getHighestBidderName()).equals("_unknown"))) {
									System.out.format("The item was granted to " + "%s" + " who offered " + "$%.2f %n", item.getHighestBidderName(), item.getCurrentPrice());
								}
							}
							break;
						}
						//Wrong command
						if (s.length() > 0) {
							System.out.print("Please use current command format.\n Valid commands are \"Y\", \"N\" and \"quit\"\n>> ");
						}
					}
				}
			default:
				break;
				
			}//switch
		}//while
	}//run

}//class

