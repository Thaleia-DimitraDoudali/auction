package server;

public class SyncServer {

	private DBconnector db1, db2;
	private int wait1 = -1, wait2 = 0;
	private Auctioneer auct1, auct2;	
	
	public SyncServer(DBconnector db1, DBconnector db2) {
		this.db1 = db1;
		this.db2 = db2;
	}
	
	//Custom barrier function
	public boolean wait(int serverId) {
		
		int reg1 = auct1.getRegTable().size();
		int reg2 = auct2.getRegTable().size();
		int index1 = auct1.getIndex();
		int index2 = auct2.getIndex();
		
		if (serverId == 1)
			wait1 = 1;
		else if (serverId == 2)
			wait2 = 1;
		
		//If the other server has no registered users, and both servers are on the same item, then proceed
		if (serverId ==1 && reg2 == 0 && index1 == index2)
			return false;
		else if (serverId == 2 && reg1 == 0 && index1 == index2)
			return false;
		if (wait1 == wait2) {
			return false;
		}
		else
			return true;
	}
	
	public void reset() {
		wait1 = -1;
		wait2 = 0;
	}
		
	//On new_high_bid sync the 2 databases to have one consistent item
	public void syncBids(int itemId) {
		Item item1 = db1.getItem(itemId);
		Item item2 = db2.getItem(itemId);
		
		item1.print();
		item2.print();

		if ((item1.getCurrentPrice() < item2.getCurrentPrice()) || 
				(item1.getHighestBidderName().equals("no_holder") &&
						!item2.getHighestBidderName().equals("no_holder"))) { //item2 wins, change item1
			db1.setItemCurrPrice(itemId, item2.getCurrentPrice());
			db1.setItemHighestBidder(itemId, item2.getHighestBidderName());
			return ;
		} else if ((item2.getCurrentPrice() < item1.getCurrentPrice()) || 
				(item2.getHighestBidderName().equals("no_holder") &&
						!item1.getHighestBidderName().equals("no_holder"))) { //item1 wins, change item2
			db2.setItemCurrPrice(itemId, item1.getCurrentPrice());
			db2.setItemHighestBidder(itemId, item1.getHighestBidderName());
			return ;
		} 
	}
	
	public void syncInitPrice(int serverId, int itemId, double price) {
		if (serverId == 1) {
			db2.setItemInitPrice(itemId, price);
		} else if (serverId == 2) {
			db1.setItemInitPrice(itemId, price);
		}
		return;
	}
	
	public void syncItemSold(int serverId, int itemId) {
		if (serverId == 1) {
			db2.setItemSold(itemId);
		} else if (serverId == 2) {
			db1.setItemSold(itemId);
		}
		return;
	}
	
	//Checks the other database in order to compare the item in both databases
	public Item agreeWinner(int itemId) {		
		
		Item item1 = db1.getItem(itemId);
		Item item2 = db2.getItem(itemId);

		if (item1.getCurrentPrice() < item2.getCurrentPrice()) { //item2 wins
			return item2;
		} else if (item2.getCurrentPrice() < item1.getCurrentPrice()) { //item1 wins
			return item1;
		} else if (item1.getHighestBidderName().equals(item2.getHighestBidderName())) { //no_holder
			return null;
		}
		//TODO: what if both bid the same amount
		return null;
	}

	//Check bidders for duplicate in the other server's DB
	public boolean checkDuplicate(int serverId, String name) {
		if (serverId == 1) {
			return db2.hasBidder(name);
		} else if (serverId == 2) {
			return db1.hasBidder(name);
		}
		return false;
	}
	
	public void sendToAllInterested (String message) {
		for (RegTableEntry entry1 : auct1.getInterestedBidders())
			auct1.getHandler().sendMessage(message, entry1.getSocketChannel());
		for (RegTableEntry entry2 : auct2.getInterestedBidders())
			auct1.getHandler().sendMessage(message, entry2.getSocketChannel());
	}
	
	//Getters - Setters
	public DBconnector getDb2() {
		return db2;
	}

	public void setDb2(DBconnector db2) {
		this.db2 = db2;
	}

	public DBconnector getDb1() {
		return db1;
	}

	public void setDb1(DBconnector db1) {
		this.db1 = db1;
	}

	public Auctioneer getAuct2() {
		return auct2;
	}

	public void setAuct2(Auctioneer auct2) {
		this.auct2 = auct2;
	}

	public Auctioneer getAuct1() {
		return auct1;
	}

	public void setAuct1(Auctioneer auct1) {
		this.auct1 = auct1;
	}
}
