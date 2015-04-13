package server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LaunchServer {

	public static void main(String[] args) {

		//TODO Read configuration file L, items
		//TODO As you read items, add them to a List<Item> to pass it as an argument on Auctioneer constructor
		//TODO If 2 auctioneers store items to a mysql database

//		String connectionURL = "jdbc:mysql://localhost:3306/mydatabase";
	//	Connection connection = null;
		//Statement statement = null;
		//Class.forName("com.mysql.jdbc.Driver").newInstance();
		//connection = DriverManager.getConnection(connectionURL, dbuser, dbpass);						
		
		
		//List<Item> 
		//for item in List
		//statement = connection.createStatement();
		//opou VALUES meta item.getDescription();
		//String sql = "INSERT INTO items (username, password) VALUES( \"" + username  + "\" ,\"" + password + "\") ;";
		//int result = statement.executeUpdate(sql);
		
		//TODO Just one auctioneer for now - later on add second one and implement sync server.
		
		
	    BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("/home/christos/items.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	    List<Item> bidItems = new ArrayList<Item>();
	    int L = 10;
	    	//read auction time
	        String line;
			try {
				line = br.readLine();
				args = line.split("\\s+");
				L = Integer.parseInt(args[0]);
	        
				//read number of items
				line = br.readLine();
				args = line.split("\\s+");
				int N = Integer.parseInt(args[0]);
	        
				//read items
				for (int i=1;i<=N;i++) {
					line = br.readLine();
					args = line.split("\\s+");
					double price = Double.parseDouble(args[0]);
					String description = line.replace(args[0], "");
					description = description.trim();
					Item item = new Item(i,price,description);
					bidItems.add(item);
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		
        (new Thread(new Auctioneer(L/1000,bidItems))).start();    
	}

}
