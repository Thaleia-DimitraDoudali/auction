package driver;

import java.util.ArrayList;

public class ItemBids{
	
	private ArrayList<String> bids = new ArrayList<String>();
	private String id;
	private String freq;
	
	public ItemBids(ArrayList<String> bids, String id, String freq){
		
		this.bids=bids;
		this.id=id;
		this.freq=freq;
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
	
	public String getfreq() {
		return freq;
	}

	public void setfreq(String freq) {
		this.freq = freq;
	}
	
	public void addBid(String bid) {
		bids.add(bid);
}
}