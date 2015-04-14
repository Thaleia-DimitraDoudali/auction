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
	
	//Constructor
	public Bidder(String bidderName, int port, InetAddress hostname){
		this.setBidderName(bidderName);
		this.setPort(port);
		this.setHostName(hostname);
	}

	Item item = new Item(0,0,"none_yet");
	int flag = 0;

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
	
	//prints on terminal the items this bidders has bought (the elements of itemsBought)
	public void printItemsBought() {
		
		System.out.println("The items you have bought are:");
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
	
	
	//executes quit sequence
	public void quitFunction() {
		//waits until either the current item is sold or a higher bid is placed
		if (this.bidderName.equals(this.item.getHighestBidderName())) {
			System.out.println("You have placed the highest bid on this item.\nPlease wait until either a higher bid is placed or you get the item...");
			int id = 10;
			while (id == 10)
				id = this.handler.receiveMessage();
			if (id == 6) {
				System.out.println("A higher bid was placed!");
			}
			if (id == 7) {
				System.out.println("The item is yours!");
			}
		}
		//leave auction
		System.out.print("You have left the auction room! ");
		handler.sendQuit();
		printItemsBought();
		System.out.println("Thank you for participating!");
		return;
	}
	
	//decodes messages entered in terminal
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

	
	//executes bidding sequence for one item
	public boolean biddingFunction(BufferedReader in) {
	
		String s;
		int bidding_is_on = 1;
		while (bidding_is_on == 1){
	    
			try {
				if (in.ready()) {
					s = in.readLine();
					if (s.length()>0) {
						if (decodeTerminal(s))
							return true;
					}
				}
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
			
			int id = handler.receiveMessage();
			
			switch (id) {
			case 7:
				//Bidding for this item stops (stop_bidding received)
				System.out.println("You can no longer bid for this item!");
				System.out.println("Please wait for the results of the auction...");
				bidding_is_on = 0;
				break;
			case 6:
				//i have received a new_high_bid message
				//the item must have changed
				if ((item.getHighestBidderName()).equals(this.bidderName)) {
					System.out.print("Your bid has been accepted for the item! Keep bidding!\n>> ");
				}
				else {
					if ((item.getHighestBidderName()).equals("no_holder")){
						System.out.format("The item has now a new reduced value: " + "$%.2f %n", item.getCurrentPrice());
						System.out.print("Start bidding now!\n>> ");
					}
					else {
						System.out.format("\nThe current highest bid is " + "$%.2f" + " and belongs to " + "%s!", item.getCurrentPrice(), item.getHighestBidderName() );
						System.out.print("Keep bidding!\n>> ");
					}
				}
				break;
			default:
				break;
			}
		}
		return false;
	}
	
	
	
	public void run() {
		
		InetSocketAddress hostAddress = new InetSocketAddress(hostname, port);
		try {
			channel = SocketChannel.open(hostAddress);
			channel.configureBlocking(false);
		} catch (IOException e2) {
			System.out.println("Cannot reach server! Please try again later...");
			return;
		}
		this.handler = new MessageClientHandler(channel, this);
		
		handler.sendConnect();
			
		System.out.println("You have entered the auction room!");
		
		int auction_is_on = 1;
		int id, id2=0;
		double amount = 0;
		
		while (auction_is_on == 1){
			if (channel.isOpen())
				id = handler.receiveMessage();
			else
				id = 8;
			
			switch (id) {
			case 9:
					System.out.println("\nError: Your bidder name is already taken. \nThaleiaPlease, try again using another name.");
					return;
			case 8:
					System.out.print("\nThe auction is completed! ");
					printItemsBought();
					System.out.println("Thank you for participating!");
					auction_is_on = 0;
					break;
			case 4:
					System.out.println("\nNew Item!");
					//There has to be a change to the local item values
					System.out.format(" Description: %s %n Initial price at $%.2f %n", item.getDescription(), item.getInitialPrice());
					System.out.print("Are you interested in it? If yes, type Y, otherwise type N:\n>> ");
					
					try {
						BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
						String s = null;
						s = in.readLine();
						id2 = handler.receiveMessage();
						if (s.equals("quit")) {
							quitFunction();
							return;
						}
						if ((id2 != 10) && (s.charAt(0) == 'Y')) {
							System.out.println("The auction has already began.\nYou will be informed about the next item soon...");
							break;
						}
						if (s.charAt(0) == 'Y') {							
						//Send i_am_interested message
							handler.sendInterested(item);
							//System.out.println("sent I am Interested!");
							//receive the start_bidding message
							id2 = 10;
							while (id2!=5) {
								id2 = handler.receiveMessage();
							}
							//System.out.format("Received Id2: %d %n",id2);
					
							if (id2 == 5) { 
								System.out.print("You can now bid for the item! Type the word 'bid' and the amount you are willing to offer:\n>> ");
								if (biddingFunction(in)) return;
							}

							//When bidding stops i must check if I have bought the item
							if ((item.getHighestBidderName()).equals(this.bidderName)) {
								System.out.println("Congratulations! The item is now yours!");
								itemsBought.add(item);
								item = new Item(0,0,"none_yet");
							}
							else {
								if ((item.getHighestBidderName()).equals("no_holder")) {
								System.out.println("Nobody bid for this item. Proceeding to the next item...");
								}
								else {
									System.out.format("The item was granted to " + "%s" + " who offered " + "$%.2f %n", item.getHighestBidderName(), item.getCurrentPrice());
								}
							}
						}
						
						System.out.println("You will be informed about the next item soon...");
							
					} catch (IOException e1) {
						e1.printStackTrace();
					}
			default:
				break;
				
			}//switch
		}//while
	}//run

}//class

