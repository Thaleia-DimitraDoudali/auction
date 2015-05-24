package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

import client.Bidder;

//Class that launches one bidder

public class LaunchClient {

	public LaunchClient(String ip, String port, String name) {
		//Read hostname and bidderPort from cat auct_name, thus args[0], args[1] and bidderName from args[2]
		System.out.println(ip + " " + port + " " + name);
		InetAddress hostname = null;
		try {
			//hostname = InetAddress.getByName(ip);
			hostname = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		int bidderPort = Integer.parseInt(port);
		String bidderName = name;
		//if bidderName == no_holder, it's not permitted, so read the bidderName from the terminal 
		if (bidderName.equals("no_holder")) {
			//Read client's name
			System.out.print("Enter your name: \n>>");
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			bidderName = null;
			try {
				bidderName = in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//Launch bidder
		Bidder bidder = new Bidder(bidderName, bidderPort, hostname);
		(new Thread(bidder)).start();
	}
	
	public static void main(String[] args) {
		
		new LaunchClient(args[0], args[1], args[2]);
	}

}