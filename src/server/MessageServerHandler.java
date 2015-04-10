package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import client.Bidder;

//Message format = message's name + ' ' + rest;

public class MessageServerHandler {
	//private Socket socketId;
	private Auctioneer auctioneer;
	
	//Constructor
	public MessageServerHandler(Auctioneer auctioneer){
		this.setAuctioneer(auctioneer);
	}

	//Getters - setters
	//public Socket getSocketId() {
	//	return socketId;
	//}
	
	//public void setSocketId(Socket socketId) {
	//	this.socketId = socketId;
	//}
	
	public Auctioneer getAuctioneer() {
		return auctioneer;
	}

	public void setAuctioneer(Auctioneer auctioneer) {
		this.auctioneer = auctioneer;
	}
	
	//Receive message
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
		client.read(buffer);
		String message = new String(buffer.array()).trim();
		
		char messageId = message.charAt(0);
		String[] args;
		Bidder bidder;
		int mtype;
		
		switch (messageId) {
		case '0':
			args = message.split("\\s+");
			bidder = new Bidder(args[2]);
			RegTableEntry newEntry = new RegTableEntry(socketId, bidder); 
			auctioneer.addToRegTable(newEntry);
			mtype = 0;
			break;
		case '1':
			args = message.split("\\s+");
			bidder = new Bidder(args[2]);
			RegTableEntry newInterest = new RegTableEntry(socketId, bidder); 
			auctioneer.addToInterestedBidders(newInterest);
			mtype = 1;
			break;
		case '2':
			args = message.split("\\s+");
			bidder = new Bidder(args[4]);
			RegTableEntry tempEntry = new RegTableEntry(socketId, bidder);
			auctioneer.receiveBid(Integer.parseInt(args[2]), Integer.parseInt(args[3]), tempEntry);
			mtype = 2;
			break;
		case '3':
			args = message.split("\\s+");
			bidder = new Bidder(args[2]);
			RegTableEntry entry = new RegTableEntry(socketId, bidder); 
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
