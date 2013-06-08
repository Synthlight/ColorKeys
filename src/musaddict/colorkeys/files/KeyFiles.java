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

import musaddict.colorkeys.CKDoor;
import musaddict.colorkeys.CKKey;
import musaddict.colorkeys.ColorKeys;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class KeyFiles {
	private static File KeysFile = new File(ColorKeys.mainDirectory + File.separator + "Keys.xml");

	private static HashMap<String, ArrayList<CKKey>> KeyList = new HashMap<String, ArrayList<CKKey>>(); //Format: <playerName, Key[]>

	public static void load() {
		ColorKeys.Log(Level.INFO, "Loading keys...");

		if (!KeysFile.exists())
			ColorKeys.Log("No keys to load.");
		else {
			KeyList = new HashMap<String, ArrayList<CKKey>>();
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = null; try {docBuilder = dbfac.newDocumentBuilder();} catch (ParserConfigurationException e) {}
			Document doc = null;

			try {
				doc = docBuilder.parse(KeysFile);
				doc.getDocumentElement().normalize();
			}
			catch (IOException | SAXException e) {
				ColorKeys.Log(Level.SEVERE, "Keys file is malformed.");

				e.printStackTrace();

				return;
			}

			try {
				@SuppressWarnings("unused")
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
								int color = Integer.parseInt(keyElement.getAttribute("color")),
									uses = Integer.parseInt(keyElement.getAttribute("uses")),
									initialUses = Integer.parseInt(keyElement.getAttribute("initialUses"));
								double price = Double.parseDouble(keyElement.getAttribute("price"));

								playerKeys.add(new CKKey(world.getName(), location, color, uses, initialUses, price));
							}
						}

						KeyList.put(playerElement.getAttribute("name"), playerKeys);
					}
				}
			}
			catch (Exception e) {
				ColorKeys.Log(Level.SEVERE, "Keys file is not in the expected format.");

				e.printStackTrace();

				return;
			}

			ColorKeys.Log(Level.INFO, "Keys loaded successfully.");
		}
	}

	public static boolean save() {
		if (KeyList.values().size() == 0) { //If queue is empty OR economy is disabled.
			KeysFile.delete();

			return true;
		}

		ColorKeys.Log(Level.INFO, "Saving keys...");

		try {
			if (!KeysFile.exists()) {
				if (!KeysFile.createNewFile()) {
					ColorKeys.Log(Level.SEVERE, "Error creating keys file.");

					return false;
				}
			}

			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			Element fileVersionElement = doc.createElement("file");
			fileVersionElement.setAttribute("version", "1.0");
			doc.appendChild(fileVersionElement);

			for (String player : KeyList.keySet()) {
				Element playerElement = doc.createElement("player");
				playerElement.setAttribute("name", player);
				fileVersionElement.appendChild(playerElement);

				for (CKKey key : KeyList.get(player)) {
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

			FileOutputStream OUT = new FileOutputStream(KeysFile);
			OUT.write(result.getWriter().toString().getBytes());
			OUT.flush();
			OUT.close();

			ColorKeys.Log(Level.INFO, "Keys saved successfully.");

			return true;
		}
		catch (Exception e) {
			ColorKeys.Log(Level.SEVERE, "Unknown error saving Keys.");

			e.printStackTrace();

			return false;
		}
	}

	public static boolean giveKey(String player, CKKey key) {
		if (playerHasKey(player, key)) //Check to see if the player already has that key.
			return false;

		ArrayList<CKKey> keys = null;

		if (KeyList.containsKey(player))
			keys = KeyList.get(player);
		else
			keys = new ArrayList<CKKey>();

		keys.add(key);
		KeyList.put(player, keys);

		return true;
	}

	public static ArrayList<CKKey> getKeys(String player) {
		if (KeyList.containsKey(player))
			return KeyList.get(player);
		else
			return null;
	}

	public static CKKey getKey(String player, String worldName, String location, int color) {
		CKKey keyToFind = new CKKey(worldName, location, color, -1, -1);
		ArrayList<CKKey> keys = null;

		if (KeyList.containsKey(player))
			keys = KeyList.get(player);
		else
			return null;

		for (int i = 0; i < keys.size(); i++)
			if (keyToFind.equals(keys.get(i)))
				return keys.get(i);

		return null;
	}

	public static CKKey getKey(String player, CKDoor door) {
		return getKey(player, door.world.getName(), door.location, door.color);
	}

	public static boolean removeKey(String player, CKKey key) {
		if (!playerHasKey(player, key)) //Check to make sure the player has that key.
			return false;

		ArrayList<CKKey> keys = null;

		if (KeyList.containsKey(player))
			keys = KeyList.get(player);
		else
			return false;

		for (int i = 0; i < keys.size(); i++)
			if (key.equals(keys.get(i))) {
				keys.remove(i);

				break;
			}

		if (keys.size() == 0)
			KeyList.remove(player);
		else
			KeyList.put(player, keys);

		return true;
	}

	public static void removeKey(CKKey key) {
		for (String player : KeyList.keySet()) { //Search through all players
			removeKey(player, key);
		}
	}

	@Deprecated
	public static boolean repairKey(String player, CKKey key) {
		if (!playerHasKey(player, key)) //Check to make sure the player has that key.
			return false;

		ArrayList<CKKey> keys = null;

		if (KeyList.containsKey(player))
			keys = KeyList.get(player);
		else
			return false;

		for (int i = 0; i < keys.size(); i++)
			if (key.equals(keys.get(i))) {
				CKKey temp = keys.get(i);

				temp.uses = temp.initialUses;

				keys.set(i, temp);

				break;
			}

		KeyList.put(player, keys);

		return true;
	}

	public static boolean updateKey(String player, CKKey key) {
		if (!playerHasKey(player, key)) //Check to make sure the player has that key.
			return false;

		ArrayList<CKKey> keys = null;

		if (KeyList.containsKey(player))
			keys = KeyList.get(player);
		else
			return false;

		for (int i = 0; i < keys.size(); i++)
			if (key.equals(keys.get(i))) {
				keys.set(i, key);

				break;
			}

		KeyList.put(player, keys);

		return true;
	}

	public static boolean playerHasKey(String player, String worldName, String location, int color) {
		CKKey keyToFind = new CKKey(worldName, location, color, -1, -1);

		return playerHasKey(player, keyToFind);
	}

	public static boolean playerHasKey(String player, CKKey key) {
		if (KeyList.containsKey(player))
			return KeyList.get(player).contains(key);
		else
			return false;
	}
}
