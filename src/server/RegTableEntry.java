package server;

public class RegTableEntry {
	private int socketId;
	private client.Bidder bidder;
	
	//Constructor
	public RegTableEntry(int socketId, client.Bidder bidder) {
		this.setSocketId(socketId);
		this.setBidder(bidder);
	}

	//Getters - setters
	public int getSocketId() {
		return socketId;
	}

	public void setSocketId(int socketId) {
		this.socketId = socketId;
	}

	public client.Bidder getBidder() {
		return bidder;
	}

	public void setBidder(client.Bidder bidder) {
		this.bidder = bidder;
	}
}
