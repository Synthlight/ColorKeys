package musaddict.colorkeys.listeners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import musaddict.colorkeys.CKDoor;
import musaddict.colorkeys.CKKey;
import musaddict.colorkeys.ColorKeys;
import musaddict.colorkeys.PartialCKDoor;
import musaddict.colorkeys.UnlockedDoors;
import musaddict.colorkeys.files.DebugFiles;
import musaddict.colorkeys.files.DoorFiles;
import musaddict.colorkeys.files.KeyFiles;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.material.Door;

public class PlayerListener implements Listener {
	public static ColorKeys plugin;
	private static final long doorAuotCloseDelay = 60L; //Amount of time to wait before closing the door after it was opened.
	private static final long doorAuotReLockDelay = 30 * 20; //Amount of time to wait before the "You unlocked the door" times-out and the door is locked again.
	private static final long delayBeforeOpenningDoor = 1L;

	public PlayerListener(ColorKeys instance) {
		plugin = instance;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		final Block block = event.getClickedBlock();

		if (block == null)
			return;

		if (ColorKeys.isSelecting(player)) {
			if (!(block.getState().getData() instanceof Door) && block.getType() != Material.WOOL) { //Selection mode but not a selectable block.
				return;
			}

			PartialCKDoor pDoor = new PartialCKDoor(player, block);

			if (pDoor.isValid) {
				if (pDoor.isBlockPartOfCKDoor()) {
					player.sendMessage(ChatColor.GOLD + "[CK] "+ ChatColor.GREEN + "Door " + ChatColor.AQUA + pDoor.toDoor().location + ChatColor.GREEN + " selected.");
				}
				else {
					player.sendMessage(ChatColor.GOLD + "[CK] "+ ChatColor.GREEN + "Door selected.");
				}

				ColorKeys.playerDoorSelection.put(player, pDoor);
			}
			else {
				if ((player.getItemInHand().getType() == Material.WOOD_DOOR || player.getItemInHand().getType() == Material.IRON_DOOR) && !(block.getState().getData() instanceof Door))
					return;

				if (ColorKeys.playerDoorSelection.containsKey(player))
					ColorKeys.playerDoorSelection.remove(player);

				player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "That is not a valid ColorKey door.");
			}

			event.setCancelled(true);
			return;
		}

		final CKDoor ckDoor = DoorFiles.getCKDoorFromDoorBlock(block);

		if (ckDoor != null) {
			if (ckDoor.isOpen()) { //If either half is open.
				player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "This door will close automatically.");
				event.setCancelled(true);

				return;
			}

			String usePerm = "colorkeys.use." + ckDoor.world.getName() + "." + ckDoor.location + "." + ckDoor.color;

			if (DebugFiles.isDebugging(player))
				player.sendMessage("usePerm: " + usePerm);

			if (event.getAction() == Action.RIGHT_CLICK_BLOCK) { //'Unlock' door
				event.setCancelled(true);

				if ((player.hasPermission(usePerm) || player.hasPermission("colorkeys.mod")) && plugin.getConfig().getBoolean("enable-admin-bypass")) {
					player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GRAY + "You unlocked the door. (Admin Bypass)");

					UnlockedDoors.add(player, ckDoor);

					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() { //Auto-close the door
						public void run() {
							UnlockedDoors.remove(player, ckDoor);
						}
					}, doorAuotReLockDelay);

