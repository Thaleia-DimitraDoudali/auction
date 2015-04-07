package client;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import server.Item;

//Message format = message's name + ' ' + rest;

public class MessageClientHandler {
	private Socket socketId;
	private Bidder bidder;
	
	//Constructor
	public MessageClientHandler(Socket socketId, Bidder bidder){
		this.setSocketId(socketId);
		this.setBidder(bidder);
	}

	//Getters - setters
	public Socket getSocketId() {
		return socketId;
	}

	public void setSocketId(Socket socketId) {
		this.socketId = socketId;
	}
	
	public Bidder getBidder() {
		return bidder;
	}
	
	public void setBidder(Bidder bidder) {
		this.bidder = bidder;
	}
	
	//Send an already composed message
	public void sendString(String message) {
		try {
			OutputStream os = socketId.getOutputStream();
			PrintWriter pw = new PrintWriter(os, true);
			pw.println(message);
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	//Messages that a bidder can send
	
	//Compose and send a connect message to auctioneer
	public void sendConnect() {
		//protocol: id + connect + etc
		String message = "connect" + ' ' + bidder.getBidderName();
		sendString(message);
	}
	
	
	//Compose and send an i_am_interested message to auctioneer
	public void sendInterested(Item item) {
		String message = "1 i_am_interested" + ' ' + bidder.getBidderName();
		sendString(message);
	}
	
	//Compose and send a my_bid message to auctioneer
	//!! confirmation that amount > price should be in Client
	public void sendBid(Item item, int amount) {
		String message = "2 my_bid" + ' ' + amount + ' ' + item.getItemId() + ' ' + bidder.getBidderName();
		sendString(message);
	}
	
	//TODO:
	//???? Should be in bidder
	public void sendListHighBid() {
		
	}
	
	//TODO:
	//????? should be in bidder
	public void sendListDescription() {
		
	}
	
	//TODO
	public void sendQuit() {
		String message = "4 quit" + ' ' + bidder.getBidderName();
		sendString(message);
	}
	
	
	//TODO: Messages that a bidder can receive
	
	public void decodeMessage(String message) {
		//According to message call the appropriate Bidder function, which will
		//usually have to inform the bidder (command line out) about what happened!
		
		//connect = 0
		//i_am_interested = 1
		//my_bid = 2
		//quit = 3
		//bid_item = 4
		//start_bidding = 5
		//new_high_bid = 6
		//stop_bidding = 7
		//auction_complete = 8
		//duplicate_name = 9
		
		char messageId = message.charAt(0);
		String[] args;
		
		switch (messageId) {
		case '4':
			args = message.split("\\s+");
			(bidder.getItem()).setItemId(args[2]);
			(bidder.getItem()).setPrice(args[3]);
			//print something in terminal???
			//do something with description (which is the remaining strings in args)
			break;
		case '5':
			args = message.split("\\s+");
			//should use args[2] to confirm itemId is the item.getItemId()
			//initial price should be removed from message
			break;
		case '6':
			args = message.split("\\s+");
			(bidder.getItem()).setPrice(args[2]);
			(bidder.getItem()).setHighestBidder(arg[3]);
			//print something in terminal???
			//should also do something with args[4] (itemId)
			//PROBLEM -> how will bidder know if a 6 is received because there is a new highest bid or because of 10% down
			//		and not think the auction has started in the latter case
			//		should print something that tells which case it is
			break;
		case '7':
			args = message.split("\\s+");
			(bidder.getItem()).setPrice(args[2]);
			(bidder.getItem()).setHighestBidder(arg[3]);
			//print something in terminal???
			//should also do something with args[4] (itemId)
			//inform bidders to stop bidding and check if they are the winners (if so add item to their list)
			break;
		case '8':
			args = message.split("\\s+");
			//inform bidders to shut down
			break;
		case '9':
			args = message.split("\\s+");
			//inform bidder to shut down or abort???
			break;
		default:
			break;
		//bid_item
		//start_bidding
		//new_high_bid
		//stop_bidding
		//auction_complete
		//duplicate_name
		}
	}
}
