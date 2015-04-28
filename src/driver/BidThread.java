package driver;

import client.Bidder;

public class BidThread implements Runnable {

	BidderXML bidder;
	Bidder bdr;
	
	public BidThread(BidderXML bidder, Bidder b) {
		this.bidder = bidder;
		this.bdr = b;
	}
	
	@Override
	public void run() {
		// Send bid sleep for frequency sec
		for (int i = 0; i < bidder.getBids().size(); i++) {
			System.out.println(bidder.getBids().get(i));
			bdr.getHandler().sendBid(bdr.getItem(), Double.parseDouble(bidder.getBids().get(i)));
			//sleep
			try {
				Thread.sleep((long) (1000*bidder.getFreq()));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
