package server;

public class SyncServer {

	private DBconnector db1, db2;
	int proceed1 = -1, proceed2 = 0;
	
	public SyncServer(DBconnector db1, DBconnector db2) {
		this.db1 = db1;
		this.db2 = db2;
	}
	
	//Custom barrier function
	public boolean proceed(int serverId) {
		if (serverId == 1)
			proceed1 = 1;
		if (serverId == 2)
			proceed2 = 1;
		if (proceed1 == proceed2) {
			return false;
		}
		else
			return true;
	}
	
	public void reset() {
		proceed1 = -1;
		proceed2 = 0;
	}
	
	//On new_high_bid sync the 2 databases to have one consistent item
	public void syncBids(int itemId) {
		Item item1 = db1.getItem(itemId);
		Item item2 = db2.getItem(itemId);

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
	
}
