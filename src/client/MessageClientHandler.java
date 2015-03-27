package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

//Message format = message's name + ' ' + rest;

public class MessageClientHandler {
	private Socket socketId;
	
	//Constructor
	public MessageClientHandler(Socket socketId){
		this.setSocketId(socketId);
	}

	//Getters - setters
	public Socket getSocketId() {
		return socketId;
	}

	public void setSocketId(Socket socketId) {
		this.socketId = socketId;
	}
	
	//Send a connect message to auctioneer
	public void sendConnect(Bidder bidder) {
		String message = "connect" + ' ' + bidder.getBidderName();
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
	
	//TODO:
	public void sendBid() {
		
	}
	
	//TODO:
	public void sendListHighBid() {
		
	}
	
	//TODO:
	public void sendListDescription() {
		
	}
	//TODO
	public void sendQuit() {
		
	}
	
	//TODO: Messages that a bidder can receive
	public void decodeMessage(String message) {
		//According to message call the appropriate Bidder function, which will
		//usually have to inform the bidder (command line out) about what happened!
		
		//bid_item
		//start_bidding
		//new_high_bid
		//stop_bidding
		//auction_complete
		//duplicate_name
	}
	
	
	
	
	
	
	
	
	
	
}
