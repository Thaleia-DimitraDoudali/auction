package driver;

import java.util.ArrayList;

public class BidderXML {
	private String name;
	//private double freq;
	private ArrayList<ItemBids> itembids = new ArrayList<ItemBids>();
	
	public BidderXML(String name, ArrayList<ItemBids> itembids) {
		this.name = name;
		//this.freq = freq;
		this.itembids = itembids;
	}
	
	/*public void addItemBids(String itembid) {
		itembids.add(itembid);
	}*/
	
	public void print() {
		System.out.print("Name = " + name + " Bids: ");
		for (int i = 0; i < itembids.size(); i++){ 
			System.out.print(itembids.get(i).getid());
			System.out.print("\n");
			System.out.print(itembids.get(i).getfreq());
			System.out.print("\n");
			for (int j = 0; j < itembids.get(i).getBids().size(); j++)
			  System.out.print(itembids.get(i).getBids().get(j) + " ");
		System.out.print("\n");
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/*public double getFreq() {
		return freq;
	}

	public void setFreq(double freq) {
		this.freq = freq;
	}*/

	public ArrayList<ItemBids> getItemBids() {
		return itembids;
	}

	public void setBids(ArrayList<ItemBids> itembids) {
		this.itembids = itembids;
	}
}