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
		
		int index = 0;
		int id_curr = bdr.getItem().getItemId();
		
		
		for (int l=0; l < bidder.getItemBids().size(); l++)
			if (id_curr == Integer.parseInt(bidder.getItemBids().get(l).getid())) {
				index = l;
				break;
			}
		
		double freq_curr = Double.parseDouble(bidder.getItemBids().get(index).getfreq());
		
		// Send bid sleep for frequency sec
		for (int i = 0; i < bidder.getItemBids().get(index).getBids().size(); i++) {
			//sleep
			try {
				Thread.sleep((long) (10000*freq_curr));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			bdr.getHandler().sendBid(bdr.getItem(), Double.parseDouble(bidder.getItemBids().get(index).getBids().get(i)));
		}
	}

}
