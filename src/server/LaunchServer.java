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
		//TODO: Read items.txt from args[1] and port one for each auctioneer because we're on localhost
		
	    BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(args[0]));
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
		
		//TODO: pass bidder port to constructor
        (new Thread(new Auctioneer(L/1000,bidItems))).start(); 
        //TODO: add another new auctioneer thread
        
	}

}
