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
		dropTables();
		createTables();
	}
	
	public void connect() {
		try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("[" + serverId + "] Connected to Database");
 
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public void createTables() {
		System.out.println("[" + serverId + "] Creating tables in given database...");
	    try {
	    	statement = connection.createStatement();	      
	    	String sql = "CREATE TABLE items(itemId INT NOT NULL PRIMARY KEY,"
	    		+ " initialPrice decimal(10,3),"
	    		+ "currentPrice decimal(10,3),"
	    		+ "description VARCHAR(200), "
	    		+ "highestBidderName VARCHAR(200),"
	    		+ "sold INT(1));" ;
			statement.executeUpdate(sql);
	    	statement = connection.createStatement();
	    	sql = "CREATE TABLE bidders ("
	    			+ "name VARCHAR(200) NOT NULL PRIMARY KEY, "
	    			+ "port INT, "
	    			+ "hostname VARCHAR(200));";
			statement.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void dropTables() {
		System.out.println("[" + serverId + "] Droping tables in given database...");
	    try {
	    	statement = connection.createStatement();	      
	    	String sql = "DROP TABLE items;";
	    	statement.executeUpdate(sql);
	    	statement = connection.createStatement();	      
	    	sql = "DROP TABLE bidders;";
	    	statement.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}	    	
	}
	
	//---------------Bidders------------------
	
	public void addBidderToDB(RegTableEntry entry) {
		try {
			if (connection == null)
				connect();
			statement = connection.createStatement();
			String sql = String.format("INSERT INTO bidders set "
					+ "name = '%s', port = '%d', hostname = '%sf'", 
					entry.getBidder().getBidderName(), entry.getBidder().getPort(), 
					entry.getBidder().getHostName().toString());
			//System.out.println("[" + serverId + "]" + sql);
			statement.execute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	
	public void rmBidderFromDB(RegTableEntry entry) {
		try {
			if (connection == null)
				connect();
			statement = connection.createStatement();
			String sql = String.format("DELETE FROM bidders WHERE name = '%s';", entry.getBidder().getBidderName());
			//System.out.println("[" + serverId + "]" + sql);
			statement.execute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}				
	}
	
	public boolean hasBidder(String name) {
		try {
			statement = connection.createStatement();
			String sql = String.format("SELECT * FROM bidders WHERE name = '%s'", name);
			ResultSet rs = statement.executeQuery(sql);
			if (rs.next()) {
				//bidder found
				return true;
			}
		} catch (SQLException e) {
			//Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	//-----------------Items----------------------
	
	public void addItemToDB(Item item) {
		try {
			if (connection == null)
				connect();
			statement = connection.createStatement();
			String sql = String.format("INSERT INTO items set "
					+ "itemId = '%d', initialPrice = '%.2f', currentPrice = '%.2f', highestBidderName = '%s', "
					+ "description = \"%s\", sold = '%d'", item.getItemId(), item.getInitialPrice(), item.getCurrentPrice(),
					item.getHighestBidderName(), item.getDescription(), 0);
			//System.out.println("[" + serverId + "]" + sql);
			statement.execute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}
	
	public void setItemSold(int id) {
		try {
			if (connection == null)
				connect();
			statement = connection.createStatement();
			String sql = String.format("UPDATE items set "
					+ "sold = 1 WHERE itemId = '%d' LIMIT 1;",
					id);
			System.out.println("[" + serverId + "]" + sql);
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
	
	public void setItemInitPrice(int id, double price) {
		try {
			if (connection == null)
				connect();
			statement = connection.createStatement();
			String sql = String.format("UPDATE items set "
					+ "initialPrice = '%.2f' WHERE itemId = '%d' LIMIT 1;",
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
				item.setSold(rs.getInt("sold"));
			}
		} catch (SQLException e) {
			//Auto-generated catch block
			e.printStackTrace();
		}
		return item;
	}
}
