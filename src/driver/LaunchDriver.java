package driver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import client.Bidder;
import server.LaunchServer;

public class LaunchDriver {

	public static void stabilize() {
		//for system stabilization
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		
		//Parse configuration file
		ParseXML parser = new ParseXML(args[0]);
		System.out.println("-----------------Bidders for Auctioneer 1--------------");
		for (int i = 0; i < parser.getBidders1().size(); i++)
			parser.getBidders1().get(i).print();
		System.out.println("-----------------Bidders for Auctioneer 2--------------");
		for (int i = 0; i < parser.getBidders2().size(); i++)
			parser.getBidders2().get(i).print();
		
		//Launch Server
		new LaunchServer(args[1], args[2]);
		
		//Launch Clients - all clients will be interested in all items, bidding the same for all items
		InetAddress hostname = null;
		try {
			hostname = InetAddress.getByName("localhost");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		int port = Integer.parseInt(args[1]);
		
		ArrayList<Bidder> bidders = new ArrayList<Bidder>();
		ArrayList<BidderXML> biddersXML = new ArrayList<BidderXML>();

		//Clients hooked on auctioneer 1
		for (int i = 0; i < parser.getBidders1().size(); i++) {
			//for system stabilization
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//New bidder
			Bidder bidder = new Bidder(parser.getBidders1().get(i).getName(), port, hostname);
			bidders.add(bidder);
			biddersXML.add(parser.getBidders1().get(i));
			bidder.setUpChannel();
			bidder.getHandler().sendConnect();
		}
		
		//Clients hooked on auctioneer 2
		for (int i = 0; i < parser.getBidders2().size(); i++) {
			//for system stabilization
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//New bidder
			Bidder bidder = new Bidder(parser.getBidders2().get(i).getName(), port+1, hostname);
			bidders.add(bidder);
			biddersXML.add(parser.getBidders2().get(i));
			bidder.setUpChannel();
			bidder.getHandler().sendConnect();
		}
		
		//for system stabilization
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		boolean auction_is_on = true;
		int j = 0;
		while (auction_is_on){
			j++;
			for (int i = 0; i < bidders.size(); i++) {
				Bidder b = bidders.get(i);
				BidderXML bXML = biddersXML.get(i);
				if (j == 1)
					bXML.print();
				
				int id = b.getHandler().receiveMessage();
				//new_item
				if (id == 4) {
					b.getHandler().sendInterested(b.getItem());
				}//start_bidding
				else if (id == 5) {
					(new Thread(new BidThread(bXML, b))).start();
				//auction_complete
				} else if (id ==8) {
					auction_is_on = false;
				}
			}
		}

	}

}
