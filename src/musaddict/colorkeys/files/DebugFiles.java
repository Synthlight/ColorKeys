package musaddict.colorkeys.files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
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

import musaddict.colorkeys.ColorKeys;

import org.bukkit.entity.Player;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DebugFiles {
	private static File DebugFile = new File(ColorKeys.mainDirectory + File.separator + "Debuggers.xml");

	private static HashMap<String, Boolean> Debuggers = new HashMap<String, Boolean>();

	/**
	 * Loads all Debuggers from disk.
	 */
	public static void load() {
		ColorKeys.Log(Level.INFO, "Loading Debuggers...");

		if (!DebugFile.exists())
			ColorKeys.Log("No Debuggers to load.");
		else {
			Debuggers = new HashMap<String, Boolean>();
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = null; try {docBuilder = dbfac.newDocumentBuilder();} catch (ParserConfigurationException e) {}
			Document doc = null;

			try {
				doc = docBuilder.parse(DebugFile);
				doc.getDocumentElement().normalize();
			}
			catch (IOException | SAXException e) {
				ColorKeys.Log(Level.SEVERE, "Debuggers file is malformed.");

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

				NodeList DebugNodeList = doc.getElementsByTagName("player");

				for (int i = 0; i < DebugNodeList.getLength(); i++) {
					Node DebugNode = DebugNodeList.item(i);

					if (DebugNode.getNodeType() == Node.ELEMENT_NODE) {
						Element DebugElement = (Element) DebugNode;

						String name = DebugElement.getAttribute("name");
						boolean debugging = Boolean.parseBoolean(DebugElement.getAttribute("debugging"));

						Debuggers.put(name, debugging);
					}
				}
			}
			catch (Exception e) {
				ColorKeys.Log(Level.SEVERE, "Debuggers file is not in the expected format.");

				e.printStackTrace();

				return;
			}

			ColorKeys.Log(Level.INFO, "Debuggers loaded successfully.");
		}
	}

	/**
	 * Saves all loaded Debugs to disk.
	 */
	public static boolean save() {
		if (Debuggers.size() == 0) { //If queue is empty OR economy is disabled.
			DebugFile.delete();

			return true;
		}

		ColorKeys.Log(Level.INFO, "Saving Debuggers...");

		try {
			if (!DebugFile.exists()) {
				if (!DebugFile.createNewFile()) {
					ColorKeys.Log(Level.SEVERE, "Error creating Debuggers file.");

					return false;
				}
			}

			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			Element fileVersionElement = doc.createElement("file");
			fileVersionElement.setAttribute("version", "1.0");
			doc.appendChild(fileVersionElement);

			for (String player : Debuggers.keySet()) {
				Element DebugElement = doc.createElement("player");
				DebugElement.setAttribute("name", player);
				DebugElement.setAttribute("debugging", Debuggers.get(player) + "");
				fileVersionElement.appendChild(DebugElement);
			}

			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			//trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(doc);
			trans.transform(source, result);

			FileOutputStream OUT = new FileOutputStream(DebugFile);
			OUT.write(result.getWriter().toString().getBytes());
			OUT.flush();
			OUT.close();

			ColorKeys.Log(Level.INFO, "Debuggers Saved Successfully.");

			return true;
		}
		catch (Exception e) {
			ColorKeys.Log(Level.SEVERE, "Unknown error saving Debuggers.");

			e.printStackTrace();

			return false;
		}
	}

	public static boolean isDebugging(String player) {
		if (Debuggers.containsKey(player))
			return Debuggers.get(player);
		else
			return false;
	}

	public static boolean isDebugging(Player player) {
		return isDebugging(player.getName());
	}

	public static boolean isDebugging() {
		return Debuggers.size() > 0;
	}

	public static void setDebugging(String player, boolean value) {
		if (value)
			Debuggers.put(player, value);
		else
			Debuggers.remove(player);
	}

	public static void setDebugging(Player player, boolean value) {
		setDebugging(player.getName(), value);
	}
}
