package server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import client.Bidder;

//Message format = message's name + ' ' + rest;

public class MessageServerHandler {

	private Auctioneer auctioneer;
	
	//Constructor
	public MessageServerHandler(Auctioneer auctioneer){
		this.setAuctioneer(auctioneer);
	}
	
	public Auctioneer getAuctioneer() {
		return auctioneer;
	}

	public void setAuctioneer(Auctioneer auctioneer) {
		this.auctioneer = auctioneer;
	}
	
	//Receive and decode message
	public int receiveMessage(SocketChannel client){
		
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
			client.read(buffer);
		} catch (IOException e) {
			return 10;
		}
		String message = new String(buffer.array()).trim();
		if (message.equals(null)) return 10;
		if (message.equals("")) return 10;
		
		char messageId = message.charAt(0);
		String[] args;
		Bidder bidder;
		int mtype;
		
		System.out.format("Received messageId: %s %n",message);
		
		switch (messageId) {
		case '0':
			args = message.split("\\s+");
			bidder = new Bidder(args[2],0,null);
			RegTableEntry newEntry = new RegTableEntry(client, bidder); 
			auctioneer.addToRegTable(newEntry);
			mtype = 0;
			break;
		case '1':
			args = message.split("\\s+");
			bidder = new Bidder(args[3],0,null);
			RegTableEntry newInterest = new RegTableEntry(client, bidder); 
			auctioneer.addToInterestedBidders(newInterest);
			mtype = 1;
			break;
		case '2':
			args = message.split("\\s+");
			bidder = new Bidder(args[4],0,null);
			RegTableEntry tempEntry = new RegTableEntry(client, bidder);
			auctioneer.receiveBid(Double.parseDouble(args[2]), Integer.parseInt(args[3]), tempEntry);
			mtype = 2;
			break;
		case '3':
			args = message.split("\\s+");
			bidder = new Bidder(args[2],0,null);
			RegTableEntry entry = new RegTableEntry(client, bidder); 
			auctioneer.removeFromInterestedBidders(entry);
			auctioneer.removeFromRegTable(entry);
			mtype = 3;
			break;
		default:
			mtype = 10;
			break;
		}

		return mtype;
		
	}
	
	public void sendMessage(String message, SocketChannel client) {
		byte [] bmessage = new String(message).getBytes();
		ByteBuffer buffer = ByteBuffer.wrap(bmessage);
		try {
			client.write(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