					return;
				}

				CKKey key = KeyFiles.getKey(player.getName(), ckDoor);

				if (key != null) { //check if player has key
					if (key.uses == 0) {
						player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "Your key for this door is broken!");
					}
					else if (key.uses > 0) {
						if (key.uses == 1) {
							player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GRAY + "You have " + ChatColor.GOLD + key.uses + ChatColor.GRAY + " use left for this key. Left-click to open.");
							player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "This key will break if you open the door.");
						}
						else {
							player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GRAY + "You have " + ChatColor.GOLD + key.uses + ChatColor.GRAY + " uses left for this key. Left-click to open.");
						}

						UnlockedDoors.add(player, ckDoor);

						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() { //Auto-close the door
							public void run() {
								UnlockedDoors.remove(player, ckDoor);
							}
						}, doorAuotReLockDelay);
					}
					else {
						player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GRAY + "You unlocked the door.");

						UnlockedDoors.add(player, ckDoor);

						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() { //Auto-close the door
							public void run() {
								UnlockedDoors.remove(player, ckDoor);
							}
						}, doorAuotReLockDelay);
					}
				}
				else {
					player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "You don't have the key to this door.");

					event.setCancelled(true);
				}
			}
			else { //LEFT_CLICK            Open door and lower uses
				if ((player.isOp() || player.hasPermission("colorkeys.admin")) && plugin.getConfig().getBoolean("enable-admin-bypass")) { //Admin bypass
					event.setCancelled(false);

					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() { //Auto-close the door
						public void run() {
							ckDoor.open();
						}
					}, delayBeforeOpenningDoor);

					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() { //Auto-close the door
						public void run() {
							ckDoor.close();
						}
					}, doorAuotCloseDelay);

					return;
				}

				final ArrayList<CKDoor> unlockedDoorList = UnlockedDoors.getList(player);

				if (unlockedDoorList != null && unlockedDoorList.contains(ckDoor)) {// Attempting to open door
					if (DebugFiles.isDebugging(player))
						player.sendMessage("attempting to open");

					if (!player.hasPermission(usePerm) || (player.isOp() && !plugin.getConfig().getBoolean("enable-admin-bypass"))) { //If you don't have permission search through and lower the use of the correct key
						if (DebugFiles.isDebugging(player))
							player.sendMessage("doesn't have perm to open automatically");

						CKKey key = KeyFiles.getKey(player.getName(), ckDoor); //get's the players key to the door.

						if (key != null) {
							if (key.uses > 0) {
								key.uses -= 1;

								if (key.uses == 1){
									player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GRAY + "This key has " + ChatColor.GOLD + "one" + ChatColor.GRAY + " usage remaining.");
								}
								else if (key.uses == 0){
									player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GRAY + "Your key for this door " + ChatColor.RED + "broke" + ChatColor.GRAY + "!");
								}
								else{
									player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GRAY + "This key has " + ChatColor.GOLD + key.uses + ChatColor.GRAY + " uses remaining.");
								}

								KeyFiles.updateKey(player.getName(), key);
							}// else uses is infinite (no need to subtract)

							if (DebugFiles.isDebugging(player))
									player.sendMessage("Attempting to open door: " + ckDoor.toString());

							event.setCancelled(false);

							plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() { //Auto-close the door
								public void run() {
									ckDoor.open();
								}
							}, delayBeforeOpenningDoor);
						}
						else {
							if (DebugFiles.isDebugging(player))
									player.sendMessage("Error: can't find key: \"" + ckDoor.toString() + "\" for player: " + player.getName());
						}
							
					}

					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() { //Auto-close the door
						public void run() {
							ckDoor.close();
						}
					}, doorAuotCloseDelay);

					UnlockedDoors.remove(player, ckDoor);
				}
				else { //Its a different door or hasn't been unlocked yet. (keep closed)
					player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GRAY + "You need to " + ChatColor.RED + "unlock " + ChatColor.GRAY + "this door. (Right-Click)");
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		if (player.isOp() && plugin.getConfig().getBoolean("enable-version-notifier")) {
			try {
				URL version = new URL(ColorKeys.versionURL);
				URLConnection dc = version.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(dc.getInputStream()));
				String inputLine;

				while ((inputLine = in.readLine()) != null)
					if (!inputLine.equals(ColorKeys.info.getVersion())) {
						player.sendMessage(ChatColor.GRAY + "ColorKeys is Out of date! The latest version is:");
						player.sendMessage(ChatColor.GOLD + inputLine);
						player.sendMessage(ChatColor.GRAY + "You can download the latest version of CK here:");
						player.sendMessage("http://dev.bukkit.org/server-mods/colorkeys/files/");
					}
				in.close();
			}
			catch (IOException e) {
				if (DebugFiles.isDebugging(player))
					e.printStackTrace();
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		if (ColorKeys.playerDoorSelection.containsKey(player))
			ColorKeys.playerDoorSelection.remove(player);

		ColorKeys.setSelecting(player, false);

		if (BlockListener.playerDeletionConfirmation.containsKey(player))
			BlockListener.playerDeletionConfirmation.remove(player);

		UnlockedDoors.removeAll(player);
	}
}
