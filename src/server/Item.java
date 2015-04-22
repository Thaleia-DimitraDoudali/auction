package server;

import java.util.ArrayList;
import java.util.Iterator;

public class Item{
		
	private int itemId;
	private double initialPrice;
	private String description;
	private double currentPrice;
	private String highestBidderName; 
	
	//Constructor
	public Item(int itemId, double initialPrice, String description) {
		this.setItemId(itemId);
		this.setInitialPrice(initialPrice);
		this.setDescription(description);
		this.setCurrentPrice(initialPrice);
		this.setHighestBidderName("no_holder");				 
	}
	
	public void print() {
		System.out.println("Item no. " + itemId + " initialPrice = " + initialPrice + " currentPrice = "
				+ currentPrice + " description = " + description + " highestBidderName = "
						+ highestBidderName);
	}
	
	//Getters - setters
	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public double getInitialPrice() {
		return initialPrice;
	}

	public void setInitialPrice(double initialPrice) {
		this.initialPrice = initialPrice;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getCurrentPrice() {
		return currentPrice;
	}

	public void setCurrentPrice(double currentPrice) {
		this.currentPrice = currentPrice;
	}

	public String getHighestBidderName() {
		return highestBidderName;
	}

	public void setHighestBidderName(String highestBidderName) {
		this.highestBidderName = highestBidderName;
	}

	//Iterator construction 
	
	ArrayList<Item> bidItems;

	
	public Iterator<Item> iterator() {
		return new ItemIterator();
	}
	
	class ItemIterator implements Iterator<Item> {
		int currentIndex = 0;

		@Override
		public boolean hasNext() {
			if (currentIndex >= bidItems.size()) {
				return false;
			} else {
				return true;
			}
		}

		@Override
		public Item next() {
			return bidItems.get(currentIndex++);
		}

		@Override
		public void remove() {
			bidItems.remove(--currentIndex);
		}

	}

}
