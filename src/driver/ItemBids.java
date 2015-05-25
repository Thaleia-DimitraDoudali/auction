package driver;

import java.util.ArrayList;

public class ItemBids{
	
	private ArrayList<String> bids = new ArrayList<String>();
	private String id;
	
	public ItemBids(ArrayList<String> bids, String id){
		
		this.bids=bids;
		this.id=id;
	}
	
	public ArrayList<String> getBids() {
		return bids;
	}

	public void setBids(ArrayList<String> bids) {
		this.bids = bids;
	}
	
	public String getid() {
		return id;
	}

	public void setid(String id) {
		this.id = id;
	}
	
	public void addBid(String bid) {
		bids.add(bid);
}
}