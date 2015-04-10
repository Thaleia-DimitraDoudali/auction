package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import server.Item;

//Message format = message's name + ' ' + rest;

public class MessageClientHandler {
	private Socket socketId;
	private Bidder bidder;
	private PrintWriter writer;
	private BufferedReader reader;
	
	//Constructor
	public MessageClientHandler(Socket socketId, Bidder bidder) {
		this.setSocketId(socketId);
		this.setBidder(bidder);
		try {
			(this.socketId).setSoTimeout(100);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		//set output buffer
		try {
			OutputStream os = (this.socketId).getOutputStream();
			writer = new PrintWriter(os, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//set input buffer
		try {
			reader = new BufferedReader(new InputStreamReader(socketId.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		writer.println(message);
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
		
		String message = null;
		
		try {
			message = reader.readLine();
		} catch (SocketException e) {
			return 10;
		} catch (IOException e) {
			return 10;
		}
		
		if (message == null) {
			return 10;
		}
		
		char messageId = message.charAt(0);
		String[] args;
		int mtype;
		
		switch (messageId) {
		case '4':
			args = message.split("\\s+");
			(bidder.getItem()).setItemId(Integer.parseInt(args[2]));
			(bidder.getItem()).setCurrentPrice(Integer.parseInt(args[3]));
			(bidder.getItem()).setInitialPrice(Integer.parseInt(args[3]));
			String description = message.replace("4 new_item " + args[2] + ' ' + args[3] + ' ', "");
			(bidder.getItem()).setDescription(description);
			mtype = 4;
			break;
		case '5':
			//args = message.split("\\s+");
			//should use args[2] to confirm itemId is the item.getItemId()
			mtype = 5;
			break;
		case '6':
			args = message.split("\\s+");
			(bidder.getItem()).setCurrentPrice(Integer.parseInt(args[2]));
			(bidder.getItem()).setHighestBidderName(args[3]);
			//should also do something with args[4] (itemId)
			mtype = 6;
			break;
		case '7':
			args = message.split("\\s+");
			(bidder.getItem()).setCurrentPrice(Integer.parseInt(args[2]));
			(bidder.getItem()).setHighestBidderName(args[3]);
			//should also do something with args[4] (itemId)
			mtype = 7;
			break;
		case '8':
			mtype = 8;
			try {
				reader.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			writer.close();
			break;
		case '9':
			mtype = 9;
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			writer.close();
			break;
		default:
			mtype = 10;
			break;
		}
		
		return mtype;

	}
}
