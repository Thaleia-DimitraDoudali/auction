package server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LaunchServer {

	public static void main(String[] args) {

		//TODO: If 2 auctioneers store items to 2 mysql databases
		
		
		//Read bidderPort from args, it will be different for each auctioneer
		int bidderPort = Integer.parseInt(args[0]);
		DBconnector db1 = null, db2 = null;
	    BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(args[1]));
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
	        
				//read items and store to the two databases
				db1 = new DBconnector(1);
				db2 = new DBconnector(2);
				for (int i=1;i<=N;i++) {
					line = br.readLine();
					args = line.split("\\s+");
					double price = Double.parseDouble(args[0]);
					String description = line.replace(args[0], "");
					description = description.trim();
					Item item = new Item(i,price,description);
					bidItems.add(item);
					db1.addItemToDB(item);
					db2.addItemToDB(item);
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
        (new Thread(new Auctioneer(1, L/1000,bidItems, bidderPort, db1))).start(); 
        (new Thread(new Auctioneer(2, L/1000,bidItems, bidderPort+1, db2))).start(); 
        
	}

}
