package server;

import server.Item;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBconnector {
	
	private static final String url = "jdbc:mysql://localhost/auction";
	private static final String user = "root";
	private static final String password = "root";
	Connection connection = null;
	Statement statement = null;

	
	public DBconnector() {
		connect();
	}
	
	public void connect() {
		try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to Database");
 
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public void addItemToDB(double initialPrice, String description) {
		try {
			if (connection == null)
				connect();
			statement = connection.createStatement();
			String sql = String.format("INSERT INTO items set "
					+ "initialPrice = '%.2f', description = '%s'", initialPrice, description);
			System.out.println(sql);
			statement.execute(sql);
		} catch (SQLException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public Item getItem() {
		Item item = null;
		try {
			statement = connection.createStatement();
			String sql = "SELECT * FROM items";
			ResultSet rs = statement.executeQuery(sql);
			if (rs.next())
				item = new Item(rs.getInt("itemId"), rs.getDouble("initialPrice"), rs.getString("description"));
		} catch (SQLException e) {
			//Auto-generated catch block
			e.printStackTrace();
		}
		return item;
	}
	

	
	
	public static void main(String[] args) {
		
		DBconnector db = new DBconnector();
		db.addItemToDB(10.0, "Alarm Clock");
		//It will print one item, just to see that it works
		db.getItem().print();
	}

}
