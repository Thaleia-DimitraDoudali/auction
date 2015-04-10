package client;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

import server.Item;

public class Bidder implements Runnable{
	
	//private int bidderId;
	private String bidderName;
	private Socket socketId;
	private List<server.Item> itemsBought = new ArrayList<server.Item>();
	
	//Constructor
	public Bidder(String bidderName, Socket socketId){
		//maybe id won't be useful
		//this.setBidderId(bidderId);
		this.setBidderName(bidderName);
		this.setSocket(socketId);
	}

	Item item = new Item(0,0,"none_yet");
	MessageClientHandler handler = new MessageClientHandler(socketId, this);
	int flag = 0;
	//Getters - setters
	//public int getBidderId() {
	//	return bidderId;
	//}

	//public void setBidderId(int bidderId) {
	//	this.bidderId = bidderId;
	//}
	public Item getItem(){
		return item;
	}

	public String getBidderName() {
		return bidderName;
	}

	public void setBidderName(String bidderName) {
		this.bidderName = bidderName;
	}
	
	public void setSocket(Socket socketId) {
		this.socketId = socketId;
	}

	
	public void run() {
		
		handler.sendConnect();
		//We have a problem here: we need to be able to check if there is no message at all
		if (handler.receiveMessage() == 9) {
			System.out.println("Error: Duplicate name. Please, try again using another name");
			return;
		}
			
		System.out.println("Bidder connected to the auctioneer");	
		
		int auction_is_on = 1;
		int bidding_is_on = 0;
		int id, id2;
		double amount;
		
		while (auction_is_on == 1){
			
			id = handler.receiveMessage();
			if (id == 8) {
				System.out.println("The auction is completed. Thank you for participating");
				auction_is_on = 0;
			}
			else {
				if (id == 4) {
					System.out.println("New Item!");
					//There has to be a change to the local item values
					System.out.format("Initial Price:" + "%lf" + "Description" + "%s", item.getInitialPrice(), item.getDescription());
					System.out.println("Are you interested in it? If yes, type Y else N");
					
					char c;
					try {
						c = (char) System.in.read();
					
					
					if (c == 'Y') {
						//Send i am interested message
						handler.sendInterested(item);
						
						//receive the start_bidding message
						id2 = handler.receiveMessage();
						if (id2 == 5) { 
							System.out.println("You can now bid for the item. Type the word 'bid' an the amount you are willing to offer ");
							bidding_is_on = 1;
						}
						
												
						while (bidding_is_on == 1){
						
							BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
						    String s;
						    
						    try {
								while ((s = in.readLine()) != null && s.length() != 0) {
									int length = s.length();
									amount = Double.parseDouble(s.substring(4,length - 1));
									handler.sendBid(item, amount);
								}
							} 
						    catch (IOException e) {
						    	e.printStackTrace();
						    }
							
						    id2 = handler.receiveMessage();
							
							if (id2 == 7) {
								System.out.println("You can no longer bid for this item.");
								System.out.println("Please wait for the results of the auction");
								bidding_is_on = 0;
							}
							else {
								//i have received a new high bid message
								//the item must have changed
								if ((item.getHighestBidderName()).equals(this.bidderName)) {
									System.out.println("Your bid has been accepted for the item! Keep bidding!");
									
								}
								else {
									if ((item.getHighestBidderName()).equals("no_holder")){
										System.out.format("The item has now a new reduced value:" + "%lf", item.getCurrentPrice());
										System.out.println("Start bidding now!");
									}
									else {
										System.out.format("The current highest bid is" + "%lf" + "and belongs to" + "%s", item.getCurrentPrice(), item.getHighestBidderName() );
										System.out.println("Keep bidding!");
									}
								}
								
							}
					
						}
						//When bidding stops i must check if I have bought the item
						if ((item.getHighestBidderName()).equals(this.bidderName)) {
							System.out.println("Congratulations! The item is now yours!");
							itemsBought.add(item);
						}
						else {
							System.out.format("The item was granted to" + "%s" + "who offered" + "%lf", item.getHighestBidderName(), item.getCurrentPrice());
						}
					}
					else {
						System.out.println("You will be informed about the next item soon");
					}
				
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			
			}//new item
		}//else for new item
	}//auction is on
	
}//run

}//class

//TODO: use an item variable that will be used by MessageClientHandler. It can be passed as an argument or I can make set and get functions
	//		This is not needed if handler only decodes messages and calls the bidder to processes the decoded messages. We should talk about this.
	//TODO: make functions that will be called by the message handler to (perhaps process data and) print in terminal.
