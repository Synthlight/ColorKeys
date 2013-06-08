package musaddict.colorkeys;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import musaddict.colorkeys.files.DoorFiles;
import musaddict.colorkeys.files.KeyFiles;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;

@Deprecated
public class ColorKeysFiles {
	public static File DoorsFile = new File(ColorKeys.mainDirectory + File.separator + "Doors.data");
	public static File KeysFile = new File(ColorKeys.mainDirectory + File.separator + "Keys.data");

	//public static HashMap<String, Block> DoorList = new HashMap<String, Block>(); //Format: world;location;color to match block for door
	//public static HashMap<String, String[]> KeyList = new HashMap<String, String[]>(); //Format: Player to match a string array of world;location;color,uses
	//private static String resourcePath = "plugins" + File.separator + "ColorKeys";

	//public static HashMap<String, KeyHelper[]> KeyList = new HashMap<String, KeyHelper[]>(); //Format: Player to match a string array of world;location;color,uses

	public static class DoorHelper implements Serializable {
		private static final long serialVersionUID = 7526472295622776147L;

		private UUID world;
		private int x, y, z;
		//private transient Block block;

		public DoorHelper(Block b) {
			super();

			if (b == null)
				System.out.println("This should not be!");

			world = b.getWorld().getUID();
			x = b.getX();
			y = b.getY();
			z = b.getZ();
		}

		public Block getBlock(ColorKeys ck) {
			return ck.getServer().getWorld(world).getBlockAt(x, y, z);
		}

		private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
			//always perform the default de-serialization first
			aInputStream.defaultReadObject();
		}

		private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
			//perform the default serialization for all non-transient, non-static fields
			aOutputStream.defaultWriteObject();
		}
	}

	public static HashMap<String, Block> fromSerializableMap(HashMap<String, DoorHelper> dl) {
		HashMap<String, Block> newMap = new HashMap<String, Block>();

		if (!dl.isEmpty())
			for (String s : dl.keySet()) {
				Block b = Bukkit.getServer().getWorld(dl.get(s).world).getBlockAt(dl.get(s).x, dl.get(s).y, dl.get(s).z);
				newMap.put(s, b);
			}

		return newMap; //May return empty map if no doors to load.
	}

	@SuppressWarnings("unchecked")
	public static HashMap<String, Block> loadDoor() {
		try{
			FileInputStream FIS = new FileInputStream(DoorsFile);
			ObjectInputStream IN = new ObjectInputStream(FIS);
			Object result = IN.readObject();
			ColorKeys.Log("Old Doors file loaded successfully!");
			IN.close();
			FIS.close();
			return fromSerializableMap((HashMap<String, DoorHelper>) result);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
















	public static void loadDoors() {
		HashMap<String, Block> oldDoors = loadDoor();

		if (oldDoors == null) {
			ColorKeys.Log(Level.SEVERE, "Unable to load old Doors file.");

			return;
		}

		for (String d : oldDoors.keySet()) {
			String[] parts = d.split(";");

			String worldName = parts[0],
				location = parts[1];
			int color = Integer.parseInt(parts[2]);

			Block block = oldDoors.get(d);

			int x = block.getX(),
				y = block.getY(),
				z = block.getZ();

			DoorFiles.addDoor(new CKDoor(worldName, location, color, x, y, z));
		}

		if (DoorFiles.save()) {
			ColorKeys.Log(Level.INFO, "Old Doors file converted to new format successfully; removing old Doors file.");

			if (!DoorsFile.delete()) {
				ColorKeys.Log(Level.INFO, "Unable to remove old Doors file; marking for deletion on exit.");

				DoorsFile.deleteOnExit();
			}
		}
	}









	public static void loadKeys() {
		HashMap<String, String[]> oldKeys = loadKey();

		if (oldKeys == null) {
			ColorKeys.Log(Level.SEVERE, "Unable to load old Keys file.");

			return;
		}

		for (String player : oldKeys.keySet()) {
			for (String k : oldKeys.get(player)) {
				String[] parts;
				int uses = -1;

				if (k.contains(",")) {
					String[] p2 = k.split(",");
					parts = p2[0].split(";");
					uses = Integer.parseInt(p2[1]);
				}
				else
					parts = k.split(";");

				KeyFiles.giveKey(player, new CKKey(parts[0], parts[1], Integer.parseInt(parts[2]), uses, uses));
			}
		}

		if (KeyFiles.save()) {
			ColorKeys.Log(Level.INFO, "Old Keys file converted to new format successfully; removing old Keys file.");

			if (!KeysFile.delete()) {
				ColorKeys.Log(Level.INFO, "Unable to remove old Keys file; marking for deletion on exit.");

				KeysFile.deleteOnExit();
			}
		}
	}














	@SuppressWarnings("unchecked")
	public static HashMap<String, String[]> loadKey() {
		try{
			FileInputStream FIS = new FileInputStream(KeysFile);
			ObjectInputStream IN = new ObjectInputStream(FIS);
			Object result = IN.readObject();
			ColorKeys.Log("Old Keys file loaded successfully!");
			IN.close();
			FIS.close();
			return (HashMap<String,String[]>)result;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
}
