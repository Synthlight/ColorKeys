package musaddict.colorkeys.listeners;

import java.util.HashMap;

import musaddict.colorkeys.CKDoor;
import musaddict.colorkeys.CKKey;
import musaddict.colorkeys.ColorKeys;
import musaddict.colorkeys.PartialCKDoor;
import musaddict.colorkeys.files.DebugFiles;
import musaddict.colorkeys.files.DoorFiles;
import musaddict.colorkeys.files.KeyFiles;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.material.Door;

public class BlockListener implements Listener {
	public static ColorKeys plugin;
	private static final long doorDeletionConfirmationDelay = 300L; //Amount of time to wait before closing the door after it was opened.

	public BlockListener(ColorKeys instance) {
		plugin = instance;
	}

	public static HashMap<Player, PartialCKDoor> playerDeletionConfirmation = new HashMap<Player, PartialCKDoor>();
	public static HashMap<Player, Integer> playerDeletionConfirmationTaskID = new HashMap<Player, Integer>();

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent event) {
		final Player player = event.getPlayer();
		final Block block = event.getBlock();

		if (block == null)
			return;

		final PartialCKDoor destroyedBlockPartialDoorMatch = new PartialCKDoor(block);

		if (destroyedBlockPartialDoorMatch.isBlockPartOfCKDoor()) {
			if (DebugFiles.isDebugging(player))
				player.sendMessage("Destroyed block is part of a CK door.");

			if (player.isOp() || player.hasPermission("colorkeys.remove") || player.hasPermission("colorkeys.admin")) {
				if (player.getGameMode() == GameMode.CREATIVE) {
					if (DebugFiles.isDebugging(player))
						player.sendMessage("in creative");

					//player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "Doors can only be destroyed in" + ChatColor.GRAY + " SURVIVAL" + ChatColor.RED + " mode.");

					if (block.getState().getData() instanceof Door) {
						event.setCancelled(true);

						return;
					}
				}
			}
			else {
				player.sendMessage(ChatColor.DARK_RED + "You do not have permission to remove ColorKey doors.");

				if (playerDeletionConfirmation.containsKey(player))
					playerDeletionConfirmation.remove(player);

				event.setCancelled(true);

				return;
			}

			if (playerDeletionConfirmation.containsKey(player)) { //They're in the map, begin deletion checks.
				PartialCKDoor savedPartialDoorMatch = playerDeletionConfirmation.get(player);

				if (destroyedBlockPartialDoorMatch.equals(savedPartialDoorMatch)) { //Make sure the destroyed block is part of the same door that was saved.
					final CKDoor door = destroyedBlockPartialDoorMatch.toDoor();

					if(door != null) { //Return null if key was not found, or the removed block otherwise.
						if (DoorFiles.removeDoor(door)) { //Door verified, remove from the HashMaps
							KeyFiles.removeKey(new CKKey(door.world.getName(), door.location, door.color, -1, -1)); //Remove all keys for the given door

							player.sendMessage(ChatColor.RED + "Door " + ChatColor.AQUA + door.location + ChatColor.RED + " removed."); //Calling getDoorName(Selected) returns null if the door has been successfully removed.
						}
						else {
							player.sendMessage("Unknown problem removing door: " + door.toString());
						}
					}
					else {
						player.sendMessage("No entry in map for that block.");
					}
				}
				else{
					player.sendMessage(ChatColor.DARK_RED + "*Not the same CK door*");

					if (playerDeletionConfirmationTaskID.containsKey(player))
						plugin.getServer().getScheduler().cancelTask(playerDeletionConfirmationTaskID.get(player));
				}

				if (playerDeletionConfirmation.containsKey(player))
					playerDeletionConfirmation.remove(player);

				event.setCancelled(true);
			}
			else { //Delete confirmation required.
				player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "Do you wish to permenantly remove this door?");
				player.sendMessage(ChatColor.GRAY + "Destroy door again to confirm.");

				playerDeletionConfirmation.put(player, destroyedBlockPartialDoorMatch);

				playerDeletionConfirmationTaskID.put(player, plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						if (playerDeletionConfirmation.containsKey(player)) {
							playerDeletionConfirmation.remove(player);

							player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.GRAY + "Door deletion aborted.");
						}
					}
				}, doorDeletionConfirmationDelay));

				event.setCancelled(true);
			}
		}
	}



	//TODO: If you place a door check and convert a single CK door to a double-door.
	//Problem: This is not really do-able as the top half of a placed door isn't quite an instance of a door yet so I can't get the necessary info to see if this door is part of a pair.
	/*@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPlace(BlockPlaceEvent event) {
		Block block = event.getBlockPlaced();

		if (block.getState().getData() instanceof Door) {
			Player player = event.getPlayer();
			PartialCKDoor ckDoor = new PartialCKDoor(player, block);

			if (DebugFiles.isDebugging(player))
				player.sendMessage("BPE Door Valid: " + ckDoor.isValid);

			event.setCancelled(true);
		}
	}*/



	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockDamage(BlockDamageEvent event) { //Make sure the event fires if it's a CK door so that it can be broken/destroyed.
		if (!event.isCancelled())
			return;

		Player player = event.getPlayer();

		if (new PartialCKDoor(player, event.getBlock(), true).isBlockPartOfCKDoor()) {
			if (DebugFiles.isDebugging(player))
				player.sendMessage("onBlockDamage event for a CKDoor; attempting to enable");

			event.setCancelled(false);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityExplode(EntityExplodeEvent event) {
		if (event.isCancelled())
			return;

		for (Block block : event.blockList())
			if (new PartialCKDoor(block).isBlockPartOfCKDoor()) { //Make sure the door is one that needs to be protected
				if (DebugFiles.isDebugging())
					ColorKeys.Log("EntityExplodeEvent protection");

				event.setCancelled(true);
				return;
			}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
		if (event.isCancelled())
			return;

		for (Block block : event.getBlocks())
			if (new PartialCKDoor(block).isBlockPartOfCKDoor()) { //Make sure the door is one that needs to be protected
				if (DebugFiles.isDebugging())
					ColorKeys.Log("BlockPistonExtendEvent protection");

				event.setCancelled(true);
				return;
			}

		for (int i = 1; i < event.getLength() + 2; i++) //If 2 fails go back to 3 (needed to check the 'blank' space pistons extend into)
			if (new PartialCKDoor(event.getBlock().getRelative(event.getDirection(), i)).isBlockPartOfCKDoor()) { //Make sure the door is one that needs to be protected
				if (DebugFiles.isDebugging())
					ColorKeys.Log("BlockPistonExtendEvent protection");

				event.setCancelled(true);
				return;
			}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockPistonRetract(BlockPistonRetractEvent event) {
		if (event.isCancelled())
			return;

		if (event.isSticky())
			if (new PartialCKDoor(event.getRetractLocation().getBlock()).isBlockPartOfCKDoor()) { //Make sure the door is one that needs to be protected
				if (DebugFiles.isDebugging())
					ColorKeys.Log("BlockPistonRetractEvent protection");

				event.setCancelled(true);
			}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBurn(BlockBurnEvent event) {
		if (event.isCancelled())
			return;

		if (new PartialCKDoor(event.getBlock()).isBlockPartOfCKDoor()) {
			if (DebugFiles.isDebugging())
				ColorKeys.Log("BlockBurnEvent protection");

			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockIgnite(BlockIgniteEvent event) {
		if (event.isCancelled())
			return;

		if (new PartialCKDoor(event.getBlock()).isBlockPartOfCKDoor()) {
			if (DebugFiles.isDebugging())
				ColorKeys.Log("BlockIgniteEvent protection");

			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		if (event.isCancelled())
			return;

		if (new PartialCKDoor(event.getBlock()).isBlockPartOfCKDoor()) {
			if (DebugFiles.isDebugging())
				ColorKeys.Log("EntityChangeBlockEvent protection");

			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockPhysics(BlockPhysicsEvent event) {
		if (event.isCancelled())
			return;

		if (new PartialCKDoor(event.getBlock()).isBlockPartOfCKDoor()) {
			if (DebugFiles.isDebugging())
				ColorKeys.Log("BlockPhysicsEvent protection");

			event.setCancelled(true);
		}
	}
}
