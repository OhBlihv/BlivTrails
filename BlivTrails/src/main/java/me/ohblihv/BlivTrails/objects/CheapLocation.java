package me.ohblihv.BlivTrails.objects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.World;

/**
 * Created by Chris Brown (OhBlihv) on 19/05/2016.
 */
@RequiredArgsConstructor
public class CheapLocation
{

	@Getter
	private final String worldName;

	private World world = null;

	@Getter
	private final World.Environment environment;

	@Getter
	private final int x, y, z;

	public double getMiddleX()
	{
		return x + 0.5D;
	}

	public double getMiddleY()
	{
		return y + 0.5D;
	}

	public double getMiddleZ()
	{
		return z + 0.5D;
	}

	public World getWorld()
	{
		if(world != null)
		{
			return world;
		}

		world = Bukkit.getWorld(worldName);
		return world;
	}

}
