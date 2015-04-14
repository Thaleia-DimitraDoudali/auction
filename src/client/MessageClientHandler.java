package client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;

import server.Item;

//Message format = message's name + ' ' + rest;

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
	
	
	//Messages that a bidder can send
	
	//Compose and send a connect message to auctioneer
	public void sendConnect() {
		//protocol: id + connect + etc
		String message = "0 connect" + ' ' + bidder.getBidderName();
		sendString(message);
	}
	
	
	//Compose and send an i_am_interested message to auctioneer
	public void sendInterested(Item item) {
		String message = "1 i_am_interested" + ' ' + + item.getItemId() + ' ' + bidder.getBidderName();
		sendString(message);
	}
	
	//Compose and send a my_bid message to auctioneer
	//!! confirmation that amount > price should be in Bidder, not in handler
	public void sendBid(Item item, double amount) {
		String message = "2 my_bid" + ' ' + amount + ' ' + item.getItemId() + ' ' + bidder.getBidderName();
		sendString(message);
	}
	

	public void sendQuit() {
		String message = "3 quit" + ' ' + bidder.getBidderName();
		sendString(message);
	}
	
	
	//Messages that a bidder can receive
	
	public int receiveMessage() {
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
		case '4':
			args = message.split("\\s+");
			(bidder.getItem()).setItemId(Integer.parseInt(args[2]));
			(bidder.getItem()).setCurrentPrice(Double.parseDouble(args[3]));
			(bidder.getItem()).setInitialPrice(Double.parseDouble(args[3]));
			String description = message.replace("4 new_item " + args[2] + ' ' + args[3] + ' ', "");
			(bidder.getItem()).setDescription(description);
			mtype = 4;
			break;
		case '5':
			args = message.split("\\s+");
			//should use args[2] to confirm itemId is the item.getItemId()
			mtype = 5;
			break;
		case '6':
			args = message.split("\\s+");
			(bidder.getItem()).setCurrentPrice(Double.parseDouble(args[2]));
			(bidder.getItem()).setHighestBidderName(args[3]);
			//should also do something with args[4] (itemId)
			mtype = 6;
			break;
		case '7':
			args = message.split("\\s+");
			(bidder.getItem()).setCurrentPrice(Double.parseDouble(args[2]));
			(bidder.getItem()).setHighestBidderName(args[3]);
			//should also do something with args[4] (itemId)
			mtype = 7;
			break;
		case '8':
			mtype = 8;
			break;
		case '9':
			mtype = 9;
			break;
		default:
			mtype = 10;
			break;
		}
		
		return mtype;

	}
}
