   package musaddict.colorkeys.commands;

import java.util.HashMap;

import musaddict.colorkeys.ColorKeys;
import musaddict.colorkeys.commands.AdminCommands;
import musaddict.colorkeys.commands.EcoCommands;
import musaddict.colorkeys.commands.InfoCommands;
import musaddict.colorkeys.commands.ListCommands;
import musaddict.colorkeys.commands.ModCommands;
import musaddict.colorkeys.files.DebugFiles;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor {
	public static HashMap<Player, Boolean> Selecting = new HashMap<Player, Boolean>();
	private ColorKeys plugin;

	public CommandHandler(ColorKeys plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] arg) {
		String s = "[" + sender.getName() + "] /" + command.getName();
		for (int i = 0; i < arg.length; i++) s += " " + arg[i];
		System.out.println(s);


		//if((arg.length == 1) && ColorKeys.CheckPermission(player, "ColorKeys.add")){

		if (arg.length > 0) {
			String Command = arg[0];

			if(Command.equalsIgnoreCase("select") || Command.equalsIgnoreCase("sel")) {
				return AdminCommands.select(sender, arg);
			}
			else if (Command.equalsIgnoreCase("create")) {
				return AdminCommands.create(sender, arg, plugin);
			}
			else if(Command.equalsIgnoreCase("queue") || Command.equalsIgnoreCase("q")) {
				return EcoCommands.queue(sender, arg);
			}
			else if(Command.equalsIgnoreCase("accept")) {
				return EcoCommands.accept(sender, arg);
			}
	 		else if(Command.equalsIgnoreCase("deny")) {
	 			return EcoCommands.deny(sender, arg);
			}
			else if(Command.equalsIgnoreCase("give")) {
				return ModCommands.give(sender, arg, plugin);
			}
			else if(Command.equalsIgnoreCase("cod")) {
				return EcoCommands.cod(sender, arg);
			}
			else if(Command.equalsIgnoreCase("repair")) {
				return ModCommands.repair(sender, arg, plugin);
			}
			else if(Command.equalsIgnoreCase("remove") || Command.equalsIgnoreCase("rm")) {
				return ModCommands.remove(sender, arg, plugin);
			}
			else if (Command.equalsIgnoreCase("help")) {
				return InfoCommands.help(sender, arg);
			}
			else if (Command.equalsIgnoreCase("version") || Command.equalsIgnoreCase("v")) {
				return InfoCommands.version(sender, arg);
			}
			else if (Command.equalsIgnoreCase("color") || Command.equalsIgnoreCase("colors")){
				return InfoCommands.color(sender, arg);
			}
			else if (Command.equalsIgnoreCase("list")) {
				return ListCommands.list(sender, arg);
			}
			else if (Command.equalsIgnoreCase("listall")) {
				return ListCommands.listAll(sender, arg);
			}
			else if (Command.equalsIgnoreCase("debug")) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "You cannot use that command from the console.");

					return true;
				}

				if (arg.length < 2) {
					sender.sendMessage("Not enough args.");
				}
				else {
					try {
						boolean debugState = Boolean.parseBoolean(arg[1]);

						if (arg[1].equals("1"))
							debugState = true;
						else if (arg[1].equals("0"))
							debugState = false;

						DebugFiles.setDebugging((Player) sender, debugState);

						if (debugState)
							sender.sendMessage("You are now debugging.");
						else
							sender.sendMessage("You are no longer debugging.");
					}
					catch (NumberFormatException e) {
						sender.sendMessage("Not a bool.");
					}
				}

				return true;
			}
		}
		else {
			sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "Unknown Command.");
			return true;
		}
		return false;
	}





	public static String colorName(int color) {
		switch (color) {
			case 0: return ChatColor.WHITE + "White ";//white
			case 1: return ChatColor.GOLD + "Orange ";//orange
			case 2: return ChatColor.DARK_PURPLE + "Magenta ";//magenta
			case 3: return ChatColor.DARK_AQUA + "Light Blue ";//lightblue
			case 4: return ChatColor.YELLOW + "Yellow ";//yellow
			case 5: return ChatColor.GREEN + "Lime ";//lime
			case 6: return ChatColor.LIGHT_PURPLE + "Pink ";//pink
			case 7: return ChatColor.DARK_GRAY + "Gray ";//grey
			case 8: return ChatColor.GRAY + "Light Grey ";//lightgrey
			case 9: return ChatColor.AQUA + "Cyan ";//cyan
			case 10: return ChatColor.DARK_PURPLE + "Purple ";//purple
			case 11: return ChatColor.DARK_BLUE + "Blue ";//blue
			case 12: return ChatColor.WHITE + "Brown ";//brown
			case 13: return ChatColor.DARK_GREEN + "Green ";//green
			case 14: return ChatColor.RED + "Red ";//red
			case 15: return ChatColor.BLACK + "Black ";//black
			default: return "Unknown Color";
		}
	}


	public static String signColor(int color) {
		switch (color) {
			case 0: return ChatColor.WHITE + "0" ;//white
			case 1: return ChatColor.GOLD + "1";//orange
			case 2: return ChatColor.DARK_PURPLE + "2";//magenta
			case 3: return ChatColor.DARK_AQUA + "3";//lightblue
			case 4: return ChatColor.YELLOW + "4";//yellow
			case 5: return ChatColor.GREEN + "5";//lime
			case 6: return ChatColor.LIGHT_PURPLE + "6";//pink
			case 7: return ChatColor.DARK_GRAY + "7";//grey
			case 8: return ChatColor.GRAY + "8";//lightgrey
			case 9: return ChatColor.AQUA + "9";//cyan
			case 10: return ChatColor.DARK_PURPLE + "10";//purple
			case 11: return ChatColor.DARK_BLUE + "11";//blue
			case 12: return ChatColor.WHITE + "12";//brown
			case 13: return ChatColor.DARK_GREEN + "13";//green
			case 14: return ChatColor.RED + "14";//red
			case 15: return ChatColor.BLACK + "15";//black
			default: return "Unknown Color";
		}
	}


	public static String noColorName(int color) {
		switch (color) {
			case 0: return "White";//white
			case 1: return "Orange";//orange
			case 2: return "Magenta";//magenta
			case 3: return "Light Blue";//lightblue
			case 4: return "Yellow";//yellow
			case 5: return "Lime";//lime
			case 6: return "Pink";//pink
			case 7: return "Gray";//grey
			case 8: return "Light Grey";//lightgrey
			case 9: return "Cyan";//cyan
			case 10: return "Purple";//purple
			case 11: return "Blue";//blue
			case 12: return "Brown";//brown
			case 13: return "Green";//green
			case 14: return "Red";//red
			case 15: return "Black";//black
			default: return "Unknown Color";
		}
	}


	public static int stringToColor(String color) {
		if (color.equalsIgnoreCase("White")) return 0;
		else if (color.equalsIgnoreCase("Orange")) return 1;
		else if (color.equalsIgnoreCase("Magenta")) return 2;
		else if (color.equalsIgnoreCase("Light Blue")) return 3;
		else if (color.equalsIgnoreCase("Yellow")) return 4;
		else if (color.equalsIgnoreCase("Lime")) return 5;
		else if (color.equalsIgnoreCase("Pink")) return 6;
		else if (color.equalsIgnoreCase("Gray")) return 7;
		else if (color.equalsIgnoreCase("Light Grey")) return 8;
		else if (color.equalsIgnoreCase("Cyan")) return 9;
		else if (color.equalsIgnoreCase("Purple")) return 10;
		else if (color.equalsIgnoreCase("Blue")) return 11;
		else if (color.equalsIgnoreCase("Brown")) return 12;
		else if (color.equalsIgnoreCase("Green")) return 13;
		else if (color.equalsIgnoreCase("Red")) return 14;
		else if (color.equalsIgnoreCase("Black")) return 15;
		else return -1;
	}
}
