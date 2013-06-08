package musaddict.colorkeys.commands;

import musaddict.colorkeys.CKDoor;
import musaddict.colorkeys.CKKey;
import musaddict.colorkeys.ColorKeys;
import musaddict.colorkeys.files.DoorFiles;
import musaddict.colorkeys.files.KeyFiles;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ModCommands {
	public static boolean give(CommandSender sender, String[] arg, ColorKeys plugin) {
		String commandUse = "/ck give [player] [location] [color] (-world|-w=[world]) (-uses|-u=[uses])";
		boolean canUse = false;

		if (sender instanceof Player) {
			Player player = (Player)sender;
 			if (player.isOp() || player.hasPermission("colorkeys.give") || player.hasPermission("colorkeys.admin") || player.hasPermission("colorkeys.mod")) {
 				canUse = true;
 			}
		}
		else { //From console
			canUse = true;
		}

		if (canUse) {
			if (arg.length < 2
			|| (arg.length < 4 && !(sender instanceof Player))
			|| (arg.length < 4 && sender instanceof Player && !ColorKeys.isSelecting((Player) sender))) {
				sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "Too few arguments.");
				sender.sendMessage(ChatColor.GRAY + " " + commandUse);

				return true;
			}





			//player (start)
			Player giveTo = Bukkit.getPlayer(arg[1]);

			if (giveTo == null) {
				sender.sendMessage(ChatColor.RED + "The specified player '" + arg[1] + "' is either offline or does not exist.");
				sender.sendMessage(ChatColor.GRAY + " " + commandUse);

				return true;
			}
			//player (end)





			//world (start)
			World world = null;

			for (String ar : arg) {
				String foundWorld = "";

				if (ar.contains("-world="))
					foundWorld = ar.replace("-world=", "");
				else if (ar.contains("-w="))
					foundWorld = ar.replace("-w=", "");

				if (!foundWorld.equals("")) {
					world = Bukkit.getWorld(foundWorld);

					if (world == null) {
						sender.sendMessage(ChatColor.RED + "The specified world '" + foundWorld + "' could not be found.");
						sender.sendMessage(ChatColor.GRAY + " " + commandUse);

						return true;
					}
					else
						break;
				}
			}

			if (world == null) {
				if (sender instanceof Player) {
					world = ((Player) sender).getWorld();
				}
				else {
					sender.sendMessage(ChatColor.RED + "You must specify a world if using this command from the console.");
					sender.sendMessage(ChatColor.GRAY + " " + commandUse);
				}
			}
			//world (end)





			//uses (start)
			int uses = -1;

			for (String ar : arg) {
				String foundUses = "";

				if (ar.contains("-uses="))
					foundUses = ar.replace("-uses=", "");
				else if (ar.contains("-u="))
					foundUses = ar.replace("-u=", "");

				if (!foundUses.equals("")) {
					try {
						uses = Integer.parseInt(foundUses);

						if (uses < 1) {
							sender.sendMessage(ChatColor.RED + "Uses must me a positive number greater than 0. (Omit this for infinite uses)");
							sender.sendMessage(ChatColor.GRAY + " " + commandUse);

							return true;
						}
					}
					catch (NumberFormatException e) {
						sender.sendMessage(ChatColor.RED + "The specified world use ammount '" + foundUses + "' is not a number.");
						sender.sendMessage(ChatColor.GRAY + " " + commandUse);

						return true;
					}
				}
			}
			//uses (end)



			String location = "";
			int color = -1;



			if (sender instanceof Player && ColorKeys.isSelecting((Player) sender) && ColorKeys.playerDoorSelection.containsKey((Player) sender)) {
				CKDoor ckDoor = ColorKeys.playerDoorSelection.get((Player) sender).toDoor();

				if (ckDoor != null) {
					location = ckDoor.location;
					color = ckDoor.color;
				}
				else {
					sender.sendMessage(ChatColor.RED + "The shortened version of this command requires that the selection be an existing door.");
					sender.sendMessage(ChatColor.GRAY + " " + commandUse);

					return true;
				}
			}
			else {
				//location
				location = arg[2];


				//color (start)
				color = -1;

				try {
					color = Integer.parseInt(arg[3]);

					if (color > 15 || color < 0) { //Must be between 0-15 (inclusively) to be valid.
						sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "The given color code '" + color + "' is out of bounds.");
						sender.sendMessage(ChatColor.GRAY + "/ck color");

						return true;
					}
				}
				catch (NumberFormatException e) {
					color = CommandHandler.stringToColor(arg[3]); //Will convert 'red' to the corresponding int value

					if (color == -1) { //Catch the error if the color string name was incorrect (typo) //specified color does not exist (ie: player entered: 'reed' or some other wrong name)
						sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "The given color name \"" + arg[3] + "\" is incorrect.");
						sender.sendMessage(ChatColor.GRAY + "/ck color");

						return true;
					}
				}
				//color (end)
			}






			CKKey newKey = new CKKey(world.getName(), location, color, uses, uses);

			if (!KeyFiles.playerHasKey(giveTo.getName(), newKey)) { //Make sure the player dosen't already have that key
				if (DoorFiles.doorExists(world.getName(), location, color)) { //Makes sure target door actually exists
					KeyFiles.giveKey(giveTo.getName(), newKey);
					sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GREEN + "You gave the " + CommandHandler.colorName(color) + ChatColor.GREEN + "key to " + ChatColor.AQUA + location + " " + ChatColor.GREEN + "to " + ChatColor.AQUA + giveTo.getName());
					giveTo.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GREEN + "You were given the " + CommandHandler.colorName(color) + ChatColor.GREEN + "key to " + ChatColor.AQUA + location + ChatColor.GREEN + "!");

					//clear selection
					if (sender instanceof Player && ColorKeys.isSelecting((Player) sender) && plugin.getConfig().getBoolean("end-selection-after-action")) {
						ColorKeys.playerDoorSelection.remove((Player) sender);
						ColorKeys.setSelecting((Player) sender, false);
						sender.sendMessage(ChatColor.GOLD + "Selecting disabled");
					}
				}
				else {
					sender.sendMessage(ChatColor.RED + "That door does not exist.");
					sender.sendMessage(ChatColor.GRAY + " " + commandUse);
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + "That player already has that key. Did you mean " + ChatColor.AQUA + "/ck repair" + ChatColor.RED + "?");
			}
		}

		return true;
	}





	public static boolean repair(CommandSender sender, String[] arg, ColorKeys plugin) {
		String commandUse = "/ck repair [player] [location] [color] (-world|-w=[world]) (-uses|-u=[uses])";
		boolean canUse = false;

		if (sender instanceof Player) {
			Player player = (Player)sender;
			if (player.isOp() || player.hasPermission("colorkeys.repair") || player.hasPermission("colorkeys.admin") || player.hasPermission("colorkeys.mod")) {
				canUse = true;
			}
		}
		else { //From console
			canUse = true;
		}

		if (canUse) {
			if (arg.length < 2
			|| (arg.length < 4 && !(sender instanceof Player))
			|| (arg.length < 4 && sender instanceof Player && !ColorKeys.isSelecting((Player) sender))) {
				sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "Too few arguments.");
				sender.sendMessage(ChatColor.GRAY + " " + commandUse);

				return true;
			}





			//player (start)
			Player giveTo = Bukkit.getPlayer(arg[1]);

			if (giveTo == null) {
				sender.sendMessage(ChatColor.RED + "The specified player '" + arg[1] + "' is either offline or does not exist.");
				sender.sendMessage(ChatColor.GRAY + " " + commandUse);

				return true;
			}
			//player (end)





			//world (start)
			World world = null;

			for (String ar : arg) {
				String foundWorld = "";

				if (ar.contains("-world="))
					foundWorld = ar.replace("-world=", "");
				else if (ar.contains("-w="))
					foundWorld = ar.replace("-w=", "");

				if (!foundWorld.equals("")) {
					world = Bukkit.getWorld(foundWorld);

					if (world == null) {
						sender.sendMessage(ChatColor.RED + "The specified world '" + foundWorld + "' could not be found.");
						sender.sendMessage(ChatColor.GRAY + " " + commandUse);

						return true;
					}
					else
						break;
				}
			}

			if (world == null) {
				if (sender instanceof Player) {
					world = ((Player) sender).getWorld();
				}
				else {
					sender.sendMessage(ChatColor.RED + "You must specify a world if using this command from the console.");
					sender.sendMessage(ChatColor.GRAY + " " + commandUse);
				}
			}
			//world (end)





			//uses (start)
			int uses = -1;

			for (String ar : arg) {
				String foundUses = "";

				if (ar.contains("-uses="))
					foundUses = ar.replace("-uses=", "");
				else if (ar.contains("-u="))
					foundUses = ar.replace("-u=", "");

				if (!foundUses.equals("")) {
					try {
						uses = Integer.parseInt(foundUses);

						if (uses < 1) {
							sender.sendMessage(ChatColor.RED + "Uses must me a positive number greater than 0. (Omit this for infinite uses)");
							sender.sendMessage(ChatColor.GRAY + " " + commandUse);

							return true;
						}
					}
					catch (NumberFormatException e) {
						sender.sendMessage(ChatColor.RED + "The specified world use ammount '" + foundUses + "' is not a number.");
						sender.sendMessage(ChatColor.GRAY + " " + commandUse);

						return true;
					}
				}
			}
			//uses (end)



			String location = "";
			int color = -1;



			if (sender instanceof Player && ColorKeys.isSelecting((Player) sender) && ColorKeys.playerDoorSelection.containsKey((Player) sender)) {
				CKDoor ckDoor = ColorKeys.playerDoorSelection.get((Player) sender).toDoor();

				if (ckDoor != null) {
					location = ckDoor.location;
					color = ckDoor.color;
				}
				else {
					sender.sendMessage(ChatColor.RED + "The shortened version of this command requires that the selection be an existing door.");
					sender.sendMessage(ChatColor.GRAY + " " + commandUse);

					return true;
				}
			}
			else {
				//location
				location = arg[2];


				//color (start)
				try {
					color = Integer.parseInt(arg[3]);

					if (color > 15 || color < 0) { //Must be between 0-15 (inclusively) to be valid.
						sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "The given color code '" + color + "' is out of bounds.");
						sender.sendMessage(ChatColor.GRAY + "/ck color");

						return true;
					}
				}
				catch (NumberFormatException e) {
					color = CommandHandler.stringToColor(arg[3]); //Will convert 'red' to the corresponding int value

					if (color == -1) { //Catch the error if the color string name was incorrect (typo) //specified color does not exist (ie: player entered: 'reed' or some other wrong name)
						sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "The given color name \"" + arg[3] + "\" is incorrect.");
						sender.sendMessage(ChatColor.GRAY + "/ck color");

						return true;
					}
				}
				//color (end)
			}





			CKKey key = KeyFiles.getKey(giveTo.getName(), world.getName(), location, color);

			if (key == null) {
				sender.sendMessage(ChatColor.RED + "That player doesn't have that key. Did you mean " + ChatColor.AQUA + "/ck give" + ChatColor.RED + "?");

				return true;
			}





			if(DoorFiles.doorExists(world.getName(), location, color)) { //Makes sure target door actually exists
				if (uses == -1)
					key.uses = key.initialUses;
				else
					key.uses = uses;

				KeyFiles.updateKey(giveTo.getName(), key);

				sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GREEN + "You repaired the " + CommandHandler.colorName(color) + ChatColor.GREEN + "key to " + ChatColor.AQUA + location + " " + ChatColor.GREEN + "for " + ChatColor.AQUA + giveTo.getName());
				giveTo.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GREEN + "Your " + CommandHandler.colorName(color) + ChatColor.GREEN + "key to " + ChatColor.AQUA + location + ChatColor.GREEN + " has been repaired!");

				//clear selection
				if (sender instanceof Player && ColorKeys.isSelecting((Player) sender) && plugin.getConfig().getBoolean("end-selection-after-action")) {
					ColorKeys.playerDoorSelection.remove((Player) sender);
					ColorKeys.setSelecting((Player) sender, false);
					sender.sendMessage(ChatColor.GOLD + "Selecting disabled");
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + "That door does not exist.");
				sender.sendMessage(ChatColor.GRAY + " /ck repair [player] [world] [location] [color] (uses)");
			}
		}

		return true;
	}





	public static boolean remove(CommandSender sender, String[] arg, ColorKeys plugin) {
		String commandUse = "/ck rm|remove [player] [location] [color] (-world|-w=[world])";
		boolean canUse = false;

		if (sender instanceof Player) {
			Player player = (Player)sender;
 			if (player.isOp() || player.hasPermission("colorkeys.remove") || player.hasPermission("colorkeys.admin") || player.hasPermission("colorkeys.mod")) {
 				canUse = true;
 			}
		}
		else { //From console
			canUse = true;
		}

		if (canUse) {
			if (arg.length < 2
			|| (arg.length < 4 && !(sender instanceof Player))
			|| (arg.length < 4 && sender instanceof Player && !ColorKeys.isSelecting((Player) sender))) {
				sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "Too few arguments.");
				sender.sendMessage(ChatColor.GRAY + " " + commandUse);

				return true;
			}





			//player (start)
			Player giveTo = Bukkit.getPlayer(arg[1]);

			if (giveTo == null) {
				sender.sendMessage(ChatColor.RED + "The specified player '" + arg[1] + "' is either offline or does not exist.");
				sender.sendMessage(ChatColor.GRAY + " " + commandUse);

				return true;
			}
			//player (end)





			//world (start)
			World world = null;

			for (String ar : arg) {
				String foundWorld = "";

				if (ar.contains("-world="))
					foundWorld = ar.replace("-world=", "");
				else if (ar.contains("-w="))
					foundWorld = ar.replace("-w=", "");

				if (!foundWorld.equals("")) {
					world = Bukkit.getWorld(foundWorld);

					if (world == null) {
						sender.sendMessage(ChatColor.RED + "The specified world '" + foundWorld + "' could not be found.");
						sender.sendMessage(ChatColor.GRAY + " " + commandUse);

						return true;
					}
					else
						break;
				}
			}

			if (world == null) {
				if (sender instanceof Player) {
					world = ((Player) sender).getWorld();
				}
				else {
					sender.sendMessage(ChatColor.RED + "You must specify a world if using this command from the console.");
					sender.sendMessage(ChatColor.GRAY + " " + commandUse);
				}
			}
			//world (end)





			String location = "";
			int color = -1;



			if (sender instanceof Player && ColorKeys.isSelecting((Player) sender) && ColorKeys.playerDoorSelection.containsKey((Player) sender)) {
				CKDoor ckDoor = ColorKeys.playerDoorSelection.get((Player) sender).toDoor();

				if (ckDoor != null) {
					location = ckDoor.location;
					color = ckDoor.color;
				}
				else {
					sender.sendMessage(ChatColor.RED + "The shortened version of this command requires that the selection be an existing door.");
					sender.sendMessage(ChatColor.GRAY + " " + commandUse);

					return true;
				}
			}
			else {
				//location
				location = arg[2];


				//color (start)
				try {
					color = Integer.parseInt(arg[3]);

					if (color > 15 || color < 0) { //Must be between 0-15 (inclusively) to be valid.
						sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "The given color code '" + color + "' is out of bounds.");
						sender.sendMessage(ChatColor.GRAY + "/ck color");

						return true;
					}
				}
				catch (NumberFormatException e) {
					color = CommandHandler.stringToColor(arg[3]); //Will convert 'red' to the corresponding int value

					if (color == -1) { //Catch the error if the color string name was incorrect (typo) //specified color does not exist (ie: player entered: 'reed' or some other wrong name)
						sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "The given color name \"" + arg[3] + "\" is incorrect.");
						sender.sendMessage(ChatColor.GRAY + "/ck color");

						return true;
					}
				}
				//color (end)
			}






			CKKey key = KeyFiles.getKey(giveTo.getName(), world.getName(), location, color);

			if(key != null) { //Make sure we have a valid color
				KeyFiles.removeKey(giveTo.getName(), key);

				sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GREEN + "The " + CommandHandler.colorName(color) + ChatColor.GREEN + "for " + ChatColor.AQUA + location + ChatColor.GREEN + " has been removed from ");
				sender.sendMessage(ChatColor.GRAY + giveTo.getName() + "'s " + ChatColor.GREEN + "key list." );

				//clear selection
				if (sender instanceof Player && ColorKeys.isSelecting((Player) sender) && plugin.getConfig().getBoolean("end-selection-after-action")) {
					ColorKeys.playerDoorSelection.remove((Player) sender);
					ColorKeys.setSelecting((Player) sender, false);
					sender.sendMessage(ChatColor.GOLD + "Selecting disabled");
				}
			}
			else{
				sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "That player does not have that key.");
			}
		}

		return true;
	}
}
