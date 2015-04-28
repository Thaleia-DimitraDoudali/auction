package driver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

	public static void main(String[] args) throws IOException {
		
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
		
		//Prepare each client's output file
		ArrayList<BufferedWriter> bw = new ArrayList<BufferedWriter>();
		for (int i = 0; i < bidders.size(); i++) {
			BidderXML bXML = biddersXML.get(i);
			try {
				// The output file will be created at the current directory
				String workingDir = System.getProperty("user.dir");
				File file = new File(workingDir + "/output_" + bXML.getName());
				if (!file.exists()) {
					file.createNewFile();
				}
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				bw.add(new BufferedWriter(fw));
		} catch (IOException e) {
			e.printStackTrace();
		}
		}
		
		int auction_is_on = 0;
		while (auction_is_on < bidders.size()){
			for (int i = 0; i < bidders.size(); i++) {
				Bidder b = bidders.get(i);
				BidderXML bXML = biddersXML.get(i);
				
				int id = b.getHandler().receiveMessage();
				//new_item
				if (id == 4) {
					String output = String.format("\nNew Item!\n Description: %s %n Initial price at $%.2f %n \n",
							b.getItem().getDescription(), b.getItem().getInitialPrice());
					bw.get(i).write(output);
					b.getHandler().sendInterested(b.getItem());
				}//start_bidding
				else if (id == 5) {
					bw.get(i).write("You can now bid for the item!\n");
					(new Thread(new BidThread(bXML, b))).start();
				//auction_complete
				} else if (id ==8) {
					auction_is_on ++;
					bw.get(i).write("\nThe auction is completed! \n");
					b.printItemsBought(bw.get(i));
					bw.get(i).write("Thank you for participating!\n");
				}
			}
		}
		for (int i = 0; i < bw.size(); i++)
			bw.get(i).close();
	}
}
