package server;

public class Item {
	
	//Items will be stored on a database, so that concurrency control by DBMS
	
	private int itemId;
	private int initialPrice;
	private String description;
	private int currentPrice;
	private String highestBidderName; //name
	
	//Constructor
	public Item(int itemId, int initialPrice, String description) {
		this.setItemId(itemId);
		this.setInitialPrice(initialPrice);
		this.setDescription(description);
		this.setCurrentPrice(initialPrice);
		this.setHighestBidderName("no_holder");				//bidder id = 0 means no one yet 
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

	public String getHighestBidderName() {
		return highestBidderName;
	}

	public void setHighestBidderName(String highestBidderName) {
		this.highestBidderName = highestBidderName;
	}
}
