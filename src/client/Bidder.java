package client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.net.*;
import java.nio.channels.*;

import server.Item;
 
public class Bidder implements Runnable {
	
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
	
	
	public void run() {
		
		InetSocketAddress hostAddress = new InetSocketAddress(hostname, port);
		try {
			channel = SocketChannel.open(hostAddress);
			channel.configureBlocking(false);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		MessageClientHandler handler = new MessageClientHandler(channel, this);
		
		handler.sendConnect();
		//We have a problem here: we need to be able to check if there is no message at all
			
		System.out.println("Bidder connected to the auctioneer");	
		
		int auction_is_on = 1;
		int bidding_is_on = 0;
		int id, id2=0;
		double amount = 0;
		
		while (auction_is_on == 1){
			
			id = handler.receiveMessage();
			if (id != 10) System.out.format("Received Id: %d %n",id);
			
			switch (id) {
			case 9:
					System.out.println("Error: Duplicate name. Please, try again using another name.");
					return;
			case 8:
					System.out.println("The auction is completed. Thank you for participating!");
					auction_is_on = 0;
					break;
			case 4:
					System.out.println("New Item!");
					//There has to be a change to the local item values
					System.out.format(" Description: %s %n Initial price at $%.2f %n", item.getDescription(), item.getInitialPrice());
					System.out.println("Are you interested in it? If yes, type Y, otherwise type N:");
					
					try {
						BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
						String s = null;
						s = in.readLine();
						//System.out.format("%s", s);				
						if (s.charAt(0) == 'Y') {
						//Send i_am_interested message
							handler.sendInterested(item);
					
							System.out.println("sent I am Interested!");
							System.out.println("about to receive start bidding!");
							//receive the start_bidding message
							while (id2!=5) {
								id2 = handler.receiveMessage();
							}
							System.out.format("Received Id2: %d %n",id2);
							System.out.println("received start bidding");
					
							if (id2 == 5) { 
								System.out.println("You can now bid for the item! Type the word 'bid' and the amount you are willing to offer:");
								bidding_is_on = 1;
							}
					
											
							while (bidding_is_on == 1){
					    
								try {
									if (in.ready()) {
										s = in.readLine();
										if (s.length()>0) {
											System.out.format("Just read: %s %n",s);
											amount = Double.parseDouble(s.substring(4,s.length()));
											System.out.format("Sent amount: $%.2f %n",amount);
											System.out.println("sending amount");
											if (amount <= item.getCurrentPrice()) {
												System.out.println("Your bid doesn't exceed the current value of the item and thus was not taken into consideration!");
												System.out.println("Keep biding!");
											}
											handler.sendBid(item, amount);
										}
									}
								} 
								catch (IOException e) {
									e.printStackTrace();
								}
						
								id2 = handler.receiveMessage();
								
								switch (id2) {
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
										System.out.println("Your bid has been accepted for the item! Keep bidding!");
									}
									else {
										if ((item.getHighestBidderName()).equals("no_holder")){
											System.out.format("The item has now a new reduced value: " + "$%.2f %n", item.getCurrentPrice());
											System.out.println("Start bidding now!");
										}
										else {
											System.out.format("The current highest bid is " + "$%.2f" + " and belongs to " + "%s!", item.getCurrentPrice(), item.getHighestBidderName() );
											System.out.println("Keep bidding!");
										}
									}
									break;
								default:
									break;
								}
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

//TODO: use an item variable that will be used by MessageClientHandler. It can be passed as an argument or I can make set and get functions
	//		This is not needed if handler only decodes messages and calls the bidder to processes the decoded messages. We should talk about this.
	//TODO: make functions that will be called by the message handler to (perhaps process data and) print in terminal.
