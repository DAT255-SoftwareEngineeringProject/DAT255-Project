package absolut.can;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Copied from ecm-core for our can system
 */
public class CanConfigParser {
	private static HashMap<String, Integer> senders = new HashMap<String, Integer>();
	private static HashMap<Integer, String> receivers = new HashMap<Integer, String>();
	
	public static void parseCanConfig(String path) {
		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder dombuilder;
		InputStream is = null;

		try {
			dombuilder = domfac.newDocumentBuilder();
			is = new FileInputStream(path);
			Document doc = dombuilder.parse(is);
			// vehicle
			Element root = doc.getDocumentElement();

			NodeList canList = root.getElementsByTagName("can");
			for (int i = 0; i < canList.getLength(); i++) {
				// can
				Element canElement = (Element) canList.item(i);
				
				Element roleElement = (Element) canElement.getElementsByTagName("role").item(0);
				if(roleElement == null) {
					throw new RuntimeException("Error: there are no role element within can element");
				}
				String roleStr = roleElement.getTextContent();
				
				// id
				Element idElement = (Element) canElement.getElementsByTagName("id").item(0);
				if(idElement == null) {
					throw new RuntimeException("Error: there are no id element within can element");
				}
				String idStr = idElement.getTextContent();
				int idInt = Integer.parseInt(idStr);
				
				if("sender".equals(roleStr)) {
					// name
					Element nameElement = (Element) canElement.getElementsByTagName("name").item(0);
					if(nameElement == null) {
						throw new RuntimeException("Error: there is no name element within can element");
					}
					String nameStr = nameElement.getTextContent();
					senders.put(nameStr, idInt);
				} else if("receiver".equals(roleStr)) {
					// function
					Element functionElement = (Element) canElement.getElementsByTagName("function").item(0);
					if(functionElement == null) {
						throw new RuntimeException("Error: there are no function element within can element");
					}
					String functionStr = functionElement.getTextContent();
					receivers.put(idInt, functionStr);
				} else {
					throw new RuntimeException("Error: there are wrong value in the role element");
				}
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}

	}
	
	public static HashMap<String, Integer> getSenders() {
		return senders;
	}
	
	public static HashMap<Integer, String> getReceivers() {
		return receivers;
	}
}
