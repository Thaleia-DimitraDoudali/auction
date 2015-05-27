package server;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.*;

import server.Item;
import server.MessageServerHandler;

import java.util.Timer;
import java.util.TimerTask;

public class Auctioneer implements Runnable {

	private int serverId;
	private int bidderPort;
	private int L; // timeout time
	private int N; // number of items
	private Timer timer;
	private Selector selector;
	private int index; // index of current item in range 1..N
	private List<RegTableEntry> regTable = new ArrayList<RegTableEntry>();
	private List<RegTableEntry> interestedBidders = new ArrayList<RegTableEntry>();
	private MessageServerHandler handler = new MessageServerHandler(this);
	private DBconnector db;
	private SyncServer sync;
	private InetAddress hostname;
	private ServerSocketChannel ssc;

	// Timer that unblocks selector.select()
	class WakeUp extends TimerTask {
		public void run() {
			// System.out.println("Timer to the rescue!");
			selector.wakeup();
		}
	}

	// Constructor
	public Auctioneer(int id, int L, int N, int port, DBconnector db, SyncServer sync) {
		this.serverId = id;
		this.N = N;
		this.sync = sync;
		this.L = L;
		this.timer = new Timer();
		this.bidderPort = port;
		this.db = db;
		try {
			// Now it's the local host IPv4, it could also be a VM IPv4.
			this.hostname = InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
	}

	// Called at quit
	public void removeFromRegTable(RegTableEntry entry) {
		try {
			entry.getSocketChannel().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (regTable.remove(entry)) {
			System.out.println("[" + serverId + "] Entry removed");
			db.rmBidderFromDB(entry);
		}
	}

	// Called at connect
	public void addToRegTable(RegTableEntry entry) {
		String temp = (entry.getBidder()).getBidderName();
		int flag = 0;
		// Check for duplicate_name
		for (RegTableEntry entry2 : regTable) {
			if (temp.equals((entry2.getBidder()).getBidderName())) {
				String message = "9 duplicate_name Please abort";
				handler.sendMessage(message, entry.getSocketChannel());
				flag = 1;
				break;
			}
		}
		//Check also the bidders of the other server
		if (sync.checkDuplicate(serverId, entry.getBidder().getBidderName())) {
			String message = "9 duplicate_name Please abort";
			handler.sendMessage(message, entry.getSocketChannel());
			return;
		}
		//Check for no_holder as well
		if (temp.equals("no_holder")) {
			String message = "9 duplicate_name Please abort";
			handler.sendMessage(message, entry.getSocketChannel());
			return;
		}
		if (flag == 0) {
			regTable.add(entry);
			db.addBidderToDB(entry);
		}
	}

	// Called at quit
	public void removeFromInterestedBidders(RegTableEntry entry) {
		interestedBidders.remove(entry);
	}

	// Called at i_am_interested
	public void addToInterestedBidders(RegTableEntry entry) {
		interestedBidders.add(entry);
	}

	// Called at bid amount
	public int receiveBid(double amount, int itemId, RegTableEntry entry) {
		if ((itemId == index) && (interestedBidders.contains(entry))) {
			if (db.getItem(index).getCurrentPrice() < amount) {
				db.setItemCurrPrice(index, amount);
				db.setItemHighestBidder(index,
						(entry.getBidder()).getBidderName());
				//TODO: Before sending new_high_bid, we have to check with the other DB as well
				sync.syncBids(index);
				//send new_high_bid
				this.newHighBid();
				return 2;
			}
			// send new_high_bid -- moved it here as well
			//this.newHighBid();
		}
		return 10;
	}

	// -----Messages that a server sends------

	// new_item
	public void bidItem() {
		// send to all registered bidders
		String message = "4 new_item" + ' ' + db.getItem(index).getItemId()
				+ ' ' + db.getItem(index).getInitialPrice() + ' '
				+ db.getItem(index).getDescription();
		for (RegTableEntry entry : regTable) {
			handler.sendMessage(message, entry.getSocketChannel());
		}
	}

	// start_bidding
	public void startBidding() {
		String message = "5 start_bidding" + ' '
				+ db.getItem(index).getItemId();
		// send to all interested bidders
		for (RegTableEntry entry : interestedBidders)
			handler.sendMessage(message, entry.getSocketChannel());
	}

	// new_high_bid
	public void newHighBid() {
		String message = "6 new_high_bid" + ' '
				+ db.getItem(index).getCurrentPrice() + ' '
				+ db.getItem(index).getHighestBidderName() + ' '
				+ db.getItem(index).getItemId();
		// send to all interested bidders
		sync.sendToAllInterested(message);
	}
	
	// new reduced price
	public void newReducedPrice() {
		String message = "6 new_reduced_price" + ' '
				+ db.getItem(index).getCurrentPrice() + ' '
				+ db.getItem(index).getHighestBidderName() + ' '
				+ db.getItem(index).getItemId();
		System.out.println("[" + serverId + "] " + message);
		for (RegTableEntry entry : interestedBidders)
			handler.sendMessage(message, entry.getSocketChannel());
	}

	// stop_bidding
	public void stopBidding() {
		boolean wait_stop = true;
		sync.resetPassStop(serverId);
		while (wait_stop) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			wait_stop = sync.wait_stop(serverId);
			//System.err.println("[" + serverId + "] Waiting...");
		}
		sync.reset_stop(serverId);
		//System.out.format(" index="+"%s",index);
		String message = "7 stop_bidding" + ' '
				+ db.getItem(index).getCurrentPrice() + ' '
				+ db.getItem(index).getHighestBidderName() + ' '
				+ db.getItem(index).getItemId();
		// send to all interested bidders
		sync.sendToAllInterested1(message,serverId);
		//for (RegTableEntry entry : interestedBidders)
			//handler.sendMessage(message, entry.getSocketChannel());
	}

	// auction_complete
	public void auctionComplete() {
		String message = "8 auction_complete";
		// send to all registered bidders
		for (RegTableEntry entry : regTable)
			handler.sendMessage(message, entry.getSocketChannel());
		try {
			// ensure message sent to all
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// close all channels
		/*for (RegTableEntry entry : regTable)
			try {
				(entry.getSocketChannel()).close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		//Also close server socket channel, so that no new bidders can connect
		try {
			ssc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}

	// What an auctioneer thread does
	@SuppressWarnings("unused")
	public void run() {

		System.out.println("[" + serverId + "] Auctioneer up and running!");
		
		// set up connection
		ssc = null;
		InetSocketAddress isa;
		selector = null;

		// cat auct_name so that the bidder can find out the ip and port of the
		// auctioneer
		try {
			String content = "localhost" + " " + bidderPort + " ";
			// The auct_name file will be created at the current directory
			String workingDir = System.getProperty("user.dir");
			File file = new File(workingDir + "/auct_name" + serverId);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Channel preparation
		try {
			selector = Selector.open();
			ssc = ServerSocketChannel.open();
			isa = new InetSocketAddress(hostname, bidderPort);
			System.out.println("[" + serverId + "] Successful connection!");
			ssc.bind(isa);
			ssc.configureBlocking(false);
			int ops = ssc.validOps();
			ssc.register(selector, ops, null);
		} catch (IOException e) {
			System.out
					.println("["
							+ serverId
							+ "] Couldn't listen on bidders port! Server must be reset!");
			return;
		}

		sync.reset(serverId);
		sync.reset_stop(serverId);
		
		long tStart, tMid, tEnd = 0;
		tStart = System.currentTimeMillis();

		// Loop constantly waiting for at least one bidder to connect, so that
		// auction can start
		while (this.regTable.size() < 1) {

			tEnd = System.currentTimeMillis();// current time
			tMid = System.currentTimeMillis();// start time

			// Auction timeout at 3L
			if ((tEnd - tStart) / 1000 > 3 * L) {
				System.out.println("[" + serverId
						+ "] No bidders, auction was aborted");
				timer.cancel();
				return;
			}

			// Set selector's wakeup time, to unblock him
			timer.schedule(new WakeUp(), L * 1000);

			// What happens in L sec
			while ((tEnd - tMid) / 1000 < L) {
				try {
					// select == blocking
					selector.select();
					Set<SelectionKey> selectedKeys = selector.selectedKeys();
					Iterator<SelectionKey> iter = selectedKeys.iterator();
					// Loop through the channels that have been selected
					while (iter.hasNext()) {
						SelectionKey key = (SelectionKey) iter.next();
						// Physical connections
						if (key.isAcceptable()) {
							// Accept the new client connection
							SocketChannel client = ssc.accept();
							client.configureBlocking(false);
							// Add the new connection to the selector
							client.register(selector, SelectionKey.OP_READ);
						}
						// Read from channel
						else if (key.isReadable()) {
							// Read the data from client
							SocketChannel client = (SocketChannel) key
									.channel();
							// Handler's read non blocking
							handler.receiveMessage(client);
						}
						iter.remove();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				tEnd = System.currentTimeMillis();
			}
		}

		// Now some users have set up a channel, so auction can begin

		int interested = 0;
		int bidded = 0;
		int offer_is_on = 1;
		int counter = 0;

		
		// System.out.println("reached list!");
	
		// Iterate through the list of items until all items sold
		int items_left = N;
		boolean sold = false;
		//while (items_left > 0) {
			index = 0;
			//iterate through all the items on the database
			for (int i = 0; i < N; i++) {
				
				//synchronize
				boolean wait = true;
				sync.resetPass(serverId);
				//System.err.println("[" + serverId + "] Before Waiting...");
				while (wait) {
					wait = sync.wait(serverId);
					//System.err.println("[" + serverId + "] Waiting...");
				}
				//System.err.println("[" + serverId + "] After Waiting...");
				sync.reset(serverId);
				index++;
				Item item = db.getItem(index);
				if (item.getSold() == 1)
					sold = true;
				else
					sold = false;
				//If item is not sold then put it for auction, else ignore it and move on to the next one
				if (!sold) {
					counter = 0;
					interested = 0;
					interestedBidders.clear();
					bidded = 0;
					
					// Send new_item to registered bidders
					this.bidItem();

					System.out.format("\n[" + serverId + "] New item: %s \n",
							db.getItem(index).getDescription());

					tEnd = System.currentTimeMillis();
					tStart = System.currentTimeMillis();

					timer.schedule(new WakeUp(), L * 1000);

					// receive i am interested messages or new connections
					while ((tEnd - tStart) / 1000 < L) {
						try {
							selector.select();
							Set<SelectionKey> selectedKeys = selector
									.selectedKeys();
							Iterator<SelectionKey> iter = selectedKeys
									.iterator();
							while (iter.hasNext()) {
								SelectionKey key = (SelectionKey) iter.next();
								if (key.isAcceptable()) {
									// Accept the new client connection
									SocketChannel client = ssc.accept();
									client.configureBlocking(false);
									// Add the new connection to the selector
									client.register(selector,
											SelectionKey.OP_READ);
									// System.out.println("Successful accept2!");
								} else if (key.isReadable()) {
									// Read the data from client
									SocketChannel client = (SocketChannel) key
											.channel();
									// System.out.println("Successful is readable");
									int res = handler.receiveMessage(client);
									if (res == 1)
										interested++;
								}
								iter.remove();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
						tEnd = System.currentTimeMillis();
					}

					// System.out.println("after I am interested!");


					// If at least one bidders are interested
					if (this.interestedBidders.size() >= 1) {

						// begin the auction for the current item
						this.startBidding();
						// System.out.println("start bidding was sent!");
						offer_is_on = 1;

						while (offer_is_on == 1) {

							tEnd = System.currentTimeMillis();
							tStart = System.currentTimeMillis();
							timer.schedule(new WakeUp(), L * 1000);

							// receive bids or new connections
							while ((tEnd - tStart) / 1000 < L) {
								try {
									selector.select();
									Set<SelectionKey> selectedKeys = selector
											.selectedKeys();
									Iterator<SelectionKey> iter = selectedKeys
											.iterator();
									while (iter.hasNext()) {
										SelectionKey key = (SelectionKey) iter
												.next();
										if (key.isAcceptable()) {
											// Accept the new client connection
											SocketChannel client = ssc.accept();
											client.configureBlocking(false);
											// Add the new connection to the
											// selector
											client.register(selector,
													SelectionKey.OP_READ);
										} else if (key.isReadable()) {
											// Read the data from client
											SocketChannel client = (SocketChannel) key
													.channel();
											int res = handler
													.receiveMessage(client);
											// Received bid so restart L
											if (res == 2) {
												tStart = System
														.currentTimeMillis();
												timer.cancel();
												timer.purge();
												timer = new Timer();
												timer.schedule(new WakeUp(),
														L * 1000);
											}
										}
										iter.remove();
									}
								} catch (IOException e) {
									e.printStackTrace();
								}
								tEnd = System.currentTimeMillis();
							}

							// System.out.println("after bids were done!");

							// TODO: lazy sync check agreement in winner
						/*	Item it = sync.agreeWinner(index);
							if (it != null) {
								db.setItemCurrPrice(index, it.getCurrentPrice());
								db.setItemHighestBidder(index, it.getHighestBidderName());
							}
							*/
							// If no one wanted the item = no_holder then reduce price
							if ((db.getItem(index).getHighestBidderName()).equals("no_holder")) {
								counter++;
								// Reduced price, check if you should send it or not if all bidders have quit
								if (this.interestedBidders.size() == 0) {
									// System.out.println("Nobody to bid! Moving on to next item!");
									offer_is_on = 0;
								} else {
									// 5 attempts - stop bidding
									if (counter >= 6) {
										offer_is_on = 0;
										this.stopBidding();
										// At this point the item will get back to auction later on, so initialPrice
										// becomes currentPrice
										//db.setItemInitPrice(index, db.getItem(index).getCurrentPrice());
										//sync.syncInitPrice(serverId, index, db.getItem(index).getCurrentPrice());
										// System.out.println("Value can't drop more! Moving on to next item!");
									} else {
										// Send new reduced price
										double reducedPrice = 0.9 * db.getItem(index).getCurrentPrice();
										db.setItemCurrPrice(index, reducedPrice);
										this.newReducedPrice();
										// System.out.println("10 down! ");
									}
								}
							}
							// if there were interested bidders on the item
							else {
								// if a successful bid was placed, stop the
								// bidding process and sell the item
								this.stopBidding();
								offer_is_on = 0;
								db.setItemSold(index);
								sync.syncItemSold(serverId, index);
								items_left--;
								// System.out.println("item sold! ");
							}
						}
					}
					// Auction for that item done
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		//} // All items sold!
		System.out.println("[" + serverId + "] Auction finished!");
		// wait for system to stabilize before sending new message
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// TODO: terminate auction, close all channels, if user quited then his
		// channel closed
		this.auctionComplete();
		timer.cancel();
	}

	// Getters - setters
	public int getServerId() {
		return this.serverId;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getN() {
		return N;
	}

	public void setN(int n) {
		N = n;
	}

	public SyncServer getSync() {
		return sync;
	}

	public void setSync(SyncServer sync) {
		this.sync = sync;
	}

	public int getBidderPort() {
		return bidderPort;
	}

	public void setBidderPort(int bidderPort) {
		this.bidderPort = bidderPort;
	}

	public InetAddress getHostname() {
		return hostname;
	}

	public void setHostname(InetAddress hostname) {
		this.hostname = hostname;
	}

	public List<RegTableEntry> getInterestedBidders() {
		return interestedBidders;
	}

	public void setInterestedBidders(List<RegTableEntry> interestedBidders) {
		this.interestedBidders = interestedBidders;
	}

	public List<RegTableEntry> getRegTable() {
		return regTable;
	}

	public void setRegTable(List<RegTableEntry> regTable) {
		this.regTable = regTable;
	}

	public MessageServerHandler getHandler() {
		return handler;
	}

	public void setHandler(MessageServerHandler handler) {
		this.handler = handler;
	}

}
