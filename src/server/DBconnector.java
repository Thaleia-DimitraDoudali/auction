package server;

import server.Item;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBconnector {
	
	private String url = "jdbc:mysql://localhost/auction";
	private static final String user = "root";
	private static final String password = "root";
	private Connection connection = null;
	private Statement statement = null;

	private int serverId;
	
	public DBconnector(int serverId) {
		this.serverId = serverId;
		this.url += serverId;
		connect();
		dropTable();
		createTable();
	}
	
	public void connect() {
		try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("[" + serverId + "] Connected to Database");
 
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public void createTable() {
		System.out.println("[" + serverId + "] Creating table in given database...");
	    try {
	    	statement = connection.createStatement();	      
	    	String sql = "CREATE TABLE items(itemId INT NOT NULL PRIMARY KEY,"
	    		+ " initialPrice decimal(10,3),"
	    		+ "currentPrice decimal(10,3),"
	    		+ "description VARCHAR(200), "
	    		+ "highestBidderName VARCHAR(200));" ;
			statement.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void dropTable() {
		System.out.println("[" + serverId + "] Droping table in given database...");
	    try {
	    	statement = connection.createStatement();	      
	    	String sql = "DROP TABLE items;";
	    	statement.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}	    	
	}
	
	public void addItemToDB(Item item) {
		try {
			if (connection == null)
				connect();
			statement = connection.createStatement();
			String sql = String.format("INSERT INTO items set "
					+ "itemId = '%d', initialPrice = '%.2f', currentPrice = '%.2f', highestBidderName = '%s', "
					+ "description = '%s'", item.getItemId(), item.getInitialPrice(), item.getCurrentPrice(),
					item.getHighestBidderName(), item.getDescription());
			//System.out.println("[" + serverId + "]" + sql);
			statement.execute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}
	
	public void setItemCurrPrice(int id, double price) {
		try {
			if (connection == null)
				connect();
			statement = connection.createStatement();
			String sql = String.format("UPDATE items set "
					+ "currentPrice = '%.2f' WHERE itemId = '%d' LIMIT 1;",
					price, id);
			System.out.println("[" + serverId + "]" + sql);
			statement.execute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}
	
	public void setItemHighestBidder(int id, String name) {
		try {
			if (connection == null)
				connect();
			statement = connection.createStatement();
			String sql = String.format("UPDATE items set "
					+ "highestBidderName = '%s' WHERE itemId = '%d' LIMIT 1;",
					name, id);
			System.out.println("[" + serverId + "]" + sql);
			statement.execute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}
	
	public Item getItem(int id) {
		Item item = null;
		try {
			statement = connection.createStatement();
			String sql = "SELECT * FROM items WHERE itemId = " + id;
			ResultSet rs = statement.executeQuery(sql);
			if (rs.next()) {
				item = new Item(rs.getInt("itemId"), rs.getDouble("initialPrice"), rs.getString("description"));
				item.setCurrentPrice(rs.getDouble("currentPrice"));
				item.setHighestBidderName(rs.getString("highestBidderName"));
			}
		} catch (SQLException e) {
			//Auto-generated catch block
			e.printStackTrace();
		}
		return item;
	}
}
