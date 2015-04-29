package driver;

import java.io.BufferedWriter;

import client.Bidder;

public class BidThread implements Runnable {

	BidderXML bidder;
	Bidder bdr;
	BufferedWriter bw;
	
	public BidThread(BidderXML bidder, Bidder b, BufferedWriter bw) {
		this.bidder = bidder;
		this.bdr = b;
		this.bw = bw;
	}
	
	@Override
	public void run() {
		// Send bid sleep for frequency sec
		for (int i = 0; i < bidder.getBids().size(); i++) {
			//sleep
			try {
				Thread.sleep((long) (1000*bidder.getFreq()));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			bdr.getHandler().sendBid(bdr.getItem(), Double.parseDouble(bidder.getBids().get(i)));
		}
	}

}
