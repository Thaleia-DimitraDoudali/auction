package driver;

import java.net.InetAddress;
import java.net.UnknownHostException;

import client.Bidder;
import server.LaunchServer;

public class LaunchDriver {

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
			Thread t = new Thread(bidder);
			t.start();
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
			Thread t = new Thread(bidder);
			t.start();
		}

	}

}
