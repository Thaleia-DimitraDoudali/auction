package server;

public class SyncServer {

	private DBconnector db1, db2;
	int proceed1 = -1, proceed2 = 0;
	
	public SyncServer(DBconnector db1, DBconnector db2) {
		this.db1 = db1;
		this.db2 = db2;
	}
	
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
	
	//Checks the other database in order to compare the item in both databases
	public Item agreeLazyWinner(int itemId) {		
		
		Item item1 = db1.getItem(itemId);
		Item item2 = db2.getItem(itemId);

		if (item1.getCurrentPrice() < item2.getCurrentPrice()) { //item2 wins
			return item2;
		} else if (item2.getCurrentPrice() < item1.getCurrentPrice()) { //item1 wins
			return item1;
		} else if (item1.getHighestBidderName().equals(item2.getHighestBidderName())) { //no_holder
			return null;
		}
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
