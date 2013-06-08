package musaddict.colorkeys.listeners;

import java.util.ArrayList;

import musaddict.colorkeys.CKKey;
import musaddict.colorkeys.ColorKeys;
import musaddict.colorkeys.commands.CommandHandler;
import musaddict.colorkeys.files.DoorFiles;
import musaddict.colorkeys.files.KeyFiles;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignShops implements Listener {
	private ArrayList<String> confirmPurchase = new ArrayList<String>();

	public ColorKeys plugin;

	public SignShops(ColorKeys plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getBlock() != null) { //Will prevent an NPE if the user right-clicks air.
			if (event.getBlock().getState() != null) {
				if (event.getBlock().getState() instanceof Sign) {
					Sign sign = (Sign) event.getBlock().getState();

					if (ChatColor.stripColor(sign.getLines()[0]).equalsIgnoreCase("[CK]")) { //if it's a colorkey sign
						Player player = event.getPlayer();

						if (!player.isOp() && !player.hasPermission("colorkeys.sign.create") && !player.hasPermission("colorkeys.admin")) {
							player.sendMessage(ChatColor.DARK_RED + "You do not have permission to destroy CK signs.");
							event.setCancelled(true);
							final Location bl = event.getBlock().getLocation();
							plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
								public void run() {
									bl.getBlock().getState().update();
								}
							}, 20L);
						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPlaceEvent(BlockPlaceEvent event) {
		if (event.getBlockAgainst() == null) { //Will prevent an NPE if the user right-clicks air.
			return;
		}

		if (event.getBlockAgainst().getState() == null) {
			return;
		}

		if (event.getBlockAgainst().getState() instanceof Sign){
			Sign sign = (Sign) event.getBlockAgainst().getState();

			if (ChatColor.stripColor((sign.getLine(0))).equalsIgnoreCase("[CK]")) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		Player player = event.getPlayer();
		//Sign sign = (Sign)event.getBlock().getState();
		String[] lines = event.getLines();

		if (lines[0].equalsIgnoreCase("[CK]") || lines[0].equalsIgnoreCase("CK")) {

			if(player.isOp() || player.hasPermission("colorkeys.sign.create") || player.hasPermission("colorkeys.admin")){

				if (!lines[2].contains(",")) {
					player.sendMessage("Line 3 has no ','!");

					return;
				}

				String[] signLine = lines[2].replace(" ", "").split(",");

				if (signLine.length < 2) {
					player.sendMessage("Line 3 dosen't have enough ','!");

					return;
				}

				String world = player.getWorld().getName();
				String location = lines[1];
				int color = -1;
				int uses = -1;
				Double price = -1.0;

				try {
					color = Integer.parseInt(signLine[0]);
				}
				catch (NumberFormatException e) {
					color = CommandHandler.stringToColor(signLine[0]); //Will convert 'red' to the corresponding int value

					if (color == -1) {
						player.sendMessage("Unable to parse color: " + signLine[0]);

						return;
					}
				}

				try {
					uses = Integer.parseInt(signLine[1]);
				}
				catch (NumberFormatException e) {
					player.sendMessage("Unable to parse uses: " + signLine[1]);

					return;
				}

				try {
					price = Double.parseDouble(ChatColor.stripColor(lines[3]));
				}
				catch (NumberFormatException e) {
					player.sendMessage("Unable to parse price: " + lines[3]);

					return;
				}

				if (DoorFiles.doorExists(world, location, color)) {
					event.setLine(0,  ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "CK" + ChatColor.DARK_GRAY + "]");
					event.setLine(1, ChatColor.AQUA + location);
					event.setLine(2, CommandHandler.signColor(color) + ChatColor.DARK_GRAY + ", " + uses);
					event.setLine(3, ChatColor.GREEN + "" + price);
					player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GREEN + "Successfully created a CK Sign Shop!");
				}
				else {
					event.setLine(0, ChatColor.DARK_GRAY + "[" + ChatColor.RED + "CK" + ChatColor.DARK_GRAY + "]");
					event.setLine(1, ChatColor.DARK_GRAY + "This door..");
					event.setLine(2, ChatColor.DARK_GRAY + "Is a lie!!!");
					event.setLine(3, "");
				}
			}
			else {
				event.setLine(0, ChatColor.DARK_GRAY + "[" + ChatColor.RED + "CK" + ChatColor.DARK_GRAY + "]");
				event.setLine(1, ChatColor.DARK_GRAY + "You can not");
				event.setLine(2, ChatColor.DARK_GRAY + "create CK");
				event.setLine(3, ChatColor.DARK_GRAY + "shop signs!");
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !ColorKeys.economyEnabled)
			return;

		if (event.getClickedBlock() == null) { //Will prevent an NPE if the user right-clicks air.
			return;
		}

		if (event.getClickedBlock().getState() == null) { //I added this, did it help at all? seemed to
			return;
		}

		if (event.getClickedBlock().getState() instanceof Sign){
			final Player player = event.getPlayer();

			Sign sign = (Sign) event.getClickedBlock().getState();
			String[] lines = sign.getLines();

			//line 1 [ColorKeys]
			//line 2 [location]
			//line 3 [color], [uses]
			//line 4 [price]

			String signLine1 = ChatColor.stripColor(lines[0]);

			if (signLine1.equalsIgnoreCase("[CK]")) { //if it's a colorkey sign

				if (player.isOp() || player.hasPermission("colorkeys.admin") || player.hasPermission("colorkeys.mod") || player.hasPermission("colorkeys.sign.use")) {

					if (lines[2].contains(",")) { //checks if the bottom line is splitable

						String[] signLine = ChatColor.stripColor(lines[2]).replace(" ", "").split(",");

						if (signLine.length < 2) {
							player.sendMessage("Line 3 dosen't have enough ','!");

							return;
						}

						String world = player.getWorld().getName();
						String location = ChatColor.stripColor(lines[1]);
						int color = -1;
						int uses = -1;
						Double price = -1.0;

						try {
							color = Integer.parseInt(ChatColor.stripColor(signLine[0]));
						}
						catch (NumberFormatException e) {
							color = CommandHandler.stringToColor(signLine[0]); //Will convert 'red' to the corresponding int value

							if (color == -1) {
								player.sendMessage("Unable to parse color: " + signLine[0]);

								return;
							}
						}

						try {
							uses = Integer.parseInt(ChatColor.stripColor(signLine[1]));
						}
						catch (NumberFormatException e) {
							player.sendMessage("Unable to parse uses: " + signLine[1]);

							return;
						}

						try {
							price = Double.parseDouble(ChatColor.stripColor(lines[3]));
						}
						catch (NumberFormatException e) {
							player.sendMessage("Unable to parse price: " + lines[3]);

							return;
						}

						//Begin processing the sign key purchase
						if(DoorFiles.doorExists(world, location, color)) {
							CKKey newKey = new CKKey(world, location, color, uses, uses, price);

							if (KeyFiles.playerHasKey(player.getName(), newKey)) { //Check to see if the player already has that key in his inventory.
								CKKey playersKey = KeyFiles.getKey(player.getName(), world, location, color);

								if (playersKey.uses == 0 && !confirmPurchase.contains(player.getName())) {
									player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GRAY + "Are you sure you want to pay "  + ChatColor.GREEN + price + ChatColor.GRAY + " to repair your key?");
									player.sendMessage(ChatColor.GRAY + "Click again to confirm");

									confirmPurchase.add(player.getName());

									plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
										public void run() {
											if (confirmPurchase.contains(player.getName()))
												confirmPurchase.remove(player.getName());
										}
									}, 200L);

									return;
								}
								else if (playersKey.uses == -1 || playersKey.uses > 0) {
									player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GRAY + "You already have that key.");

									return;
								}
							}

							if (confirmPurchase.contains(player.getName())) {
								confirmPurchase.remove(player.getName());
							}
							else {
								player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GRAY + "Are you sure you want to pay "  + ChatColor.GREEN + price + ChatColor.GRAY + " for this key?");
								player.sendMessage(ChatColor.GRAY + "Click again to confirm");

								confirmPurchase.add(player.getName());

								plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
									public void run() {
										if (confirmPurchase.contains(player.getName()))
											confirmPurchase.remove(player.getName());
									}
								}, 200L);

								return;
							}

							if (ColorKeys.economyEnabled && ColorKeys.economy != null) {
								if(ColorKeys.economy.hasAccount(player.getName())) {
									Double balance = ColorKeys.economy.getBalance(player.getName());
									CKKey playersKey = KeyFiles.getKey(player.getName(), world, location, color);

									if (price > balance) {
										if (playersKey != null && playersKey.uses == 0)
											player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "You can't afford to repair that key.");
										else
											player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "You can't afford to buy that key.");
									}
									else {
										ColorKeys.economy.withdrawPlayer(player.getName(), price);

										if (playersKey != null && playersKey.uses == 0) {
											playersKey.uses = uses;
											KeyFiles.updateKey(player.getName(), playersKey);

											player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GREEN + "You repaired the " + CommandHandler.colorName(color) + ChatColor.GREEN + "key to " + ChatColor.AQUA + location + ChatColor.GREEN + "!");
											player.sendMessage(ChatColor.BLUE + "Price: " + ChatColor.GRAY + price + "    " + ChatColor.GREEN + "Balance: " + ChatColor.GRAY + ColorKeys.economy.getBalance(player.getName()));
										}
										else {
											KeyFiles.giveKey(player.getName(), newKey);

											player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GREEN + "You bought the " + CommandHandler.colorName(color) + ChatColor.GREEN + "key to " + ChatColor.AQUA + location + ChatColor.GREEN + "!");
											player.sendMessage(ChatColor.BLUE + "Price: " + ChatColor.GRAY + price + "    " + ChatColor.GREEN + "Balance: " + ChatColor.GRAY + ColorKeys.economy.getBalance(player.getName()));
										}
									}
								}
								else { //They don't have an account; I don't know what the ramification of this are.
									player.sendMessage("No Account?!?!?!?");
								}
							}
						}
						else{
							player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "That door no longer exists!");
						}
					}
					else{
						player.sendMessage("Line 3 has no ','!");

						return;
					}
				}
			}
		}
	}
}
