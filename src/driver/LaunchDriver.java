package driver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import client.Bidder;
import server.Item;
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

	@SuppressWarnings("deprecation")
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
			//hostname = InetAddress.getByName("localhost");
			hostname = InetAddress.getLocalHost();
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
		
		ArrayList<Thread> thr = new ArrayList<Thread>();
		int bid_stopped[] = new int[bidders.size()];
		
		int auction_is_on = 0;
		while (auction_is_on < bidders.size()){
			for (int i = 0; i < bidders.size(); i++) {
				Bidder b = bidders.get(i);
				BidderXML bXML = biddersXML.get(i);
				
				int id = b.getHandler().receiveMessage();
				switch (id) {
					//new_item
					case 4:
						bid_stopped[i] = 0;
						if (bid_stopped[i] == 0) {
						//all bidders will be interested for all items
						String output = String.format("\nNew Item!\n Description: %s %n Initial price at $%.2f %n \n",
								b.getItem().getDescription(), b.getItem().getInitialPrice());
						bw.get(i).write(output);
						b.getHandler().sendInterested(b.getItem());
						}
						break;
					//start_bidding
					case 5:
						if (bid_stopped[i] == 0) {
						bw.get(i).write("You can now bid for the item!\n");						
						Thread t = new Thread(new BidThread(bXML, b, bw.get(i)));
						thr.add(t);
						t.start();
						}
						break;
					//new_high_bid
					case 6:
						if (bid_stopped[i] == 0) {
						//Current client is the higher bidder
						if (b.getItem().getHighestBidderName().equals(b.getBidderName()))
							bw.get(i).write("Your bid has been accepted for the item! Keep bidding!\n>> ");
						else {
							//No one iterested = no_holder so price is reduced by server
							if (b.getItem().getHighestBidderName().equals("no_holder")) {
								String output = String.format("The item has now a new reduced value: " + "$%.2f %n \n", 
										b.getItem().getCurrentPrice());
								bw.get(i).write(output);
								bw.get(i).write("Start bidding now!\n>> ");
							}
							//Another client is the higher bidder
							else {
								String output = String.format("\nThe current highest bid is " + "$%.2f" + " and belongs to " 
										+ "%s!", b.getItem().getCurrentPrice(), b.getItem().getHighestBidderName());
								bw.get(i).write(output);
								bw.get(i).write("Keep bidding!\n>> ");
							}
						}
						}
						break;
					//stop_bidding
					case 7:
						if (bid_stopped[i] == 0) {
						thr.get(i).stop();
						bid_stopped[i] = 1;
						bw.get(i).write("\nYou can no longer bid for this item!\n");
						bw.get(i).write("Please wait for the results of the auction...\n");
						//When bidding stops check who bought the item
						//Current client is the higher bidder, thus he bought the item
						if ((b.getItem().getHighestBidderName()).equals(b.getBidderName())) {
							bw.get(i).write("Congratulations! The item is now yours!\n");
							b.addToItemsBought(b.getItem());
							//new Item, because the old one remains on itemsBought list
							b.setItem(new Item(0,0,"none_yet"));
						}
						else {
							//No one interested on the item, moving on to the next one
							if ((b.getItem().getHighestBidderName()).equals("no_holder")) {
								bw.get(i).write("Nobody bid for this item. Proceeding to the next item...\n");
							}
							//Another client bought it
							else if (!((b.getItem().getHighestBidderName()).equals("_unknown"))) {
								String output = String.format("The item was granted to " + "%s" + " who offered " + "$%.2f %n", b.getItem().getHighestBidderName(), b.getItem().getCurrentPrice());
								bw.get(i).write(output);
							}
						}
						}
						break;
					//auction_complete
					case 8:
						auction_is_on ++;
						bw.get(i).write("\nThe auction is completed! \n");
						b.printItemsBought(bw.get(i));
						bw.get(i).write("Thank you for participating!\n");
						break;
					default:
						break;
				}
			}
		}
		for (int i = 0; i < bw.size(); i++)
			bw.get(i).close();
	}
}
