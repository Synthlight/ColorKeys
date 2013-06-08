package musaddict.colorkeys;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class CKKey {
	public World world;
	public String location;
	public int color, uses, initialUses;
	public double price;

	public CKKey(final String worldName, final String location, final int color, final int uses, final int initialUses) {
		this(worldName, location, color, uses, initialUses, -1);
	}

	public CKKey(final String worldName, final String location, final int color, final int uses, final int initialUses, final double price) {
		this.world = Bukkit.getWorld(worldName);
		this.location = location;
		this.color = color;
		this.uses = uses;
		this.initialUses = initialUses;
		this.price = price;
	}

	public String toString() {
		return world.getName() + ";" + location + ";" + color;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null)
			return false;

		if (obj == this)
			return true;

		if (obj.getClass() != getClass())
			return false;

		CKKey otherKey = (CKKey) obj;

		return new EqualsBuilder()
			.append(world, otherKey.world)
			.append(location, otherKey.location)
			.append(color, otherKey.color)
			.isEquals();
	}
}
