package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
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
	public int receiveMessage(String message, Socket socketId){
		
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
		Bidder bidder;
		
		switch (messageId) {
		case '0':
			args = message.split("\\s+");
			bidder = new Bidder(args[2]);
			RegTableEntry newEntry = new RegTableEntry(socketId, bidder); 
			auctioneer.addToRegTable(newEntry);
			return 0;
			break;
		case '1':
			args = message.split("\\s+");
			bidder = new Bidder(args[2]);
			RegTableEntry newInterest = new RegTableEntry(socketId, bidder); 
			auctioneer.addToInterestedBidders(newInterest);
			return 1;
			break;
		case '2':
			args = message.split("\\s+");
			bidder = new Bidder(args[4]);
			RegTableEntry tempEntry = new RegTableEntry(socketId, bidder);
			auctioneer.receiveBid(Integer.parseInt(args[2]), Integer.parseInt(args[3]), tempEntry);
			return 2;
			break;
		case '3':
			myString.split("\\s+");
			//remove bidder from interested and regtable
			return 3;
			break;
		default:
			break;
		}
		
		
		
		try {
			BufferedReader br = 
					new BufferedReader(new InputStreamReader(socketId.getInputStream()));
			String readMessage = br.readLine();
			System.out.println("Bidder said: " + readMessage);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//According to what type of message auctioneer should act accordingly
	
		//tha stelnei kai mhnumata ston bider
		
		//if connect auctioneer.addToRegTable(RegTableEntry);
		
		//if interested auctioneer.addToCurrentBiders(RegTableEntry);
		
		//if my_bid auctioneer.receiveBid(amount, itemId);
			//check price if higher than current price
	
		
		//if quit close appropriate sockets

	}
	
	public void sendMessage(String message, RegTableEntry entry) {
		Socket socketId = entry.getSocketId();
		try {
			OutputStream os = socketId.getOutputStream();
			PrintWriter pw = new PrintWriter(os, true);
			pw.println(message);
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
