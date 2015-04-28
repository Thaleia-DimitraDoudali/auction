package driver;

import java.util.ArrayList;

public class BidderXML {
	private String name;
	private double freq;
	private ArrayList<String> bids = new ArrayList<String>();
	
	public BidderXML(String name, double freq, ArrayList<String> bids) {
		this.name = name;
		this.freq = freq;
		this.bids = bids;
	}
	
	public void addBid(String bid) {
		bids.add(bid);
	}
	
	public void print() {
		System.out.print("Name = " + name + " Frequency = " + freq + " Bids: ");
		for (int i = 0; i < bids.size(); i++) {
			System.out.print(bids.get(i) + " ");
		}
		System.out.print("\n");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getFreq() {
		return freq;
	}

	public void setFreq(double freq) {
		this.freq = freq;
	}

	public ArrayList<String> getBids() {
		return bids;
	}

	public void setBids(ArrayList<String> bids) {
		this.bids = bids;
	}
}
