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


	public ParseXML() {
		File xmlFile = new File("conf.xml");
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
					ArrayList<String> bids = new ArrayList<String>();
					Element el = (Element) nodeList.item(i);
					String name = el.getElementsByTagName("name").item(0)
							.getTextContent();
					String freq = el.getElementsByTagName("frequency").item(0)
							.getTextContent();
					
					BidderXML bidder = new BidderXML(name, Double.parseDouble(freq), bids);
					
					NodeList nList = el.getElementsByTagName("bid");
					// for bids
					for (int j = 0; j < nList.getLength(); j++) {
						Element elem = (Element) nList.item(j);
						String bid = elem.getTextContent();
						bidder.addBid(bid);
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

	public static void main(String[] args) {
		ParseXML parser = new ParseXML();
		System.out.println("-----------------Bidders for Auctioneer 1--------------");
		for (int i = 0; i < parser.bidders1.size(); i++)
			parser.bidders1.get(i).print();
		System.out.println("-----------------Bidders for Auctioneer 2--------------");
		for (int i = 0; i < parser.bidders2.size(); i++)
			parser.bidders2.get(i).print();
	}
}
