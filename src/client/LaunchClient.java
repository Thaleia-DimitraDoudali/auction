package client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import client.Bidder;
//Class that launches one bidder

public class LaunchClient {

	public static void main(String[] args) {
		
		//TODO: Read bidder port from args
		
		int flag = 0;
		int bidderPort = 2223;
		InetAddress hostname = null;
		try {
			//Now it's the local host IPv4, it could also be a VM IPv4.
			hostname = InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		
		//TODO: Read bidder's name from args. This can be implemented only via terminal
		//The check for bidder's valid name is implemented in the auctioneer 
		Bidder bidder = new Bidder("thaleia");
		
		
		//Connect to the auctioneer (which one? for later)
		Socket clientSocket = null;
		try {
			clientSocket = new Socket(hostname, bidderPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		MessageClientHandler handler = new MessageClientHandler(clientSocket, bidder);
		
		//Send connect message to auctioneer through the socket
		handler.sendConnect();
		
		//There has to be a check for a "duplicate name" message
		//I have to read the socket
		try {
			BufferedReader in =
			        new BufferedReader(
			            new InputStreamReader(clientSocket.getInputStream()));
			if (in.read() != -1) flag = 1;
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		if (flag == 0) {
			System.out.println("Bidder is connected to the Auctioneer");
			//Start the bidder thread
			(new Thread(new Bidder(null))).start();
		}
		else {
			System.out.println("Duplicate name Error. Bidder aborts. Please try with a different name");
			return;
		}
		
		
		//Read from command line what bidder wants to do
		//MessageClientHandler.decodeMessage(string read);

	}

}
