package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
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

		System.out.print("Enter your name: \n>>");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String s = null;
		try {
			s = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Bidder bidder = new Bidder(s,bidderPort,hostname);
		(new Thread(bidder)).start();

	}

}

//we decided to call Socket() in bidder and include port,hostname in bidder's constructor arguments

