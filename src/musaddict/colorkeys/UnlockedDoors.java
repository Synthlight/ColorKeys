package musaddict.colorkeys;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.entity.Player;

public class UnlockedDoors {
	private static HashMap<Player, ArrayList<CKDoor>> playerUnlockedDoors = new HashMap<Player, ArrayList<CKDoor>>();

	public static void add(final Player player, final CKDoor door) {
		ArrayList<CKDoor> unlockedDoorList = null;

		if (playerUnlockedDoors.containsKey(player)) {
			unlockedDoorList = playerUnlockedDoors.get(player);

			if (unlockedDoorList.contains(door))
				return;
			else
				unlockedDoorList.add(door);
		}
		else {
			unlockedDoorList = new ArrayList<CKDoor>();

			unlockedDoorList.add(door);
		}

		playerUnlockedDoors.put(player, unlockedDoorList);
	}

	public static void remove(final Player player, final CKDoor door) {
		ArrayList<CKDoor> unlockedDoorList = null;

		if (playerUnlockedDoors.containsKey(player)) {
			unlockedDoorList = playerUnlockedDoors.get(player);

			if (unlockedDoorList.contains(door))
				unlockedDoorList.remove(door);
		}
		else
			return;

		if (unlockedDoorList.size() > 0)
			playerUnlockedDoors.put(player, unlockedDoorList);
		else
			playerUnlockedDoors.remove(player);
	}

	public static void removeAll(final Player player) {
		if (playerUnlockedDoors.containsKey(player))
			playerUnlockedDoors.remove(player);
		else
			return;
	}

	public static ArrayList<CKDoor> getList(final Player player) {
		if (playerUnlockedDoors.containsKey(player))
			return playerUnlockedDoors.get(player);
		else
			return null;
	}
}
