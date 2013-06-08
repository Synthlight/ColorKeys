package musaddict.colorkeys;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import musaddict.colorkeys.commands.CommandHandler;
import musaddict.colorkeys.files.DebugFiles;
import musaddict.colorkeys.files.DoorFiles;
import musaddict.colorkeys.files.KeyFiles;
import musaddict.colorkeys.files.QueueFiles;
import musaddict.colorkeys.listeners.BlockListener;
import musaddict.colorkeys.listeners.PlayerListener;
import musaddict.colorkeys.listeners.SignShops;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/*
ColorKeys TODOs

Changelog:

Added: Full support for double doors. (Rewrote BBE and PIE to be more efficient and to add support for double doors.)
Changed: To remove a CK Door destroy either the wool block beneath the door or the door's front wool block. (Not the door itself)
Changed: Improved detection of double/single doors and their respective wool blocks. As a result detection now encompasses and protects the lower wool block, the lower front wool block and the door itself.
Added: '/ck sel' now shorthand/alias for '/ck select'.
Added: When selecting, if the selected door is an existing  CK Door the location and color will be displayed.
Added: New door selection method: To select a door type '/ck select', then just right click a door/wool. (No hoe needed) Selection will prevent any interaction with wool/doors while active but nothing else.
Added: Configuration option (end-selection-after-action) to disable selection after running a door command. Otherwise the selection won't be cleared so other commands can still be run on the same selection.
Changed: Door save info (Doors.xml) has been upgraded to v1.1 to include the necessary info for double doors. This upgrade should be fully automatic but backup your save data just incase.
Changed: '/ck give' will now try to use the door selection ('/ck select') if location and color are omitted. This requires that the selection be an existing CKDoor and not one yet to be created.
Changed: '/ck repair' will now try to use the door selection. (See '/ck give' for more info.)
Changed: '/ck remove' will now try to use the door selection. (See '/ck give' for more info.)
Fixed: Bug in '/ck list <loc>' which would cause it to say a valid location dosen't exist.
Fixed: Other minor typos and bugfixes.


Future stuff:
TODO: Keys expire from time. (as opposed to uses)
TODO: Tutorial system.



Bookshelf stuff
TODO: New feature: Use '/ck place' to hide keys in bookshelves.
TODO: New save system for bookKeys.
TODO: Add '/ck place'                The command will require the user to right-click on the bookshelf after running it to select a proper block.
TODO: Add a 10sec timeout (config option) for '/ck place'.
TODO: bookKeys will be removed when the block is destroyed.

-/ck place (location) (color) (-u=) (-w=)
-prompts you to right click a book shelf
-RC shelf will save the given door info to that block
-players can RC to get the key
-if they already have the key, nothing is said in chat
 */

public class ColorKeys extends JavaPlugin implements Listener {
	public static PluginDescriptionFile info;
	private static Logger logger = Logger.getLogger("Minecraft");
	public static final String mainDirectory = "plugins/ColorKeys";
	public static final String versionURL = "http://root.brutsches.com/version.txt";

	private static HashMap<Player, Boolean> playerSelectionMode = new HashMap<Player, Boolean>();
	public static HashMap<Player, PartialCKDoor> playerDoorSelection = new HashMap<Player, PartialCKDoor>();

	public static boolean economyEnabled = false;
	public static Economy economy = null;

	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() {
		info = getDescription();
		logger = Logger.getLogger("Minecraft");

		getConfig().options().copyDefaults(true);
		saveConfig();

		new File(mainDirectory).mkdir(); //makes the ColorKeys directory/folder in the plugins directory if it dosen't exist.

		DebugFiles.load();

		if(ColorKeysFiles.DoorsFile.exists())
			ColorKeysFiles.loadDoors();
		else
			DoorFiles.load();

		if(ColorKeysFiles.KeysFile.exists())
			ColorKeysFiles.loadKeys();
		else
			KeyFiles.load();

		if (getConfig().getBoolean("enable-economy")) {
			economyEnabled = true;

			try {
				RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);

				if (economyProvider != null) {
					economy = economyProvider.getProvider();
					Log(Level.INFO, "hooked into economy.");
				}
			}
			catch (NoClassDefFoundError e) {
				Log(Level.WARNING, "Vault plugin could not be found.");
			}
		}

		QueueFiles.load();

		getServer().getPluginManager().registerEvents(new BlockListener(this), this);
		getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		getServer().getPluginManager().registerEvents(new SignShops(this), this);
		getServer().getPluginManager().registerEvents(this, this);

		getCommand("ck").setExecutor(new CommandHandler(this));

		Log(Level.INFO, "is enabled, version: " + info.getVersion());
		Log(Level.INFO, "written by [Musaddict, Dak393, FeedDante]");
	}

	@Override
	public void onDisable() {
		DebugFiles.save();
		DoorFiles.save();
		KeyFiles.save();
		QueueFiles.save();

		Log(Level.INFO, "was successfully disabled.");
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPluginDisable(PluginDisableEvent event) {
		if (economy != null) {
			RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);

			if (economyProvider == null) {
				economy = null;
				Log(Level.INFO, "un-hooked from economy.");
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPluginEnable(PluginEnableEvent event) {
		if (getConfig().getBoolean("enable-economy") && economy == null) {
			try {
				RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);

				if (economyProvider != null) {
					economy = economyProvider.getProvider();
					Log(Level.INFO, "hooked into economy.");
				}
			}
			catch (NoClassDefFoundError e) {
			}
		}
	}

	public static void Log(final String message) {
		Log(Level.INFO, message);
	}

	public static void Log(final Level logLevel, final String message) {
		logger.log(logLevel, "[" + info.getName() + "] " + message);
	}

	public static boolean isSelecting(final Player player) {
		if (playerSelectionMode.containsKey(player))
			return playerSelectionMode.get(player);
		else
			return false;
	}

	public static void setSelecting(final Player player, final boolean value) {
		playerSelectionMode.put(player, value);

		if (!value && playerDoorSelection.containsKey(player))
			playerDoorSelection.remove(player);
	}
}
