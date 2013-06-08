package musaddict.colorkeys.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import musaddict.colorkeys.ColorKeys;
import musaddict.colorkeys.files.DebugFiles;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class InfoCommands {
	public static boolean help(CommandSender sender, String[] arg) { //TODO: Only display commands the player has permissions to use.
		if(arg.length == 2){
			if(arg[1].equals("1")){
				sender.sendMessage(ChatColor.GOLD + "ColorKeys " + ChatColor.GRAY + ColorKeys.info.getVersion() + ChatColor.GOLD + " Page 1");
				sender.sendMessage(ChatColor.GOLD + "____________________________________");
				sender.sendMessage(ChatColor.GOLD + "/ck help (page)" + ChatColor.GRAY + " - What you just typed");
				sender.sendMessage(ChatColor.GOLD + "/ck select" + ChatColor.GRAY + " - Enables/disables door selection");
				sender.sendMessage(ChatColor.GOLD + "/ck create [loc]" + ChatColor.GRAY + " - Saves that color door for a given location");
				sender.sendMessage(ChatColor.GOLD + "/ck give [player] [loc] [color] (-u=[uses]) (-w=[world])" + ChatColor.GRAY + " - Give keys");
				sender.sendMessage(ChatColor.GOLD + "/ck repair" + ChatColor.GRAY + " - Set up like 'Give', repairs uses of a key");
				sender.sendMessage(ChatColor.GOLD + "/ck list (loc)" + ChatColor.GRAY + " - Lists locations, and keys to a location");
				sender.sendMessage(ChatColor.GOLD + "/ck listall (loc)" + ChatColor.GRAY + " - Lists all locations, and keys to a location");
				sender.sendMessage(ChatColor.GOLD + "/ck color" + ChatColor.GRAY + "(" + ChatColor.GOLD + "s" + ChatColor.GRAY + ") - Shows a list of CK color codes");
			}
			else if(arg[1].equals("2")){
				sender.sendMessage(ChatColor.GOLD + "ColorKeys " + ChatColor.GRAY + ColorKeys.info.getVersion() + ChatColor.GOLD + " Page 2");
				sender.sendMessage(ChatColor.GOLD + "____________________________________");
				sender.sendMessage(ChatColor.GOLD + "/ck queue" + ChatColor.GRAY + " - Displays all keys available for you to buy");
				sender.sendMessage(ChatColor.GOLD + "/ck accept [ID]" + ChatColor.GRAY + " - Purchases the key labed with the ID number");
				sender.sendMessage(ChatColor.GOLD + "/ck deny [ID]" + ChatColor.GRAY + " - Denies the key labeled with the ID number");
				sender.sendMessage(ChatColor.GOLD + "/ck cod [player] [loc] [color] (-u=[uses]) (-w=[world])");
				sender.sendMessage(ChatColor.GOLD + "/ck cod" + ChatColor.GRAY + " (continued) - puts the key in selected players queue.");
				sender.sendMessage(ChatColor.GOLD + "/ck remove [player] [loc] [color] (-w=[world])" + ChatColor.GRAY + " - Remove keys");
				sender.sendMessage("");
				sender.sendMessage("");
			}
			else{
				sender.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "That page does not exist.");
			}
		}
		else{

		sender.sendMessage(ChatColor.GOLD + "ColorKeys " + ChatColor.GRAY + ColorKeys.info.getVersion() + ChatColor.GOLD + "Page 1");
		sender.sendMessage(ChatColor.GOLD + "____________________________________");
		sender.sendMessage(ChatColor.GOLD + "/ck help" + ChatColor.GRAY + " - What you just typed");
		sender.sendMessage(ChatColor.GOLD + "/ck select" + ChatColor.GRAY + " - Enables/disables door selection");
		sender.sendMessage(ChatColor.GOLD + "/ck create [loc]" + ChatColor.GRAY + " - Saves that color door for a given location");
		sender.sendMessage(ChatColor.GOLD + "/ck give [player] [loc] [color] (-u=[uses]) (-w=[world])" + ChatColor.GRAY + " - Give keys");
		sender.sendMessage(ChatColor.GOLD + "/ck repair" + ChatColor.GRAY + " - Set up like 'Give', repairs uses of a key");
		sender.sendMessage(ChatColor.GOLD + "/ck list (loc)" + ChatColor.GRAY + " - Lists locations, and keys to a location");
		sender.sendMessage(ChatColor.GOLD + "/ck listall (loc)" + ChatColor.GRAY + " - Lists all locations, and keys to a location");
		sender.sendMessage(ChatColor.GOLD + "/ck color" + ChatColor.GRAY + "(" + ChatColor.GOLD + "s" + ChatColor.GRAY + ") - Shows a list of CK color codes");
		}
		return true;
	}





	public static boolean version(CommandSender sender, String[] arg) {
		sender.sendMessage(ChatColor.GRAY + "You are running " + ChatColor.GOLD + "ColorKeys " + ChatColor.GRAY + ColorKeys.info.getVersion());

		try {
			URL version = new URL(ColorKeys.versionURL);
			URLConnection dc = version.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(dc.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				if(inputLine.equals(ColorKeys.info.getVersion())){
					sender.sendMessage(ChatColor.GRAY + "ColorKeys is up to date!");
				}
				else{
					sender.sendMessage(ChatColor.GRAY + "ColorKeys is Out of date! The latest version is:");
					sender.sendMessage(ChatColor.GOLD + inputLine);
					sender.sendMessage(ChatColor.GRAY + "You can download the latest version of CK here:");
					sender.sendMessage("http://dev.bukkit.org/server-mods/colorkeys/files/");
				}
			}
			in.close();
		}
		catch (IOException e) {
			if (DebugFiles.isDebugging())
				e.printStackTrace();
		}

		return true;
	}





	public static boolean color(CommandSender sender, String[] arg) {
		sender.sendMessage(ChatColor.GOLD + "ColorKeys Door Colors");
		sender.sendMessage(CommandHandler.colorName(0) + ChatColor.GRAY + "0, " + CommandHandler.colorName(1) + ChatColor.GRAY + "1, " + CommandHandler.colorName(2) + ChatColor.GRAY + "2, " + CommandHandler.colorName(3) + ChatColor.GRAY + "3, " + CommandHandler.colorName(4) + ChatColor.GRAY + "4, " + CommandHandler.colorName(5) + ChatColor.GRAY + "5,");
		sender.sendMessage(CommandHandler.colorName(6) + ChatColor.GRAY + "6, " + CommandHandler.colorName(7) + ChatColor.GRAY + "7, " + CommandHandler.colorName(8) + ChatColor.GRAY + "8, " + CommandHandler.colorName(9) + ChatColor.GRAY + "9, " + CommandHandler.colorName(10) + ChatColor.GRAY + "10, " + CommandHandler.colorName(11) + ChatColor.GRAY + "11,");
		sender.sendMessage(CommandHandler.colorName(12) + ChatColor.GRAY + "12, " + CommandHandler.colorName(13) + ChatColor.GRAY + "13, " + CommandHandler.colorName(14) + ChatColor.GRAY + "14, " + CommandHandler.colorName(15) + ChatColor.GRAY + "15");
		return true;
	}
}
