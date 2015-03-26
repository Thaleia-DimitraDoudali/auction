package server;

public class Launch {

	public static void main(String[] args) {

		//TODO Read configuration file L, items
		//TODO As you read items, add them to a List<Item> to pass it as an argument on Auctioneer constructor
		//TODO If 2 auctioneers store items to a mysql database

		//TODO Just one auctioneer for now - later on add second one and implement sync server.
        (new Thread(new Auctioneer(null))).start();
        
        //Opens TCP socket for bidders to connect
        
	}

}
