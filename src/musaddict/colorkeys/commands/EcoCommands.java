package musaddict.colorkeys.commands;

import java.util.ArrayList;

import musaddict.colorkeys.CKKey;
import musaddict.colorkeys.ColorKeys;
import musaddict.colorkeys.files.DoorFiles;
import musaddict.colorkeys.files.KeyFiles;
import musaddict.colorkeys.files.QueueFiles;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EcoCommands {
	public static boolean queue(CommandSender sender, String[] arg) {
		//Some command to list all the keys in the COD queue.
		//arg[1] (optional) must be valid target player
		//If not specified this command will assume the current player is the target
		Player targetPlayer = null;

		if (arg.length > 1)
			targetPlayer = Bukkit.getPlayer(arg[1]);
		else if (sender instanceof Player)
			targetPlayer = (Player) sender;

		if (targetPlayer == null) {
			sender.sendMessage(ChatColor.RED + "That player is either offline or does not exist.");
			sender.sendMessage(ChatColor.GRAY + " /ck cod [player]");

			return true;
		}

		if (QueueFiles.CODKeys.containsKey(targetPlayer.getName())) {
			sender.sendMessage(ChatColor.GOLD + "--Your Queue--" + ChatColor.GOLD + "#- " + ChatColor.BLUE + "[world]" + ChatColor.GRAY
					+ ":" + ChatColor.AQUA + "[loc]" + ChatColor.GRAY + ", [color] " + ChatColor.DARK_RED + "Uses: " + ChatColor.GRAY
					+ "[amt] " + ChatColor.GREEN + "Price: [amt]");

			ArrayList<CKKey> keys = QueueFiles.CODKeys.get(targetPlayer.getName());

			for (CKKey key : new ArrayList<CKKey>(keys)) { //To check for and remove keys that the player already has in his inventory.
				if (KeyFiles.playerHasKey(targetPlayer.getName(), key)) {
					//sender.sendMessage("Removing key (already owned): " + key.world.getName() + ";" + key.location + ";" + key.color);
					keys.remove(key);
				}
			}

			if (keys.size() == 0) {
				sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GRAY + "Your queue has been empied. You already had the listed keyes."); //All keys in the queue were duplicates of the player's keys and have been removed from the queue, tell the player this.

				return true;
			}

			for (CKKey key : new ArrayList<CKKey>(keys)) { //To check for and remove keys that are for a door that no longer exists.
				if (!DoorFiles.doorExists(key)) {
					//sender.sendMessage("Removing key (door gone): " + key.world.getName() + ";" + key.location + ";" + key.color);
					keys.remove(key);
				}
			}

			if (keys.size() == 0) {
				sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GRAY + "Your queue has been empied. The doors have been deleted."); //All keys in the queue were for CK doors that no longer exist and have been removed from the queue, tell the player this.

				return true;
			}

			if (keys.size() > 0) {
				for (int i = 0; i < keys.size(); i++)
					sender.sendMessage(ChatColor.GOLD + "" + i + "- "
					 + ChatColor.BLUE + keys.get(i).world.getName() + ChatColor.GRAY +  ":"
					  + ChatColor.AQUA + keys.get(i).location + ChatColor.GRAY + ", "
					   + CommandHandler.colorName(keys.get(i).color) //colorName add a space.
					    + ChatColor.DARK_RED + "Uses: " + ChatColor.GRAY + keys.get(i).uses //Might be infinite?
					     + ChatColor.GREEN + " Price: " + keys.get(i).price);

				sender.sendMessage(ChatColor.GRAY + "Use /ck accept (ID|all) to accept keys.");
			}
			else {
				sender.sendMessage(ChatColor.RED + "Your queue is empty."); //Message: Player has nothing in his queue
			}
		}
		else
			sender.sendMessage(ChatColor.RED + "Your queue is empty."); //Message: Player has nothing in his queue

		return true;
	}





	public static boolean accept(CommandSender sender, String[] arg) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "You cannot use that command in the console.");
			return true; //Commands cannot be handled in the console 'cause sender isn't an instance of player.
		}
		//Needs to account for multiple keys being sent to the player.
		//Need to make sure that the key/door is still valid and wasn't removed in the interim.
		//Checks to make sure the key isn't already in the player's possession.
		//arg[1] is either 'all' to try to accept/buy all in the queue or the number of the key to buy (see above command [temp name] to list all keys in the queue)
		//If there is only one key in the queue ignore the arg[1]

		Player player = (Player) sender;

		if (QueueFiles.CODKeys.containsKey(player.getName())) {
			ArrayList<CKKey> keys = QueueFiles.CODKeys.get(player.getName());

			if (keys.size() > 0) {
				if (arg.length == 1 && keys.size() > 1) {
					sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "Please specify which key you'd like to accept."); //Message: You did not specify a key (or all) but you have more than one
				}
				else {
					for (CKKey key : new ArrayList<CKKey>(keys)) { //To check for and remove keys that the player already has in his inventory.
						if (KeyFiles.playerHasKey(player.getName(), key)) {
							//sender.sendMessage("Removing key (already owned): " + key.world.getName() + ";" + key.location + ";" + key.color);
							keys.remove(key);
						}
					}

					if (keys.size() == 0) {
						sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GRAY + "Your queue has been empied. You already had the listed  keyes."); //All keys in the queue were duplicates of the player's keys and have been removed from the queue, tell the player this.

						return true;
					}

					for (CKKey key : new ArrayList<CKKey>(keys)) { //To check for and remove keys that are for a door that no longer exists.
						if (!DoorFiles.doorExists(key)) {
							//sender.sendMessage("Removing key (door gone): " + key.world.getName() + ";" + key.location + ";" + key.color);
							keys.remove(key);
						}
					}

					if (keys.size() == 0) {
						sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GRAY + "Your queue has been empied. All doors were deleted."); //All keys in the queue were for CK doors that no longer exist and have been removed from the queue, tell the player this.

						return true;
					}

					boolean all = false;

					if (arg.length == 1) {
						all = true;
					}
					else if (arg.length > 1) {
						if (arg[1].equals("all")) {
							all = true;
						}
					}

					if (all) {
						Double totalPrice = 0d;

						for (CKKey key : keys) {
							totalPrice += key.price;
						}

						if (ColorKeys.economyEnabled && ColorKeys.economy != null) {
							if(ColorKeys.economy.hasAccount(player.getName())) {
								Double balance = ColorKeys.economy.getBalance(player.getName());

								if (totalPrice > balance) {
									sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "You can't afford to buy all those keys.");
								}
								else {
									for (CKKey key : keys) {
										ColorKeys.economy.withdrawPlayer(player.getName(), key.price);
										KeyFiles.giveKey(player.getName(), key);
										player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GREEN + "You bought the " + CommandHandler.colorName(key.color) + ChatColor.GREEN + " key to " + ChatColor.AQUA + key.location + ChatColor.GREEN + "!");
										player.sendMessage(ChatColor.BLUE + "Price: " + ChatColor.GRAY + key.price + "    " + ChatColor.GREEN + "Balance: " + ChatColor.GRAY + ColorKeys.economy.getBalance(player.getName()));
									}

									QueueFiles.CODKeys.remove(player.getName());
								}
							}
							else { //They don't have an account; I don't know what the ramification of this are.
								sender.sendMessage("No Account?!?!?!?");
							}
						}
					}
					else {
						int keyToGive = -1;

						try {
							keyToGive = Integer.parseInt(arg[1]);
						}
						catch (NumberFormatException ex) {
							sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "Invalid key. Please enter the numeric ID of the key you'd like to accept."); //Message: Not a number!!

							return true;
						}

						if (keyToGive < 0 || keyToGive >= keys.size()) {
							sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "Invalid ID. Please chose an ID from your queue."); //Message: Key index must be a positive integer > 0
						}
						else {
							if (ColorKeys.economyEnabled && ColorKeys.economy != null) {
								//Check for price on key (at index keyToGive) and give the key if they have enough.
								if(ColorKeys.economy.hasAccount(player.getName())) {
									Double balance = ColorKeys.economy.getBalance(player.getName());

									if (keys.get(keyToGive).price > balance) {
										sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "You can't afford the chosen key(s)"); //Message: You can't afford that yet! Go get some more cash you bum, then try this command again.
									}
									else {
										ColorKeys.economy.withdrawPlayer(player.getName(), keys.get(keyToGive).price);
										KeyFiles.giveKey(player.getName(), keys.get(keyToGive));
										player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GREEN + "You bought the " + CommandHandler.colorName(keys.get(keyToGive).color) + ChatColor.GREEN + " key to " + ChatColor.AQUA + keys.get(keyToGive).location + ChatColor.GREEN + "!");
										player.sendMessage(ChatColor.BLUE + "Price: " + ChatColor.GRAY + keys.get(keyToGive).price + "    " + ChatColor.GREEN + "Balance: " + ChatColor.GRAY + ColorKeys.economy.getBalance(player.getName()));
										keys.remove(keyToGive);

										if (keys.size() > 0)
											QueueFiles.CODKeys.put(player.getName(), keys);
										else
											QueueFiles.CODKeys.remove(player.getName());
									}
								}
								else { //They don't have an account; I don't know what the ramification of this are.
									sender.sendMessage("No Account?!?!?!?");
								}
							}
							else {
								if (!ColorKeys.economyEnabled) {
									sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.DARK_RED + " WARNING: Economy disabled in ColorKeys Configuration.");
								}
								else if (ColorKeys.economy == null) {
									sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.DARK_RED + " WARNING: No economy plugin detected.");
								}
							}
						}
					}
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + "Your queue is empty.");
			}
		}
		else {
			sender.sendMessage(ChatColor.RED + "Your queue is empty.");
		}

		return true;
	}





	public static boolean deny(CommandSender sender, String[] arg) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "You cannot use that command in the console.");
			return true; //Commands cannot be handled in the console 'cause sender isn't an instance of player.
		}
		//Needs to account for multiple keys being sent to the player.
		//arg[1] is either 'all' to try to reject all in the queue or the number of the key to reject (see above command [temp name] to list all keys in the queue)
		//If there is only one key in the queue ignore the arg[1]

		Player player = (Player) sender;

		if (QueueFiles.CODKeys.containsKey(player.getName())) {
			ArrayList<CKKey> keys = QueueFiles.CODKeys.get(player.getName());

			if (keys.size() > 0) {
				if (arg.length == 1 && keys.size() > 1) {
					sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "Please specify which key you'd like to deny."); //Message: You did not specify a key (or all) but you have more than one
				}
				else {
					boolean all = false;

					if (arg.length > 1) {
						if (arg[1].equals("all")) {
							all = true;
						}
					}

					if (all) {
						QueueFiles.CODKeys.remove(player.getName());
					}
					else {
						int keyToGive = -1;

						try {
							keyToGive = Integer.parseInt(arg[1]);
						}
						catch (NumberFormatException ex) {
							sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "Invalid key. Please enter the numeric ID of the key you'd like to deny."); //Message: Not a number!!

							return true;
						}

						if (keyToGive < 0 || keyToGive >= keys.size()) {
							sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "Invalid ID. Please chose an ID from your queue."); //Message: Key index must be a positive integer > 0
						}
						else {
							player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GREEN + "You denied the " + CommandHandler.colorName(keys.get(keyToGive).color) + ChatColor.GREEN + " key to " + ChatColor.AQUA + keys.get(keyToGive).location + ChatColor.GREEN + "!");
							keys.remove(keyToGive);
							QueueFiles.CODKeys.put(player.getName(), keys);
						}
					}
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + "Your queue is empty.");
			}
		}
		else {
			sender.sendMessage(ChatColor.RED + "Your queue is empty.");
		}
		return true;
	}





	public static boolean cod(CommandSender sender, String[] arg) {
		String commandUse = "/ck cod [player] [location] [color] [price] (-world|-w=[world]) (-uses|-u=[uses])";
		boolean canUse = false;

		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (player.isOp() || player.hasPermission("colorkeys.give") || player.hasPermission("colorkeys.admin") || player.hasPermission("colorkeys.mod")) {
				canUse = true;
			}
		}
		else { //From console
			canUse = true;
		}

		if (canUse) {
			if (arg.length < 5) {
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



			//location
			String location = arg[2];



			//color (start)
			int color = -1;

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



			//price (start)
			double price = -1;

			try {
				price = Double.parseDouble(arg[4]);

				if (price < 1) {
					sender.sendMessage(ChatColor.RED + "Price must me a positive number greater than 0.");
					sender.sendMessage(ChatColor.GRAY + " " + commandUse);

					return true;
				}
			}
			catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + "Price must me a positive number greater than 0.");
				sender.sendMessage(ChatColor.GRAY + " " + commandUse);

				return true;
			}
			//price (end)




			CKKey newKey = new CKKey(world.getName(), location, color, uses, uses, price);

			if(!KeyFiles.playerHasKey(giveTo.getName(), newKey)) { //Make sure the player dosen't already have that key
				if(DoorFiles.doorExists(world.getName(), location, color)) { //Makes sure target door actually exists
					if (ColorKeys.economyEnabled && ColorKeys.economy != null) {
						if (QueueFiles.CODKeys.containsKey(giveTo.getName())) {
							ArrayList<CKKey> keys = QueueFiles.CODKeys.get(giveTo.getName());
							boolean found = false;

							if (keys.size() > 0)
								for (CKKey key : keys)
									if (key.world.equals(world) && key.location.equals(location) && key.color == color)
										found = true;

							if (!found) {
								keys.add(newKey);
								QueueFiles.CODKeys.put(giveTo.getName(), new ArrayList<CKKey>(keys));
								giveTo.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GRAY + "The " + CommandHandler.colorName(color) + ChatColor.GRAY + " key for " + ChatColor.AQUA + location + ChatColor.GRAY + " has been added to your queue.");
								giveTo.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GRAY + " '/ck q' to view your queue.");
								sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GRAY + "The " + CommandHandler.colorName(color) + ChatColor.GRAY + " key for " + ChatColor.AQUA + location + ChatColor.GRAY + " has been queued for" + ChatColor.GREEN + giveTo.getName() + ChatColor.GRAY + ".");
							}
							else {
								sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "The " + CommandHandler.colorName(color) + ChatColor.RED + " key for " + ChatColor.AQUA + location + ChatColor.RED + " is already queued for" + ChatColor.GREEN + giveTo.getName() + ChatColor.RED + ".");
							}
						}
						else {
							ArrayList<CKKey> keys = new ArrayList<CKKey>();
							keys.add(newKey);

							QueueFiles.CODKeys.put(giveTo.getName(), new ArrayList<CKKey>(keys));
							giveTo.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GRAY + "The " + CommandHandler.colorName(color) + ChatColor.GRAY + " key for " + ChatColor.AQUA + location + ChatColor.GRAY + " has been added to your cue.");
							giveTo.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GRAY + " '/ck q' to view your queue.");
							sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GRAY + "The " + CommandHandler.colorName(color) + ChatColor.GRAY + " key for " + ChatColor.AQUA + location + ChatColor.GRAY + " has been queued for" + ChatColor.GREEN + giveTo.getName() + ChatColor.GRAY + ".");
						}

						return true;
					}
					else {
						if (!ColorKeys.economyEnabled) {
							sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.DARK_RED + " WARNING: Economy disabled in ColorKeys Configuration.");
						}
						else if (ColorKeys.economy == null) {
							sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.DARK_RED + " WARNING: No economy plugin detected.");
						}
					}
					return true;
				}
				else {
					sender.sendMessage(ChatColor.RED + "That door does not exist.");
					sender.sendMessage(ChatColor.GRAY + " /ck give [player] [world] [location] [color] [uses] [price]");

					return true;
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + "That player already has that key. Did you mean " + ChatColor.AQUA + "/ck repair" + ChatColor.RED + "?");

				return true;
			}
		}
		return true;
	}
}
