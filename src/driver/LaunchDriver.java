package driver;

import server.LaunchServer;

public class LaunchDriver {

	public static void main(String[] args) {
		
		//Parse configuration file
		ParseXML parser = new ParseXML();
		System.out.println("-----------------Bidders for Auctioneer 1--------------");
		for (int i = 0; i < parser.getBidders1().size(); i++)
			parser.getBidders1().get(i).print();
		System.out.println("-----------------Bidders for Auctioneer 2--------------");
		for (int i = 0; i < parser.getBidders2().size(); i++)
			parser.getBidders2().get(i).print();
		
		//Launch Server
		new LaunchServer(args[0], args[1]);
	}

}
