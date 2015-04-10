package client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
//import java.io.BufferedReader;
//import java.io.InputStreamReader;

import client.Bidder;
//Class that launches one bidder

public class LaunchClient {

	public static void main(String[] args) {
		
		//TODO: Read bidder port from args
		
		int bidderPort = 2223;
		InetAddress hostname = null;
		try {
			//Now it's the local host IPv4, it could also be a VM IPv4.
			hostname = InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		
		//Connect to the auctioneer (which one? for later)
		Socket clientSocket = null;
		try {
			clientSocket = new Socket(hostname, bidderPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//TODO: Read bidder's name from args. This can be implemented only via terminal
				//The check for bidder's valid name is implemented in the auctioneer 
		
		Bidder bidder = new Bidder("thaleia",clientSocket);
		(new Thread(bidder)).start();

	}

}

//we decided to call Socket() in bidder and include port,hostname in bidder's constructor arguments

