package musaddict.colorkeys;

import musaddict.colorkeys.files.DebugFiles;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.material.Door;

public class CKDoor {
	public World world;
	public String location;
	public int color;
	public boolean isDouble;
	public int x, y, z;
	public int otherX, otherY, otherZ;

	public CKDoor(final String worldName, final String location, final int color) {
		this(worldName, location, color, 0, 0, 0);
	}

	public CKDoor(final String worldName, final String location, final int color, final int x, final int y, final int z, final int otherX, final int otherY, final int otherZ) {
		this(worldName, location, color, x, y, z, true, otherX, otherY, otherZ);
	}

	public CKDoor(final String worldName, final String location, final int color, final int x, final int y, final int z, final boolean isDouble, final int otherX, final int otherY, final int otherZ) {
		this(worldName, location, color, x, y, z);

		this.isDouble = isDouble;
		this.otherX = otherX;
		this.otherY = otherY;
		this.otherZ = otherZ;
	}

	public CKDoor(final String worldName, final String location, final int color, final Block block) {
		this(worldName, location, color, block.getX(), block.getY(), block.getZ());
	}

	public CKDoor(final String worldName, final String location, final int color, final Block block, final Block block2) {
		this(worldName, location, color, block.getX(), block.getY(), block.getZ(), block2.getX(), block2.getY(), block2.getZ());
	}

	public CKDoor(final String worldName, final String location, final int color, final int x, final int y, final int z) {
		this.world = Bukkit.getWorld(worldName);
		this.location = location;
		this.color = color;
		this.x = x;
		this.y = y;
		this.z = z;

		this.isDouble = false;
		otherX = otherY = otherZ = 0;
	}

	public String toString() {
		return world.getName() + ";" + location + ";" + color;
	}

	public Block[] getBlocks()
	{
		if (isDouble)
			return new Block[] {world.getBlockAt(x, y, z), world.getBlockAt(otherX, otherY, otherZ)};
		else
			return new Block[] {world.getBlockAt(x, y, z)};
	}

	public boolean isOpen() {
		BlockState state1 = world.getBlockAt(x, y, z).getRelative(BlockFace.UP).getState();
		Door door1 = (Door) state1.getData();

		if (DebugFiles.isDebugging())
			ColorKeys.Log(toString() + " --- isOpen: " + door1.isOpen());

		if (isDouble) {
			BlockState state2 = world.getBlockAt(otherX, otherY, otherZ).getRelative(BlockFace.UP).getState();

			Door door2 = (Door) state2.getData();

			if (DebugFiles.isDebugging())
				ColorKeys.Log(toString() + " --- isOpen2: " + door2.isOpen());

			return (door1.isOpen() || door2.isOpen());
		}
		else
			return door1.isOpen();
	}

	private void setOpen(final boolean openState) {
		if (DebugFiles.isDebugging())
			ColorKeys.Log(toString() + " --- setOpen: " + openState);

		BlockState state1 = world.getBlockAt(x, y, z).getRelative(BlockFace.UP).getState();
		BlockState state1_Top = world.getBlockAt(x, y, z).getRelative(BlockFace.UP, 2).getState();

		Door door1 = (Door) state1.getData();
		Door door1_Top = (Door) state1_Top.getData();

		if (openState != door1.isOpen()) {
			door1.setOpen(openState);
			door1_Top.setOpen(openState);

			state1.update();
			state1_Top.update();

			world.playEffect(new Location(world, x, y + 1, z), Effect.DOOR_TOGGLE, 0);
		}

		if (isDouble) {
			BlockState state2 = world.getBlockAt(otherX, otherY, otherZ).getRelative(BlockFace.UP).getState();
			BlockState state2_Top = world.getBlockAt(otherX, otherY, otherZ).getRelative(BlockFace.UP, 2).getState();

			Door door2 = (Door) state2.getData();
			Door door2_Top = (Door) state2_Top.getData();

			if (openState != door2.isOpen()) {
				door2.setOpen(openState);
				door2_Top.setOpen(openState);

				state2.update();
				state2_Top.update();

				world.playEffect(new Location(world, otherX, otherY + 1, otherZ), Effect.DOOR_TOGGLE, 0);
			}
		}
	}

	public void open() {
		setOpen(true);
	}

	public void close() {
		setOpen(false);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null)
			return false;

		if (obj == this)
			return true;

		if (obj.getClass() != getClass())
			return false;

		CKDoor otherDoor = (CKDoor) obj;

		return new EqualsBuilder()
			.append(world, otherDoor.world)
			.append(location, otherDoor.location)
			.append(color, otherDoor.color)
			.isEquals();
	}
}
