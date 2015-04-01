package server;

public class Item {
	
	//Items will be stored on a database, so that concurrency control by DBMS
	
	private int itemId;
	private int initialPrice;
	private String description;
	private int currentPrice;
	private int highestBidderId; //name
	
	//Constructor
	public Item(int itemId, int initialPrice, String description) {
		this.setItemId(itemId);
		this.setInitialPrice(initialPrice);
		this.setDescription(description);
		this.setCurrentPrice(initialPrice);
		this.setHighestBidderId(0);				//bidder id = 0 means no one yet 
	}

	//Getters - setters
	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getInitialPrice() {
		return initialPrice;
	}

	public void setInitialPrice(int initialPrice) {
		this.initialPrice = initialPrice;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getCurrentPrice() {
		return currentPrice;
	}

	public void setCurrentPrice(int currentPrice) {
		this.currentPrice = currentPrice;
	}

	public int getHighestBidderId() {
		return highestBidderId;
	}

	public void setHighestBidderId(int highestBidderId) {
		this.highestBidderId = highestBidderId;
	}
}
