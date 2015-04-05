package server;

import java.net.Socket;

public class RegTableEntry {
	private Socket socketId;
	private client.Bidder bidder;
	
	//Constructor
	public RegTableEntry(Socket socketId, client.Bidder bidder) {
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

	public client.Bidder getBidder() {
		return bidder;
	}

	public void setBidder(client.Bidder bidder) {
		this.bidder = bidder;
	}
}
