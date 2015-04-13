package server;
import java.nio.channels.SocketChannel;

public class RegTableEntry {
	private SocketChannel channel;
	private client.Bidder bidder;
	
	//Constructor
	public RegTableEntry(SocketChannel channel, client.Bidder bidder) {
		this.setSocketChannel(channel);
		this.setBidder(bidder);
	}

	//Getters - setters
	public SocketChannel getSocketChannel() {
		return channel;
	}

	public void setSocketChannel(SocketChannel channel) {
		this.channel= channel;
	}

	public client.Bidder getBidder() {
		return bidder;
	}

	public void setBidder(client.Bidder bidder) {
		this.bidder = bidder;
	}
	
	//override to permit use of entry.remove() function
	@Override
	public boolean equals(Object obj) {
		RegTableEntry entry = (RegTableEntry) obj;
		if (((this.bidder).getBidderName()).equals((entry.getBidder()).getBidderName()))
			return true;
		else return false;
	}
}
