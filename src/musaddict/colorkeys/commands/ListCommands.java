package musaddict.colorkeys.commands;

import java.util.ArrayList;
import java.util.HashMap;

import musaddict.colorkeys.CKDoor;
import musaddict.colorkeys.CKKey;
import musaddict.colorkeys.files.DebugFiles;
import musaddict.colorkeys.files.DoorFiles;
import musaddict.colorkeys.files.KeyFiles;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ListCommands {
	public static boolean list(CommandSender sender, String[] arg) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "You cannot use that command in the console.");
			return true; //Commands cannot be handled in the console 'cause sender isn't an instance of player.
		}
		Player player = (Player)sender;
		if (arg.length >= 2){ //list one specific location
			String location = arg[1];

			ArrayList<String> allLocations = new ArrayList<String>();

			for (CKDoor door : DoorFiles.getDoors())
				if (!allLocations.contains(door.location))
					allLocations.add(door.location);

			if (allLocations.size() == 0 || !allLocations.contains(location)) {
				player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "That location doesn't exist.");

				return true;
			}

			ArrayList<CKKey> keys = KeyFiles.getKeys(player.getName());

			if(keys != null) {
				if (keys.size() == 0) {
					player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "You have no keys for any world");

					return true;
				}

				String message = "";
				boolean found = false;

				if (DebugFiles.isDebugging(player))
					player.sendMessage("Keys size: " + keys.size());

				for(int i=0; i < keys.size(); i++) {
					if (DebugFiles.isDebugging(player))
						player.sendMessage("loc1 (arg): " + location + ", loc2 (key): " + keys.get(i).location);

					if(keys.get(i).location.equals(location)) { //Possible StringIndexOutOfBounds
						if (keys.get(i).world.getName().equals(player.getWorld().getName())) {
							found = true;

							if (keys.get(i).uses == 0) {
								message = message + ChatColor.GRAY + CommandHandler.noColorName(keys.get(i).color) + ", ";
							}
							else if(message.contains(ChatColor.GRAY + CommandHandler.noColorName(keys.get(i).color))) {
								if (keys.get(i).uses == -1)
									message.replace(ChatColor.GRAY + CommandHandler.noColorName(keys.get(i).color), ChatColor.GREEN + CommandHandler.noColorName(keys.get(i).color));
								else
									message.replace(ChatColor.GRAY + CommandHandler.noColorName(keys.get(i).color), ChatColor.GREEN + CommandHandler.noColorName(keys.get(i).color) + "(" + keys.get(i).uses + ")");
							}
							else {
								if (keys.get(i).uses == -1)
									message = message + ChatColor.GREEN + CommandHandler.noColorName(keys.get(i).color) + ", ";
								else
									message = message + ChatColor.GREEN + CommandHandler.noColorName(keys.get(i).color) + "(" + keys.get(i).uses + ")" + ", ";
							}
						}
					}
				}

				if (!found) {
					player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "You have no keys for " + ChatColor.AQUA + location + ChatColor.RED + " in " + ChatColor.BLUE + player.getWorld().getName());

					return true;
				}

				player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GRAY + "Keys for " + ChatColor.AQUA + location + ChatColor.GRAY + " within " + ChatColor.BLUE + player.getWorld().getName());
				player.sendMessage(ChatColor.GOLD + "Keys: " + message.substring(0, message.length()-2));

				return true;
			}
			else {//player not in KeyList
				player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "You have no keys for " + ChatColor.AQUA + location);

				return true;
			}
		}
		else { //list all locations with keys
			ArrayList<CKKey> keys = KeyFiles.getKeys(player.getName());

			if(keys != null) {
				if (keys.size() == 0) {
					player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "You have no keys in the world: " + ChatColor.AQUA + player.getWorld().getName());
					return true;
				}

				HashMap<String, Boolean> names = new HashMap<String, Boolean>(); //It is possible for the retrieved string to be "" (empty) if there are no keys

				for (int i=0; i < keys.size(); i++) {
					if (!names.containsKey(keys.get(i).location)) {
						if (keys.get(i).uses == 0) { //check for uses
							names.put(keys.get(i).location, false);
						}
						else { //has uses
							names.put(keys.get(i).location, true);
						}
					}
					else {
						if (keys.get(i).uses == 0){//check for uses (don't replace anything if uses == 0)
						}
						else {//has uses
							if (!names.get(keys.get(i).location)){//(makes sure its set to true)
								names.put(keys.get(i).location, true);
							}
						}
					}
				}
				String output = "";
				String[] message = names.toString().split(", ");
				for(int j = 0; j < message.length; j++){
					if (message[j].contains("true")){
						output = output.concat(ChatColor.GREEN + message[j]);
						output = output.replace("=true", ChatColor.GRAY + ", ");
					}
					else{
						output = output.concat(ChatColor.GRAY + message[j]);
						output = output.replace("=false", ", ");
					}
				}
				output = output.replace("{","");
				output = output.replace("}","");
				if(output.length() > 0){
					player.sendMessage(ChatColor.GOLD + "Locations: " + output.substring(0, output.length()-2));
				}
				names.clear();
				return true;
			}
			else {//player not in KeyList
				player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "You have no keys in the world: " + ChatColor.AQUA + player.getWorld().getName());

				return true;
			}
		}
	}





	public static boolean listAll(CommandSender sender, String[] arg) {
		boolean canUse = false;
		if (sender instanceof Player) {
			Player player = (Player)sender;
			if (player.isOp() || player.hasPermission("colorkeys.listall") || player.hasPermission("colorkeys.admin") || player.hasPermission("colorkeys.mod")) {
				canUse = true;
			}
		}
		else { //From console
			canUse = true;
		}
		if (canUse) {
			if (arg.length >= 2){ //list one specific location
				HashMap<String, ArrayList<Integer>> doorThings = new HashMap<String, ArrayList<Integer>>(); //List of <world, colors[]>

				ArrayList<CKDoor> doors = DoorFiles.getDoors();

				if (doors.size() > 0) {
					for (CKDoor door : doors) {
						if (door.location.equals(arg[1])) {
							if (doorThings.containsKey(door.world.getName())) {
								ArrayList<Integer> colorList = doorThings.get(door.world.getName());

								if (colorList != null) {
									if (colorList.size() > 0) {
										if (!colorList.contains(door.color)) {
											colorList.add(door.color);
										}
									}
									else {
										colorList.add(door.color);
									}
								}
								else {
									colorList = new ArrayList<Integer>();

									colorList.add(door.color);
								}

								doorThings.put(door.world.getName(), colorList);
							}
							else {
								ArrayList<Integer> colorList = new ArrayList<Integer>();

								colorList.add(door.color);

								doorThings.put(door.world.getName(), colorList);
							}
						}
					}

					if (doorThings.size() > 0) {
						sender.sendMessage(ChatColor.GOLD + "[CK] " +ChatColor.GRAY + "All doors for: " + ChatColor.AQUA + arg[1]);

						for (String world : doorThings.keySet()) {
							ArrayList<Integer> colorList = doorThings.get(world);
							String colors = "";

							for (int i = 0; i < colorList.size(); i++) {
								if (i > 0)
									colors += ChatColor.GRAY + ", ";

								colors += CommandHandler.colorName(colorList.get(i)) + ChatColor.GRAY + colorList.get(i);
							}

							//colors == "blue(10), red(2), orange(3)" etc   like this?
							sender.sendMessage(ChatColor.BLUE + world + ChatColor.GRAY + ": " + colors);
						}
					}
					else {
						sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GRAY + "No doors found.");
					}
				}
				else {
					sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GRAY + "No ColorKey doors have been created.");
				}

				return true;


				/*String location = arg[1];
				String[] Doors = ColorKeysFiles.DoorList.toString().split(", ");
				String output = "";
				for(int i=0; i < Doors.length; i++){
					int position = Doors[i].indexOf('=');
					Doors[i] = Doors[i].substring(0, position);
					String[] name = Doors[i].split(",");
					String[] args = name[0].split(";");
					String Location = args[1];
					int color = Integer.parseInt(args[2]);
					if(Location.equals(location)){
						output = output + colorName(color) + ChatColor.GRAY + color + ", ";
					}
				}
				sender.sendMessage(ChatColor.GOLD + "Doors: " + output.substring(0, output.length() - 2));
				return true;*/
			}
			else{
				ArrayList<CKDoor> doors = DoorFiles.getDoors();

				if (doors.size() == 0) {
					sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GRAY + "No ColorKey door have been created.");

					return true;
				}

				String output = "";

				for (CKDoor door : doors) {
					if (!output.contains("{" + door.location + "}")) {
						output += "{" + door.location + "}";
					}
				}

				output = output.replace("{","");
				output = output.replace("}",", ");

				if (output.length() > 0) {
					sender.sendMessage(ChatColor.GOLD + "All locations: " + ChatColor.GRAY + output.substring(0, output.length() - 2)); //output.length() - 2 will be -2 if output is empty.            .substring(0, -2) will throw StringIndexOutOfBoundsException
				}

				return true;
			}
		}
		else {
			sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "You do not have permission to use that command.");

			return true;
		}
	}
}
