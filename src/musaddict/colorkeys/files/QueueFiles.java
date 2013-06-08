package musaddict.colorkeys.files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import musaddict.colorkeys.CKKey;
import musaddict.colorkeys.ColorKeys;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class QueueFiles {
	private static File QueueFile = new File(ColorKeys.mainDirectory + File.separator + "Queue.xml");

	public static HashMap<String, ArrayList<CKKey>> CODKeys = new HashMap<String, ArrayList<CKKey>>(); //Format: <playerName, Key[]>

	public static void load() {
		if (!ColorKeys.economyEnabled)
			return;

		ColorKeys.Log(Level.INFO, "Loading COD queue...");

		if (!QueueFile.exists())
			ColorKeys.Log("No COD queue to load.");
		else {
			CODKeys = new HashMap<String, ArrayList<CKKey>>();
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = null; try {docBuilder = dbfac.newDocumentBuilder();} catch (ParserConfigurationException e) {}
			Document doc = null;

			try {
				doc = docBuilder.parse(QueueFile);
				doc.getDocumentElement().normalize();
			}
			catch (IOException | SAXException e) {
				ColorKeys.Log(Level.SEVERE, "COD queue file is malformed.");

				e.printStackTrace();

				return;
			}

			try {
				Double fileVersion = -1.0;

				NodeList fileVersionNodeList = doc.getElementsByTagName("file");

				if (fileVersionNodeList.getLength() > 0) {
					for (int i = 0; i < fileVersionNodeList.getLength(); i++) {
						Node fileVersionNode = fileVersionNodeList.item(i);

						if (fileVersionNode.getNodeType() == Node.ELEMENT_NODE) {
							Element fileVersionElement = (Element) fileVersionNode;

							fileVersion = Double.parseDouble(fileVersionElement.getAttribute("version"));
						}
					}
				}
				else {
					fileVersion = 1.0;
				}

				NodeList playerNodeList = doc.getElementsByTagName("player");

				for (int i = 0; i < playerNodeList.getLength(); i++) {
					Node playerNode = playerNodeList.item(i);

					if (playerNode.getNodeType() == Node.ELEMENT_NODE) {
						Element playerElement = (Element) playerNode;

						NodeList keyNodeList = playerElement.getElementsByTagName("key");
						ArrayList<CKKey> playerKeys = new ArrayList<CKKey>();

						for (int s = 0; s < keyNodeList.getLength(); s++) {
							Node keyNode = keyNodeList.item(s);

							if (keyNode.getNodeType() == Node.ELEMENT_NODE) {
								Element keyElement = (Element) keyNode;

								World world = Bukkit.getWorld(keyElement.getAttribute("world"));
								String location = keyElement.getAttribute("location");
								int color = Integer.parseInt(keyElement.getAttribute("color")), uses = Integer.parseInt(keyElement.getAttribute("uses")), initialUses = -1;
								double price = Double.parseDouble(keyElement.getAttribute("price"));

								if (fileVersion > 1.0)
									initialUses = Integer.parseInt(keyElement.getAttribute("initialUses"));

								playerKeys.add(new CKKey(world.getName(), location, color, uses, initialUses, price));
							}
						}

						CODKeys.put(playerElement.getAttribute("name"), playerKeys);
					}
				}
			}
			catch (Exception e) {
				ColorKeys.Log(Level.SEVERE, "COD queue file is not in the expected format.");

				e.printStackTrace();

				return;
			}

			ColorKeys.Log(Level.INFO, "COD queue loaded successfully.");
		}
	}

	public static void save() {
		if (!ColorKeys.economyEnabled)
			return;

		if (CODKeys.values().size() == 0) { //If queue is empty OR economy is disabled.
			QueueFile.delete();

			return;
		}

		ColorKeys.Log(Level.INFO, "Saving COD queue...");

		try {
			if (!QueueFile.exists()) {
				if (!QueueFile.createNewFile()) {
					ColorKeys.Log(Level.SEVERE, "Error creating COD queue file.");

					return;
				}
			}

			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			Element fileVersionElement = doc.createElement("file");
			fileVersionElement.setAttribute("version", "1.1");
			doc.appendChild(fileVersionElement);

			for (String player : CODKeys.keySet()) {
				Element playerElement = doc.createElement("player");
				playerElement.setAttribute("name", player);
				fileVersionElement.appendChild(playerElement);

				for (CKKey key : CODKeys.get(player)) {
					Element keyElement = doc.createElement("key");
					keyElement.setAttribute("world", key.world.getName());
					keyElement.setAttribute("location", key.location);
					keyElement.setAttribute("color", key.color + "");
					keyElement.setAttribute("uses", key.uses + "");
					keyElement.setAttribute("initialUses", key.initialUses + "");
					keyElement.setAttribute("price", key.price + "");
					playerElement.appendChild(keyElement);
				}
			}

			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			//trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(doc);
			trans.transform(source, result);

			FileOutputStream OUT = new FileOutputStream(QueueFile);
			OUT.write(result.getWriter().toString().getBytes());
			OUT.flush();
			OUT.close();

			ColorKeys.Log(Level.INFO, "COD queue saved successfully.");
		}
		catch (Exception e) {
			ColorKeys.Log(Level.SEVERE, "Unknown error saving COD queue.");

			e.printStackTrace();

			return;
		}
	}
}
