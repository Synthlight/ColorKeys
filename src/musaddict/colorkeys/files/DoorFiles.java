package musaddict.colorkeys.files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
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
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Door;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DoorFiles {
	private static File DoorFile = new File(ColorKeys.mainDirectory + File.separator + "Doors.xml");

	private static ArrayList<CKDoor> DoorList = new ArrayList<CKDoor>();

	/**
	 * Loads all doors from disk.
	 */
	public static void load() {
		ColorKeys.Log(Level.INFO, "Loading doors...");

		if (!DoorFile.exists())
			ColorKeys.Log("No doors to load.");
		else {
			DoorList = new ArrayList<CKDoor>();
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = null; try {docBuilder = dbfac.newDocumentBuilder();} catch (ParserConfigurationException e) {}
			Document doc = null;

			try {
				doc = docBuilder.parse(DoorFile);
				doc.getDocumentElement().normalize();
			}
			catch (IOException | SAXException e) {
				ColorKeys.Log(Level.SEVERE, "Doors file is malformed.");

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

				NodeList doorNodeList = doc.getElementsByTagName("door");

				for (int i = 0; i < doorNodeList.getLength(); i++) {
					Node doorNode = doorNodeList.item(i);

					if (doorNode.getNodeType() == Node.ELEMENT_NODE) {
						Element doorElement = (Element) doorNode;

						World world = Bukkit.getWorld(doorElement.getAttribute("world"));
						String location = doorElement.getAttribute("location");
						int color = Integer.parseInt(doorElement.getAttribute("color"));
						int x = Integer.parseInt(doorElement.getAttribute("x")),
							y = Integer.parseInt(doorElement.getAttribute("y")),
							z = Integer.parseInt(doorElement.getAttribute("z"));

						if (fileVersion >= 1.1)
						{
							boolean isDouble = Boolean.parseBoolean(doorElement.getAttribute("isDouble"));
							int otherX = Integer.parseInt(doorElement.getAttribute("otherX")),
								otherY = Integer.parseInt(doorElement.getAttribute("otherY")),
								otherZ = Integer.parseInt(doorElement.getAttribute("otherZ"));

							DoorList.add(new CKDoor(world.getName(), location, color, x, y, z, isDouble, otherX, otherY, otherZ));
						}
						else {
							DoorList.add(new CKDoor(world.getName(), location, color, x, y, z));
						}
					}
				}
			}
			catch (Exception e) {
				ColorKeys.Log(Level.SEVERE, "Doors file is not in the expected format.");

				e.printStackTrace();

				return;
			}

			ColorKeys.Log(Level.INFO, "Doors loaded successfully.");
		}
	}

	/**
	 * Saves all loaded doors to disk.
	 */
	public static boolean save() {
		if (DoorList.size() == 0) { //If queue is empty OR economy is disabled.
			DoorFile.delete();

			return true;
		}

		ColorKeys.Log(Level.INFO, "Saving doors...");

		try {
			if (!DoorFile.exists()) {
				if (!DoorFile.createNewFile()) {
					ColorKeys.Log(Level.SEVERE, "Error creating doors file.");

					return false;
				}
			}

			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			Element fileVersionElement = doc.createElement("file");
			fileVersionElement.setAttribute("version", "1.1");
			doc.appendChild(fileVersionElement);

			for (CKDoor door : DoorList) {
				Element doorElement = doc.createElement("door");
				doorElement.setAttribute("world", door.world.getName());
				doorElement.setAttribute("location", door.location);
				doorElement.setAttribute("color", door.color + "");
				doorElement.setAttribute("x", door.x + "");
				doorElement.setAttribute("y", door.y + "");
				doorElement.setAttribute("z", door.z + "");
				doorElement.setAttribute("isDouble", door.isDouble + "");
				doorElement.setAttribute("otherX", door.otherX + "");
				doorElement.setAttribute("otherY", door.otherY + "");
				doorElement.setAttribute("otherZ", door.otherZ + "");
				fileVersionElement.appendChild(doorElement);
			}

			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			//trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(doc);
			trans.transform(source, result);

			FileOutputStream OUT = new FileOutputStream(DoorFile);
			OUT.write(result.getWriter().toString().getBytes());
			OUT.flush();
			OUT.close();

			ColorKeys.Log(Level.INFO, "Doors saved successfully.");

			return true;
		}
		catch (Exception e) {
			ColorKeys.Log(Level.SEVERE, "Unknown error saving doors.");

			e.printStackTrace();

			return false;
		}
	}

	/**
	 * Adds a door.
	 */
	public static boolean addDoor(CKDoor door) {
		if (!DoorList.contains(door))
			DoorList.add(door);
		else
			return false;

		return true;
	}

	/**
	 * Removes the door.
	 */
	public static boolean removeDoor(CKDoor door) {
		if (DoorList.size() == 0) //Check to make sure the player has that key.
			return false;

		if (DoorList.contains(door))
			DoorList.remove(door);
		else
			return false;

		return true;
	}

	/**
	 * Either returns the Door object if found or null otherwise.
	 */
	public static CKDoor getDoor(Block block) {
		for (CKDoor door : DoorList)
			for (Block dBlock : door.getBlocks())
				if (dBlock.equals(block))
					return door;

		return null;
	}

	/**
	 * Either returns the Door object if found or null otherwise.
	 */
	public static CKDoor getCKDoorFromDoorBlock(Block block) {
		if (block.getState().getData() instanceof Door) {
			Door door = (Door) (block.getState().getData());

			if (door.isTopHalf())
				return DoorFiles.getDoor(block.getRelative(BlockFace.DOWN, 2));
			else
				return DoorFiles.getDoor(block.getRelative(BlockFace.DOWN));
		}
		else
			return null;
	}

	/**
	 * Returns a list of all doors.
	 */
	public static ArrayList<CKDoor> getDoors() {
		return DoorList;
	}

	/**
	 * Returns true if the door exists or false otherwise.
	 */
	public static boolean doorExists(String worldName, String location, int color) {
		CKDoor door = new CKDoor(worldName, location, color);

		return DoorList.contains(door);
	}

	/**
	 * Returns true if the door for the given key exists or false otherwise.
	 */
	public static boolean doorExists(CKKey key) {
		CKDoor door = new CKDoor(key.world.getName(), key.location, key.color);

		return DoorList.contains(door);
	}

	/**
	 * Returns true if the door exists or false otherwise.
	 */
	public static boolean doorExists(Block block) {
		return getDoor(block) != null;
	}
}
