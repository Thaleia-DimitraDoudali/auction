package client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

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
		
		//TODO: Read bidder's name from args
		//TODO: Check if inserted name is valid
		Bidder bidder = new Bidder(0, "thaleia");
		
		//Maybe do all the following on bidder's run function
		//run bidder as a thread, or not
		
		
		//Connect to the auctioneer (which one? for later)
		Socket clientSocket = null;
		try {
			clientSocket = new Socket(hostname, bidderPort);
			System.out.println("Bidder connects to Auctioneer");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		MessageClientHandler handler = new MessageClientHandler(clientSocket);
		
		//Send connect message to auctioneer through the socket
		handler.sendConnect(bidder);
		
		//Read from command line what bidder wants to do
		//MessageClientHandler.decodeMessage(string read);

	}

}
