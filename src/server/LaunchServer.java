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
			
        (new Thread(new Auctioneer(L/1000,bidItems, bidderPort))).start(); 
        //TODO: add another new auctioneer thread with (bidderPort+1)
        
	}

}
