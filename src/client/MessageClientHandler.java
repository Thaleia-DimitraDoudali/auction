package client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;

import server.Item;

/* Message format = id + ' ' + message's name + ' ' + rest;
	connect = 0
	i_am_interested = 1
	my_bid = 2
	quit = 3
	bid_item = 4
	start_bidding = 5
	new_high_bid = 6 
	stop_bidding = 7
	auction_complete = 8
	duplicate_name = 9
	empty message = 10
	wrong itemId = 11 
*/

public class MessageClientHandler {
	private SocketChannel channel;
	private Bidder bidder;
	
	//Constructor
	public MessageClientHandler(SocketChannel channel, Bidder bidder) {
		this.setSocketChannel(channel);
		this.setBidder(bidder);
	}
	
	//Getters - setters
	public SocketChannel getSocketChannel() {
		return channel;
	}

	public void setSocketChannel(SocketChannel channel) {
		this.channel = channel;
	}
	
	public Bidder getBidder() {
		return bidder;
	}
	
	public void setBidder(Bidder bidder) {
		this.bidder = bidder;
	}
	
	//Send an already composed message
	public void sendString(String message) {
		byte [] bmessage = new String(message).getBytes();
		ByteBuffer buffer = ByteBuffer.wrap(bmessage);
		try {
			channel.write(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//------Messages that a bidder can send------
	
	//Compose and send a connect message to auctioneer
	public void sendConnect() {
		String message = "0 connect" + ' ' + bidder.getBidderName();
		sendString(message);
	}
	
	//Compose and send an i_am_interested message to auctioneer
	public void sendInterested(Item item) {
		String message = "1 i_am_interested" + ' ' + + item.getItemId() + ' ' + bidder.getBidderName();
		sendString(message);
	}
	
	//Compose and send a my_bid message to auctioneer
	public void sendBid(Item item, double amount) {
		String message = "2 my_bid" + ' ' + amount + ' ' + item.getItemId() + ' ' + bidder.getBidderName();
		sendString(message);
	}

	//Compose and send a quit message to auctioneer
	public void sendQuit() {
		String message = "3 quit" + ' ' + bidder.getBidderName();
		sendString(message);
	}
	
	//-------Messages that a bidder can receive------
	
	/*According to the type of message, the handler will change the fields of bidder's current item. 
	  Returns the integer message id, so that bidder can continue the processing of the message. */
	public int receiveMessage() {
			
		ByteBuffer buffer = ByteBuffer.allocate(256);
		try {
			channel.read(buffer);
		} catch (IOException e) {
			return 10;
		}
		String message = new String(buffer.array()).trim();
		if (message.equals(null)) return 10;
		if (message.equals("")) return 10;
		
		char messageId = message.charAt(0);
		String[] args;
		int mtype;
		
		//System.out.format("Received messageId: %s %n",message);
		
		switch (messageId) {
		
			//bid_item
			case '4':
				args = message.split("\\s+");
				(bidder.getItem()).setItemId(Integer.parseInt(args[2]));
				(bidder.getItem()).setCurrentPrice(Double.parseDouble(args[3]));
				(bidder.getItem()).setInitialPrice(Double.parseDouble(args[3]));
				String description = message.replace("4 new_item " + args[2] + ' ' + args[3] + ' ', "");
				(bidder.getItem()).setDescription(description);
				mtype = 4;
				break;
			//start_bidding
			case '5':
				args = message.split("\\s+");
				if (bidder.getItem().getItemId() != Integer.parseInt(args[2])) {
					(bidder.getItem()).setHighestBidderName("_unknown");
					mtype = 11;
				}
				else
					mtype = 5;
				break;
			//new_high_bid
			case '6':
				args = message.split("\\s+");
				if (bidder.getItem().getItemId() != Integer.parseInt(args[4])) {
					(bidder.getItem()).setHighestBidderName("_unknown");
					mtype = 11;
				}
				else {
					(bidder.getItem()).setCurrentPrice(Double.parseDouble(args[2]));
					(bidder.getItem()).setHighestBidderName(args[3]);
					mtype = 6;
				}
				break;
			//stop_bidding
			case '7':
				args = message.split("\\s+");
				if (bidder.getItem().getItemId() != Integer.parseInt(args[4])) {
					(bidder.getItem()).setHighestBidderName("_unknown");
					mtype = 11;
				}
				else {
					(bidder.getItem()).setCurrentPrice(Double.parseDouble(args[2]));
					(bidder.getItem()).setHighestBidderName(args[3]);
					mtype = 7;
				}
				break;
			//auction_complete
			case '8':
				mtype = 8;
				break;
			//duplicate_name
			case '9':
				mtype = 9;
				break;
			//empty message
			default:
				mtype = 10;
				break;
		}		
		return mtype;
	}
}