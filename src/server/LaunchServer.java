package server;

import java.sql.Connection;
import java.sql.Statement;

public class LaunchServer {

	public static void main(String[] args) {

		//TODO Read configuration file L, items
		//TODO As you read items, add them to a List<Item> to pass it as an argument on Auctioneer constructor
		//TODO If 2 auctioneers store items to a mysql database

		String connectionURL = "jdbc:mysql://localhost:3306/mydatabase";
		Connection connection = null;
		Statement statement = null;
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		connection = DriverManager.getConnection(connectionURL, dbuser, dbpass);						
		
		
		//List<Item> 
		//for item in List
		statement = connection.createStatement();
		//opou VALUES meta item.getDescription();
		String sql = "INSERT INTO items (username, password) VALUES( \"" + username  + "\" ,\"" + password + "\") ;";
		int result = statement.executeUpdate(sql);
		
		//TODO Just one auctioneer for now - later on add second one and implement sync server.
        (new Thread(new Auctioneer(null))).start();    
	}

}
