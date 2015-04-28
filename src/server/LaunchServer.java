package server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class LaunchServer {
	
	public LaunchServer(String port, String file) {
		// Read bidderPort from args, it will be different for each auctioneer
		int bidderPort = Integer.parseInt(port);
		DBconnector db1 = null, db2 = null;
		BufferedReader br = null;
		String[] inp;
		try {
			br = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		int L = 10;
		int N = 0;
		// read auction time
		String line;
		try {
			line = br.readLine();
			inp = line.split("\\s+");
			L = Integer.parseInt(inp[0]);

			// read number of items
			line = br.readLine();
			inp = line.split("\\s+");
			N = Integer.parseInt(inp[0]);

			// read items and store to the two databases
			db1 = new DBconnector(1);
			db2 = new DBconnector(2);
			for (int i = 1; i <= N; i++) {
				line = br.readLine();
				inp = line.split("\\s+");
				double price = Double.parseDouble(inp[0]);
				String description = line.replace(inp[0], "");
				description = description.trim();
				Item item = new Item(i, price, description);
				// Store items to databases
				db1.addItemToDB(item);
				db2.addItemToDB(item);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		SyncServer sync = new SyncServer(db1, db2);
		
		//Auctioneer 1
		Auctioneer auct1 = new Auctioneer(1, L / 1000, N, bidderPort, db1, sync);
		sync.setAuct1(auct1);
		Thread t1 = (new Thread(auct1));
		t1.start();
		//Auctioneer 2
		Auctioneer auct2 = new Auctioneer(2, L / 1000, N, bidderPort + 1, db2, sync);
		sync.setAuct2(auct2);
		Thread t2 = (new Thread(auct2));
		t2.start();
	}

	public static void main(String[] args) {

		new LaunchServer(args[0], args[1]);
	}

}
