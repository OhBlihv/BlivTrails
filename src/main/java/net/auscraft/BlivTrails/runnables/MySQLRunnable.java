package net.auscraft.BlivTrails.runnables;

import net.auscraft.BlivTrails.BlivTrails;
import net.auscraft.BlivTrails.util.BUtil;

import java.util.UUID;

public abstract class MySQLRunnable implements Runnable
{

	static final BlivTrails instance = BlivTrails.getInstance();

	final UUID uuid;
	final byte[] uuidBytes;

	public MySQLRunnable(UUID uuid)
	{
		this.uuid = uuid;
		this.uuidBytes = BUtil.toBytes(uuid);
	}

	public abstract void run();
}
