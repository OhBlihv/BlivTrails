package net.auscraft.BlivTrails.runnables;

import net.auscraft.BlivTrails.BlivTrails;
import net.auscraft.BlivTrails.PlayerConfig;
import net.auscraft.BlivTrails.TrailManager;
import net.auscraft.BlivTrails.util.UUIDUtils;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class MySQLRunnable implements Runnable
{

	static final BlivTrails instance = BlivTrails.getInstance();

	static ConcurrentHashMap<UUID, PlayerConfig> trailMap = null;
	static TrailManager.VanishHook vanishHook;

	final UUID uuid;
	final byte[] uuidBytes;

	public MySQLRunnable(UUID uuid)
	{
		this.uuid = uuid;
		this.uuidBytes = UUIDUtils.toBytes(uuid);
	}

	public static void reload()
	{
		trailMap = TrailManager.getTrailMap();
		vanishHook = TrailManager.getVanishHook();
	}

	public abstract void run();
}
