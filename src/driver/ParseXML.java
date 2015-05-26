package driver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class ParseXML {
	
	private ArrayList<BidderXML> bidders1 = new ArrayList<BidderXML>();
	private ArrayList<BidderXML> bidders2 = new ArrayList<BidderXML>();


	public ParseXML(String path) {
		File xmlFile = new File(path);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		Document doc;

		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(xmlFile);

			NodeList nodList = doc.getElementsByTagName("auctioneer");
			// for auctioneers
			for (int k = 0; k < nodList.getLength(); k++) {
				Element e = (Element) nodList.item(k);
				NodeList nodeList = e.getElementsByTagName("bidder");
				// for bidders
				for (int i = 0; i < nodeList.getLength(); i++) {
					//ArrayList<String> bids = new ArrayList<String>();
					ArrayList<ItemBids> itembids = new ArrayList<ItemBids>();
					
					Element el = (Element) nodeList.item(i);
					
					String name = el.getElementsByTagName("name").item(0)
							.getTextContent();
					//String freq = el.getElementsByTagName("frequency").item(0)
						//	.getTextContent();
					
					BidderXML bidder = new BidderXML(name, itembids);
					
					NodeList nList = el.getElementsByTagName("item");
					// for items
					for (int j = 0; j < nList.getLength(); j++) {
						Element elem = (Element) nList.item(j);
						String id = elem.getElementsByTagName("id").item(0).getTextContent();
						String freq = elem.getElementsByTagName("frequency").item(0).getTextContent();
						ArrayList<String> bids = new ArrayList<String>();
						
						
						NodeList bList = elem.getElementsByTagName("bid");
						for (int l = 0; l < bList.getLength(); l++){
							Element em = (Element) bList.item(l);
							String bid = em.getTextContent();
							//itembids.get(j).addBid(bid);
							bids.add(bid);
						}
						
						ItemBids item = new ItemBids(bids, id, freq);
						itembids.add(item);
					}
					if (k == 0)
						bidders1.add(bidder);
					else if (k == 1)
						bidders2.add(bidder);
				}
			}

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException | IOException e) {
			e.printStackTrace();
		}

	}


	public ArrayList<BidderXML> getBidders1() {
		return bidders1;
	}


	public void setBidders1(ArrayList<BidderXML> bidders1) {
		this.bidders1 = bidders1;
	}


	public ArrayList<BidderXML> getBidders2() {
		return bidders2;
	}


	public void setBidders2(ArrayList<BidderXML> bidders2) {
		this.bidders2 = bidders2;
	}

}