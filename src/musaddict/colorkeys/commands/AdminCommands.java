package musaddict.colorkeys.commands;

import musaddict.colorkeys.CKDoor;
import musaddict.colorkeys.ColorKeys;
import musaddict.colorkeys.PartialCKDoor;
import musaddict.colorkeys.files.DebugFiles;
import musaddict.colorkeys.files.DoorFiles;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminCommands {
	public static boolean select(CommandSender sender, String[] arg) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "You cannot use that command in the console.");
			return true;
		}

		Player player = (Player)sender;

		if (player.isOp() || player.hasPermission("colorkeys.select") || player.hasPermission("colorkeys.admin")) {
			boolean selectingState = ColorKeys.isSelecting(player);

			if (selectingState)
					player.sendMessage(ChatColor.GOLD + "Selecting disabled");
			else
					player.sendMessage(ChatColor.GOLD + "Selecting enabled");

			ColorKeys.setSelecting(player, !selectingState);
		}
		else {
			player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "You do not have permission to use that command.");
		}
		return true;
	}





	public static boolean create(CommandSender sender, String[] arg, ColorKeys plugin) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "You cannot use that command in the console.");
			return true; //Commands cannot be handled in the console 'cause sender isn't an instance of player.
		}

		Player player = (Player)sender;

		if (player.isOp() || player.hasPermission("colorkeys.create") || player.hasPermission("colorkeys.admin")) {
			if (arg.length > 1) {
				if (!ColorKeys.playerDoorSelection.containsKey(player)) {
					player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "No valid door was selected. Nothing was saved.");

					return true;
				}

				PartialCKDoor pDoor = ColorKeys.playerDoorSelection.get(player);
				String location = arg[1];
				String worldName = pDoor.woolBaseBlock.getWorld().getName();
				int color = (int) pDoor.woolBaseBlock.getData();

				if (DoorFiles.doorExists(pDoor.woolBaseBlock) || DoorFiles.doorExists(pDoor.otherWoolBaseBlock)) {
					player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "That selection is already a CK door."); //TODO: Customize message

					return true;
				}

				if (ColorKeys.economyEnabled) { //if iConomy is enabled, warn the player about Location Name length
					if (location.length() > 15) {
						player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "Warning: " + ChatColor.GRAY + "Location names must have less than 16 characters to use in sign shops.");
					}
				}

				if (DoorFiles.doorExists(pDoor.woolBaseBlock) || DoorFiles.doorExists(pDoor.otherWoolBaseBlock) || DoorFiles.doorExists(worldName, location, color)) {
					player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "The " + CommandHandler.colorName(color) + ChatColor.RED + "door at " + ChatColor.AQUA + location + ChatColor.RED + " already exists.");
				}
				else {
					CKDoor ckDoor = null;

					if (pDoor.isDouble)
						ckDoor = new CKDoor(worldName, location, color, pDoor.woolBaseBlock, pDoor.otherWoolBaseBlock);
					else
						ckDoor = new CKDoor(worldName, location, color, pDoor.woolBaseBlock);

					DoorFiles.addDoor(ckDoor);
					DoorFiles.save();

					player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GREEN + "You have created the " + CommandHandler.colorName(color) + ChatColor.GREEN + "door for " + ChatColor.AQUA + location);

					if (DebugFiles.isDebugging(player))
						player.sendMessage("end-selection-after-action: " + plugin.getConfig().getBoolean("end-selection-after-action"));

					//clear selection
					if (plugin.getConfig().getBoolean("end-selection-after-action")) {
						ColorKeys.playerDoorSelection.remove(player);
						ColorKeys.setSelecting(player, false);
						player.sendMessage(ChatColor.GOLD + "Selecting disabled");
					}

					//close the new door
					ckDoor.close();
				}
			}
			else {
				player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "Too few arguments.");
				player.sendMessage(ChatColor.GRAY + "/ck create [location]");
			}
		}
		else{
			player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "You do not have permission to use that command.");
		}

		return true;
	}
}
