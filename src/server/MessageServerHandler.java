package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

//Message format = message's name + ' ' + rest;

public class MessageServerHandler {
	private Socket socketId;
	private Auctioneer auctioneer;
	
	//Constructor
	public MessageServerHandler(Auctioneer auctioneer, Socket socketId){
		this.setSocketId(socketId);
		this.setAuctioneer(auctioneer);
	}

	//Getters - setters
	public Socket getSocketId() {
		return socketId;
	}
	public void setSocketId(Socket socketId) {
		this.socketId = socketId;
	}
	public Auctioneer getAuctioneer() {
		return auctioneer;
	}

	public void setAuctioneer(Auctioneer auctioneer) {
		this.auctioneer = auctioneer;
	}
	
	//Receive message
	public void receiveMessage(){
		try {
			BufferedReader br = 
					new BufferedReader(new InputStreamReader(socketId.getInputStream()));
			String readMessage = br.readLine();
			System.out.println("Bidder said: " + readMessage);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//According to what type of message auctioneer should act accordingly
		
		//if connect auctioneer.addToRegTable(RegTableEntry);
		
		//if interested auctioneer.addToCurrentBiders(RegTableEntry);
		
		//if my_bid auctioneer.receiveBid(amount, itemId);
		
		//if quit close appropriate sockets

	}

}
