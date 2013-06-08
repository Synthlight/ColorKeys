package musaddict.colorkeys;

import java.util.logging.Level;

import musaddict.colorkeys.files.DebugFiles;
import musaddict.colorkeys.files.DoorFiles;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.Door;

public class PartialCKDoor {
	public boolean isValid;
	public boolean isDouble;
	public Block woolBaseBlock;
	public Block otherWoolBaseBlock;

	private static BlockFace getOppositeDirection(final String dir) {
		if (dir.equalsIgnoreCase("north")) return BlockFace.SOUTH;
		else if (dir.equalsIgnoreCase("south")) return BlockFace.NORTH;
		else if (dir.equalsIgnoreCase("east")) return BlockFace.WEST;
		else if (dir.equalsIgnoreCase("west")) return BlockFace.EAST;
		else {
			ColorKeys.Log(Level.SEVERE, "Impossible direction found: " + dir);

			return null;
		}
	}

	public boolean isBlockPartOfCKDoor() {
		if (isValid)
			return DoorFiles.getDoor(woolBaseBlock) != null;
		else
			return false;
	}

	public CKDoor toDoor() {
		if (isValid)
			return DoorFiles.getDoor(woolBaseBlock);
		else
			return null;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null)
			return false;

		if (obj == this)
			return true;

		if (obj.getClass() != getClass())
			return false;

		PartialCKDoor otherPCKDoor = (PartialCKDoor) obj;

		if (new EqualsBuilder()
		.append(isValid, otherPCKDoor.isValid)
		.append(isDouble, otherPCKDoor.isDouble)
		.isEquals()) {
			if (!isValid) //At this point both isValid and isDouble (in either instance) match their respective selves.
				return false;

			if (isDouble) { //The mumbo-jumbo here is cause both wool blocks could be flip-flopped in the different instances AND either wool block (in either instance) could be null.
				return ((woolBaseBlock.equals(otherPCKDoor.woolBaseBlock) || woolBaseBlock.equals(otherPCKDoor.otherWoolBaseBlock))
				&& (otherWoolBaseBlock.equals(otherPCKDoor.otherWoolBaseBlock) || otherWoolBaseBlock.equals(otherPCKDoor.woolBaseBlock)));
			}
			else {
				return (woolBaseBlock.equals(otherPCKDoor.woolBaseBlock));
			}
		}

		return false;
	}

	public PartialCKDoor(final Block block) { //searchForDoorFromBlock()
		this(null, block, true);
	}

	public PartialCKDoor(final Player player, final Block block) { //searchForDoorFromBlock()
		this(player, block, false);
	}

	public PartialCKDoor(final Player player, final Block block, final boolean ignorePlayer) { //searchForDoorFromBlock()
		isValid = false;
		Block woolBaseBlock = null;
		Block doorBaseBlock = null;
		Block doorTopBlock = null;
		Door doorBase = null;
		Door doorTop = null;
		BlockFace directionOfTravelFromWoolToDoorWoolBlock = null;

		if (block.getType() == Material.WOOL) {
			if (block.getRelative(BlockFace.UP).getState().getData() instanceof Door) {
				woolBaseBlock = block;

				doorBaseBlock = block.getRelative(BlockFace.UP);
			}
			else {
				for (BlockFace dir : new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
					if (block.getRelative(dir).getRelative(BlockFace.UP).getState().getData() instanceof Door) {
						directionOfTravelFromWoolToDoorWoolBlock = dir;

						woolBaseBlock = block.getRelative(dir);

						doorBaseBlock = block.getRelative(dir).getRelative(BlockFace.UP);

						break;
					}
				}
			}
		}
		else if (block.getState().getData() instanceof Door) {
			doorBaseBlock = block;
		}

		if (doorBaseBlock != null) {
			if (doorBaseBlock.getState().getData() instanceof Door) {
				doorBase = (Door) (doorBaseBlock.getState().getData());

				try {
					if (doorBase.isTopHalf()) {
						doorTopBlock = doorBaseBlock;
						doorTop = (Door) (doorTopBlock.getState().getData());
						doorBaseBlock = doorBaseBlock.getRelative(BlockFace.DOWN);
						doorBase = (Door) (doorBaseBlock.getState().getData());
					}
					else {
						doorTopBlock = doorBaseBlock.getRelative(BlockFace.UP);
						doorTop = (Door) (doorTopBlock.getState().getData());
					}
				}
				catch (ClassCastException e) {
					if (player != null && DebugFiles.isDebugging(player))
						player.sendMessage("ClassCastException");

					return;
				}

				if (woolBaseBlock == null) {
					if (doorBaseBlock.getRelative(BlockFace.DOWN).getType() == Material.WOOL) {
						woolBaseBlock = doorBaseBlock.getRelative(BlockFace.DOWN);
					}
				}
			}
			else {
				doorBaseBlock = null;
			}
		}

		if (woolBaseBlock == null) {
			if(player != null)
				player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "There is no wool block in the selection.");

			return;
		}
		else if (doorBaseBlock == null) {
			if(player != null)
				player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "There is no door block in the selection.");

			return;
		}

		BlockFace facingDir = doorBase.getFacing();
		BlockFace hingeCorner = doorBase.getHingeCorner();

		if (directionOfTravelFromWoolToDoorWoolBlock != null && directionOfTravelFromWoolToDoorWoolBlock.getOppositeFace() != facingDir) {
			if(player != null)
				player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "The front wool block is not infront of the door.");

			return;
		}

		if (woolBaseBlock.getRelative(facingDir).getType() == Material.WOOL) {
			Block frontWoolBaseBlock = woolBaseBlock.getRelative(facingDir);

			if (frontWoolBaseBlock.getData() != woolBaseBlock.getData()) {
				if(player != null)
					player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "The front wool block is the wrong color.");

				return;
			}
		}
		else {
			if(player != null)
				player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "The front block is not wool.");

			return;
		}

		//At this point we have a valid single door, the next section saves the single door info and begins the search for a double door.
		isValid = true;
		this.woolBaseBlock = woolBaseBlock;





		//To top block of a door is either 1000 (8) for a regular door OR 1001 (9) for a flipped door
		int doorTopData = (int) doorTop.getData();
		int doorBaseData = (int) doorBase.getData();
		boolean isFlippedDoor = doorTopData == 9 || doorTopData == 13? true : false;
		BlockFace possibleDoubleDir;

		if (isFlippedDoor)
			possibleDoubleDir = BlockFace.valueOf(hingeCorner.toString().replace(facingDir.toString(), "").replace("_", ""));
		else
			possibleDoubleDir = getOppositeDirection(hingeCorner.toString().replace(facingDir.toString(), "").replace("_", ""));

		if (player != null && DebugFiles.isDebugging(player)) {
			player.sendMessage("facing: " + doorBase.getFacing().toString());
			player.sendMessage("doorTopData: " + doorTopData + ", " + Integer.toBinaryString(doorTopData));
			player.sendMessage("doorBaseData: " + doorBaseData + ", " + Integer.toBinaryString(doorBaseData));
			player.sendMessage("isFlippedDoor: " + isFlippedDoor + ", possibleDoubleDir: " + possibleDoubleDir.toString());
			//player.sendMessage("Double door dir material: " + doorBaseBlock.getRelative(possibleDoubleDir).getType().toString());
		}

		if (doorBaseBlock.getRelative(possibleDoubleDir).getState().getData() instanceof Door) {
			if (player != null && DebugFiles.isDebugging(player))
				player.sendMessage("Double door found.");

			isValid = false; //We found a paired door so we must mark it invalid till we verrify that the paired door is correct.

			Block otherDoorBaseBlock = doorBaseBlock.getRelative(possibleDoubleDir);
			Door otherDoorTop = (Door) (otherDoorBaseBlock.getRelative(BlockFace.UP).getState().getData());
			Door otherDoorBase = (Door) (otherDoorBaseBlock.getState().getData());
			int otherDoorTopData = (int) otherDoorTop.getData();
			boolean isOtherDoorFlipped = otherDoorTopData == 9 || otherDoorTopData == 13? true : false;
			BlockFace otherFacingDir = otherDoorBase.getFacing();
			Block otherWoolBaseBlock = otherDoorBaseBlock.getRelative(BlockFace.DOWN);
			Block otherFrontWoolBaseBlock = otherWoolBaseBlock.getRelative(facingDir);

			//Makes sure that the flipped door points in the correct direction and is not part of some other pair.
			if (isFlippedDoor ^ isOtherDoorFlipped && facingDir == otherFacingDir) { //If one is true and the other false then it's a double door.  (^ is XOR)
				if (player != null && DebugFiles.isDebugging(player))
					player.sendMessage("Double door valid.");

				if (otherWoolBaseBlock.getType() == Material.WOOL) {
					if (otherWoolBaseBlock.getData() != woolBaseBlock.getData()) {
						if(player != null)
							player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "The wool block for the double door is the wrong color.");

						return;
					}
					else {
						if (otherFrontWoolBaseBlock.getType() == Material.WOOL) {
							if (otherFrontWoolBaseBlock.getData() != woolBaseBlock.getData()) {
								if(player != null)
									player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "The front wool block beneath the double door is the wrong color.");

								return;
							}
						}
						else {
							if(player != null)
								player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "The front block beneath the double door is not wool.");

							return;
						}
					}
				}
				else {
					if(player != null)
						player.sendMessage(ChatColor.GOLD + "[CK] " + ChatColor.RED + "The block beneath the double door is not wool.");

					return;
				}

				//other door valid && wool is fine
				isValid = true;
				isDouble = true;
				this.otherWoolBaseBlock = otherWoolBaseBlock;
			}
			else {
				if (player != null && DebugFiles.isDebugging(player))
					player.sendMessage("Double door invalid.");
			}
		}
	}
}
